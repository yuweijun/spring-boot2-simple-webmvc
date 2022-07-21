package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.http.HttpResponseStatus;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.Test;

import static com.example.simple.spring.web.mvc.http.RestAssuredUtil.given;
import static org.hamcrest.Matchers.equalTo;

public class RequestParamControllerTest {

    @Test
    public void requestParam() {
    }

    @Test
    public void requestStringParam() {
    }

    @Test
    public void requestParamNamed() {
        final RequestSpecification request = given();
        request.get("/requestParamNamed?id=1&username=test")
               .prettyPeek()
               .then()
               .statusCode(HttpResponseStatus.OK.code());
    }

    @Test
    public void requestIntegerParamException() {
        final RequestSpecification request = given();
        request.get("/requestIntegerParam?id=not-number&username=test")
               .prettyPeek()
               .then()
               .statusCode(HttpResponseStatus.BAD_REQUEST.code())
               .body("error", equalTo("org.springframework.beans.TypeMismatchException"));
    }

    @Test
    public void requestIntegerParam() {
        final RequestSpecification request = given();
        request.get("/requestIntegerParam?id=1&username=test")
               .prettyPeek()
               .then()
               .statusCode(HttpResponseStatus.OK.code())
               .body("id", equalTo(1));
    }
}