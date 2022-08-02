package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.http.HttpStatus;
import io.restassured.filter.session.SessionFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

class ScopedBeanControllerTest {

    @Test
    void requestScope() {
        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE);

        request.get("/requestScope")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.code())
               .body(containsString("bean index value"));

        final String body1 = request.get("/requestScope").prettyPeek().as(String.class);
        final String body2 = request.get("/requestScope").prettyPeek().as(String.class);
        assertNotEquals(body1, body2);
    }

    @Test
    void sessionScope() {
        SessionFilter filter = new SessionFilter();
        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE);

        request.filter(filter)
               .get("/sessionScope")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.code())
               .body(containsString("bean index value"));

        final String body1 = request.filter(filter).get("/sessionScope").prettyPeek().as(String.class);
        final String body2 = request.filter(filter).get("/sessionScope").prettyPeek().as(String.class);
        assertEquals(body1, body2);
    }

    @Test
    void sessionInvalidate() {
        SessionFilter filter = new SessionFilter();

        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE);

        request.filter(filter)
               .get("/sessionInvalidate")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.code())
               .body(containsString("bean index value"));

        final String body1 = request.filter(filter).get("/sessionInvalidate").prettyPeek().as(String.class);
        final String body2 = request.filter(filter).get("/sessionInvalidate").prettyPeek().as(String.class);
        assertNotEquals(body1, body2);
    }
}