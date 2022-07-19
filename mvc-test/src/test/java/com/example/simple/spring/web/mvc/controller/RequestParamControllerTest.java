package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.http.HttpStatus;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.simple.spring.web.mvc.http.RestAssuredUtil.given;

public class RequestParamControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestParamControllerTest.class);

    @Test
    void requestParam() {
    }

    @Test
    void requestStringParam() {
    }

    @Test
    void requestParamNamed() {
    }

    @Test
    void requestIntegerParam() {
        final RequestSpecification request = given();
        request.get("/requestIntegerParam?id=id1&username=test")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value());
               // .body("error.message", equalTo("error message"))
               // .body("error.code", equalTo("BadArgument"));

    }
}