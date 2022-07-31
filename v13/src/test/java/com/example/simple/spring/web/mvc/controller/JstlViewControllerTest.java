package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.http.HttpResponseStatus;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static com.example.simple.spring.web.mvc.http.RestAssuredUtil.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

public class JstlViewControllerTest {

    @Test
    public void jstlView() {
        final RequestSpecification request = given();
        request.get("/jstlView")
               .prettyPeek()
               .then()
               .statusCode(HttpResponseStatus.OK.code())
               .body(containsString("<h2>Hello World</h2>"));
    }

    @Test
    public void redirectView() {
        final RequestSpecification request = given();
        request.get("/redirectView")
               .prettyPeek()
               .then()
               .statusCode(HttpResponseStatus.OK.code())
               .body(containsString("<h2>Hello World</h2>"));
    }

}