package storage.com.box.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    INVALID_USERNAME(1001, "user name must be at least 6 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1002, "password must be at least 6 characters", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1003, "your email is invalid", HttpStatus.BAD_REQUEST),
    USER_ALREADY_EXIST(  1003,"user already exist", HttpStatus.BAD_REQUEST),
    USER_NOT_EXIST(  1004,"user not exist", HttpStatus.NOT_FOUND),
    AUTHORIZATION_FAILED(1005, "Authentication failed", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1006, "access denied", HttpStatus.FORBIDDEN),
    AUTHENTICATION_FAIL(1007, "authenticate fail", HttpStatus.BAD_REQUEST),
    CATEGORY_EXISTS(1008, "category already exists", HttpStatus.BAD_REQUEST),
    FEE_NOT_FOUND(1009, "fee not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_EXIST(1010, "category not found", HttpStatus.NOT_FOUND),

    ;

    int code;
    String message;
    HttpStatus httpStatus;

    ErrorCode(int code, String message,  HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

}
