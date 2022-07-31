package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.http.HttpStatus;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

public class GlobalExceptionHandlerTest {

    @Test
    public void responseStatus() {
        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE);

        request.get("/responseStatus")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.code());
    }

    @Test
    public void loginFailed() {
        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE);

        request.get("/loginFailed")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.code())
               .body("exception", equalTo("com.example.simple.spring.web.mvc.controller.exception.AuthenticationFailedException"));
    }

    @Test
    public void notFound() {
        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE);

        request.get("/notFound")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.code());
    }

    @Test
    public void illegalArgumentException() {
        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE);

        request.get("/illegalArgumentException")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.BAD_GATEWAY.code());
    }
}
