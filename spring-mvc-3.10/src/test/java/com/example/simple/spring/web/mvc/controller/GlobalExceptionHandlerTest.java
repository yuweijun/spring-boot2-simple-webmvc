package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.http.HttpResponseStatus;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.Test;

import static com.example.simple.spring.web.mvc.http.RestAssuredUtil.given;
import static org.hamcrest.Matchers.equalTo;

public class GlobalExceptionHandlerTest {

    @Test
    public void responseStatus() {
        final RequestSpecification request = given();
        request.get("/responseStatus")
               .prettyPeek()
               .then()
               .statusCode(HttpResponseStatus.BAD_REQUEST.code());
    }

    @Test
    public void loginFailed() {
        final RequestSpecification request = given();
        request.get("/loginFailed")
               .prettyPeek()
               .then()
               .statusCode(HttpResponseStatus.BAD_REQUEST.code())
               .body("exception", equalTo("com.example.simple.spring.web.mvc.controller.exception.AuthenticationFailedException"));
    }

    @Test
    public void notFound() {
        final RequestSpecification request = given();
        request.get("/notFound")
               .prettyPeek()
               .then()
               .statusCode(HttpResponseStatus.NOT_FOUND.code());
    }

}
