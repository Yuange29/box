package storage.com.box.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import storage.com.box.dto.request.AuthenticationRequest;
import storage.com.box.dto.request.IntrospectRequest;
import storage.com.box.dto.request.RefreshTokenRequest;
import storage.com.box.dto.response.AuthenticationResponse;
import storage.com.box.dto.response.IntrospectResponse;
import storage.com.box.entity.InvalidToken;
import storage.com.box.entity.User;
import storage.com.box.exception.AppException;
import storage.com.box.exception.ErrorCode;
import storage.com.box.repository.InvalidTokenRepository;
import storage.com.box.repository.UserRepository;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
@Slf4j
public class AuthenticationService {

    final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    final UserRepository userRepository;
    final InvalidTokenRepository invalidTokenRepository;

    @Value("${jwt.secret}")
    protected String SIGNER_KEY;

    @Value("${jwt.access-token-expiration}")
    protected long ASSERTION_EXPIRATION_TIME;

    @Value("${jwt.refresh-token-expiration}")
    protected long REFRESH_TOKEN_EXPIRATION_TIME;

    public AuthenticationResponse authenticate(AuthenticationRequest request)
            throws AppException {

        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        boolean authenticated = passwordEncoder
                .matches(request.getPassword(), user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.AUTHENTICATION_FAIL);

        String accessToken = generateToken(user, false);
        String refreshToken = generateToken(user, true);

        return AuthenticationResponse.builder()
                .status("success")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public IntrospectResponse introspect(IntrospectRequest request)
            throws AppException, JOSEException {

        var token = request.getToken();

        boolean isValid = true;

        try {
            verifyToken(token, "access");
        } catch (ParseException e) {
            isValid =  false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request)
            throws ParseException, JOSEException {

        // Kiểm tra phải là refresh token
        SignedJWT signedJWT = verifyToken(request.getToken(), "refresh");

        // Blacklist refresh token cũ
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidToken invalidToken = InvalidToken.builder()
                .id(jwtId)
                .exp(exp)
                .build();

        invalidTokenRepository.save(invalidToken);

        // Lấy thông tin user
        String userName = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        // Generate cả 2 token mới — token rotation
        String newAccessToken = generateToken(user, false);
        String newRefreshToken = generateToken(user, true);

        return AuthenticationResponse.builder()
                .status("success")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(IntrospectRequest request)
            throws ParseException, JOSEException {
        try {
            var signJwt = verifyToken(request.getToken(), "access");

            InvalidToken invalidToken = InvalidToken.builder()
                    .id(signJwt.getJWTClaimsSet().getJWTID())
                    .exp(signJwt.getJWTClaimsSet().getExpirationTime())
                    .build();

            invalidTokenRepository.save(invalidToken);

        } catch (AppException e) {
            // Token hết hạn hoặc không hợp lệ → coi như đã logout thành công
        }
    }

    public SignedJWT verifyToken(String token, String expectedTokenType)
            throws JOSEException, ParseException {

        SignedJWT jwt = SignedJWT.parse(token);

        // Check algorithm
        if (!jwt.getHeader().getAlgorithm().equals(JWSAlgorithm.HS256)) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAIL);
        }

        // Check chữ ký
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        if (!jwt.verify(verifier)) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAIL);
        }

        // Check issuer
        if (!"storage-service".equals(jwt.getJWTClaimsSet().getIssuer())) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAIL);
        }

        // Check token_type — "access" or "refresh"
        String tokenType = jwt.getJWTClaimsSet().getStringClaim("token_type");
        if (!expectedTokenType.equals(tokenType)) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAIL);
        }

        if (jwt.getJWTClaimsSet().getExpirationTime().before(new Date())) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAIL);
        }

        if (invalidTokenRepository.existsById(jwt.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAIL);
        }

        return jwt;
    }

    String generateToken(User user, boolean isRefresh) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        long expirationSeconds = isRefresh
                ? REFRESH_TOKEN_EXPIRATION_TIME   // ví dụ: 7 ngày
                : ASSERTION_EXPIRATION_TIME;       // ví dụ: 15 phút

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUserName())
                .issuer("storage-service")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(expirationSeconds, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("token_type", isRefresh ? "refresh" : "access") // ← thêm mới
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add(role.getName());
                role.getPermissions().forEach(permission -> {
                    stringJoiner.add(permission.getName());
                });
            });
        }

        return stringJoiner.toString();
    }

}
