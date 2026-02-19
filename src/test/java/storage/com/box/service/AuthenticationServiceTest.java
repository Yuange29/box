package storage.com.box.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import storage.com.box.dto.request.AuthenticationRequest;
import storage.com.box.dto.request.IntrospectRequest;
import storage.com.box.dto.request.RefreshTokenRequest;
import storage.com.box.dto.response.AuthenticationResponse;
import storage.com.box.entity.Permission;
import storage.com.box.entity.Role;
import storage.com.box.entity.User;
import storage.com.box.exception.AppException;
import storage.com.box.exception.ErrorCode;
import storage.com.box.repository.InvalidTokenRepository;
import storage.com.box.repository.UserRepository;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/test.properties")
public class AuthenticationServiceTest {

    @MockitoBean
    UserRepository userRepository;
    @MockitoBean
    InvalidTokenRepository invalidTokenRepository;

    @MockitoSpyBean
    AuthenticationService authenticationService;

    AuthenticationRequest request;
    AuthenticationResponse response;
    IntrospectRequest introspectRequest;
    RefreshTokenRequest refreshTokenRequest;
    Permission permission;
    Role role;
    User user;

    @BeforeEach
    void initData() {

        request = AuthenticationRequest.builder()
                .userName("test user")
                .password("password")
                .build();

        response = AuthenticationResponse.builder()
                .status("success")
                .accessToken("token")
                .build();

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        permission = Permission.builder()
                .name("TEST")
                .description("permission test")
                .build();

        role = Role.builder()
                .name("USER")
                .permissions(java.util.Set.of(permission))
                .build();

        user = User.builder()
                .userId("dsf2sdAsa2sdSdf")
                .userName("test user")
                .password(passwordEncoder.encode("password"))
                .email("test@gamil.com")
                .roles(Set.of(role))
                .build();

    }

    @Test
    void authenticate_validRequest_success() {
        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.of(user));

