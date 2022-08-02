package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.http.HttpStatus;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

class AnnotationControllerTest {

    @Test
    void annotation() {
        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE);

        request.get("/rest/annotation")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.code());
    }

    @Test
    void diov() {
        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE);

        request.get("/rest/diov")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.code());
    }
}