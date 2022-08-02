package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.http.HttpStatus;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

public class RequestParamControllerTest {

    @Test
    public void requestParamNamed() {
        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE);

        request.get("/requestParamNamed?id=1&username=test")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.code());
    }

    @Test
    public void requestIntegerParamException() {
        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE);

        request.get("/requestIntegerParam?id=not-number&username=test")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.code())
               .body("error", equalTo("org.springframework.beans.TypeMismatchException"));
    }

    @Test
    public void requestIntegerParam() {
        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE);

        request.get("/requestIntegerParam?id=1&username=test")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.code())
               .body("id", equalTo(1));
    }
}