        var response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());

        verify(userRepository).findByUserName(request.getUserName());
    }

    @Test
    void authenticate_invalidUser_fail() {
        when(userRepository.findByUserName(request.getUserName()))
                .thenReturn(Optional.empty());

        var exception = assertThrows(AppException.class,
                () -> authenticationService.authenticate(request));

        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.USER_NOT_EXIST.getMessage());
    }

    @Test
    void authenticate_invalidPassword_fail() {
        when(userRepository.findByUserName(request.getUserName()))
                .thenReturn(Optional.of(user));

        request.setPassword("fail_password");

        var exception = assertThrows(AppException.class,
                () -> authenticationService.authenticate(request));

        assertThat(exception.getMessage()).isEqualTo("authenticate fail");
    }

    @Test
    void introspect_validRequest_success() throws Exception {
        introspectRequest = IntrospectRequest.builder()
                .token("validToken")
                .build();

        SignedJWT signedJWT =mock(SignedJWT.class);

        doReturn(signedJWT)
                .when(authenticationService)
                .verifyToken("validToken", "access");

        var response = authenticationService.introspect(introspectRequest);

        assertThat(response.isValid()).isTrue();
    }

    @Test
    void introspect_invalidRequest_fail() throws Exception {
        introspectRequest = IntrospectRequest.builder()
                .token("invalidToken")
                .build();

        doThrow(new ParseException("Invalid Token", 0))
                .when(authenticationService)
                .verifyToken("invalidToken", "access");

        var response = authenticationService.introspect(introspectRequest);

        assertThat(response.isValid()).isFalse();
    }

    @Test
    void refreshToken_validRequest_success() throws Exception {

        refreshTokenRequest = RefreshTokenRequest.builder()
                .token("refreshToken")
                .build();

        SignedJWT signedJWT = mock(SignedJWT.class);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .jwtID(user.getUserId())
                .subject(user.getUserName())
                .expirationTime(new Date(System.currentTimeMillis() + 100000))
                .build();

        when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        doReturn(signedJWT)
                .when(authenticationService)
                .verifyToken(refreshTokenRequest.getToken(), "refresh");

        when(userRepository.findByUserName(request.getUserName()))
                .thenReturn(Optional.of(user));

        doReturn("newToken").when(authenticationService).generateToken(user, true);

        var response = authenticationService.refreshToken(refreshTokenRequest);

        assertThat(response.getAccessToken()).isEqualTo("newToken");

    }

    @Test
    void refreshToken_invalidUser_fail() throws Exception {

        refreshTokenRequest = RefreshTokenRequest.builder()
                .token("refreshToken")
                .build();

        SignedJWT signedJWT = mock(SignedJWT.class);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .jwtID(user.getUserId())
                .subject("invalidUser")
                .expirationTime(new Date(System.currentTimeMillis() + 100000))
                .build();

        when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        doReturn(signedJWT)
                .when(authenticationService)
                .verifyToken(refreshTokenRequest.getToken(), "refresh");

        when(userRepository.findByUserName(ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        var exception = assertThrows(AppException.class,() ->
                authenticationService.refreshToken(refreshTokenRequest));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.USER_NOT_EXIST.getMessage());

    }

    @Test
    void logout_validRequest_success() throws Exception {

        introspectRequest = IntrospectRequest.builder()
                .token("existToken")
                .build();

        SignedJWT signedJWT = mock(SignedJWT.class);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .jwtID("testID")
                .expirationTime(new Date(System.currentTimeMillis() + 1000))
                .build();

        when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        doReturn(signedJWT)
                .when(authenticationService)
                .verifyToken("existToken", "access");

        authenticationService.logout(introspectRequest);

        verify(invalidTokenRepository)
                .save(argThat(token ->
                        token.getId().equals("testID")));

    }

    @Test
    void logout_invalidToken_throwException() throws Exception {
        IntrospectRequest request = IntrospectRequest.builder()
                .token("invalid-token")
                .build();

        doThrow(new ParseException("invalid", 0))
                .when(authenticationService)
                .verifyToken("invalid-token", "access");

        assertThrows(ParseException.class,
                () -> authenticationService.logout(request));

        verify(invalidTokenRepository, never()).save(any());
    }

    @Test
    void verifyToken_validRequest_success() throws Exception {
        String token = authenticationService.generateToken(user, false);

        when(invalidTokenRepository.existsById(anyString()))
                .thenReturn(false);

        SignedJWT jwt = authenticationService.verifyToken(token, "access");

        assertNotNull(jwt);
        assertEquals("test user", jwt.getJWTClaimsSet().getSubject());
    }

    // generate Exp token
    private String generateExpiredToken(User user) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUserName())
                .issuer("http://locahost:8080/storage")
                .issueTime(Date.from(Instant.now().minusSeconds(3600)))
                .expirationTime(Date.from(Instant.now().minusSeconds(10))) // HẾT HẠN
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", "USER")
                .build();

        com.nimbusds.jose.Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        jwsObject.sign(new MACSigner("12345678901234567890123456789012".getBytes()));

        return jwsObject.serialize();
    }

    @Test
    void verifyToken_expiredToken_fail() throws Exception {
        String expiredToken = generateExpiredToken(user);

        when(invalidTokenRepository.existsById(anyString()))
                .thenReturn(true);

        assertThrows(AppException.class,
                () -> authenticationService.verifyToken(expiredToken, "access"));
    }

    @Test
    void verifyToken_invalidRequest_fail() {
        String token = authenticationService.generateToken(user, false);

        when(invalidTokenRepository.existsById(anyString()))
                .thenReturn(true);

        var exception = assertThrows(AppException.class,() ->
                authenticationService.verifyToken(token, "access"));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.AUTHENTICATION_FAIL.getMessage());

    }

    @Test
    void buildScope_valid_success() {

        String scope = authenticationService.buildScope(user);

        assertThat(scope).isEqualTo("USER TEST");
    }

    @Test
    void buildScope_invalid_fail() {
        user.setRoles(null);

        String scope = authenticationService.buildScope(user);

        assertThat(scope).isEmpty();
    }

}
