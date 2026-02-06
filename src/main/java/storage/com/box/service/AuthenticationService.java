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
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
@Slf4j
public class AuthenticationService {

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

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        boolean authenticated = passwordEncoder
                .matches(request.getPassword(), user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.AUTHENTICATION_FAIL);

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .status("success")
                .token(token)
                .build();
    }

    public IntrospectResponse introspect(IntrospectRequest request)
            throws AppException, JOSEException {

        var token = request.getToken();

        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (ParseException e) {
            isValid =  false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request)
            throws ParseException, JOSEException {

        var signJwt = verifyToken(request.getToken(), true);

        String jwtId = signJwt.getJWTClaimsSet().getJWTID();

        Date exp = signJwt.getJWTClaimsSet().getExpirationTime();

        InvalidToken invalidToken = InvalidToken.builder()
                .id(jwtId)
                .exp(exp)
                .build();

        invalidTokenRepository.save(invalidToken);

        var name = signJwt.getJWTClaimsSet().getSubject();

        var user = userRepository.findByUserName(name).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_EXIST));

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .status("success")
                .token(token)
                .build();
    }

    public void logout(IntrospectRequest request)
            throws ParseException, JOSEException {
        var signJwt = verifyToken(request.getToken(), false);

        var jwtId = signJwt.getJWTClaimsSet().getJWTID();

        var exp = signJwt.getJWTClaimsSet().getExpirationTime();

        InvalidToken invalidToken = InvalidToken.builder()
                .id(jwtId)
                .exp(exp)
                .build();

        invalidTokenRepository.save(invalidToken);
    }

    public SignedJWT verifyToken(String token, boolean isRefreshToken)
            throws JOSEException, ParseException {

        SignedJWT jwt = SignedJWT.parse(token);

        if (!jwt.getHeader().getAlgorithm().equals(JWSAlgorithm.HS256)) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAIL);
        }

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        if (!jwt.verify(verifier)) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAIL);
        }

        Date expiration = isRefreshToken
                ? new Date(jwt.getJWTClaimsSet().getIssueTime()
                .toInstant().plus(REFRESH_TOKEN_EXPIRATION_TIME, ChronoUnit.SECONDS)
                .toEpochMilli())
                : jwt.getJWTClaimsSet().getExpirationTime();

        if (expiration.before(new Date())) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAIL);
        }

        if (invalidTokenRepository.existsById(jwt.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAIL);
        }

        return jwt;
    }

    String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet claimsSet = new  JWTClaimsSet.Builder()
                .subject(user.getUserName())
//                .issuer("http://localhost:8080/storage")
                .issuer("storage-service")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(ASSERTION_EXPIRATION_TIME, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload  payload = new Payload(claimsSet.toJSONObject());

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
