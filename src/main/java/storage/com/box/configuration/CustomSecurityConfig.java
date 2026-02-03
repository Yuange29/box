package storage.com.box.configuration;

import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import storage.com.box.dto.request.IntrospectRequest;
import storage.com.box.exception.AppException;
import storage.com.box.exception.ErrorCode;
import storage.com.box.service.AuthenticationService;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomSecurityConfig implements JwtDecoder {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    AuthenticationService authenticationService;
    NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String jwtToken) {

        try {
            var response = authenticationService.introspect(IntrospectRequest.builder()
                    .token(jwtToken)
                    .build());

            if (!response.isValid()) throw new AppException(ErrorCode.AUTHENTICATION_FAIL);

        } catch (JOSEException e) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAIL);
        }

        if (Objects.isNull(nimbusJwtDecoder)) {

            SecretKey secretKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");

            nimbusJwtDecoder = NimbusJwtDecoder
                    .withSecretKey(secretKey)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();

        }

        return nimbusJwtDecoder.decode(jwtToken);
    }

}
