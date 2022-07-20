package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.http.HttpStatus;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static com.example.simple.spring.web.mvc.http.RestAssuredUtil.given;
import static org.hamcrest.Matchers.equalTo;

public class GlobalExceptionHandlerTest {

    @Test
    public void loginFailed() {
        final RequestSpecification request = given();
        request.get("/loginFailed")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("exception", equalTo("com.example.simple.spring.web.mvc.controller.exception.AuthenticationFailedException"));
    }

}
