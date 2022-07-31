package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.http.HttpStatus;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.util.MimeTypeUtils.TEXT_HTML_VALUE;

public class JstlViewControllerTest {

    @Test
    public void jstlView() {
        final RequestSpecification request = given();
        request.header("Content-Type", TEXT_HTML_VALUE);

        request.get("/jstlView")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.code())
               .body(containsString("<h2>Hello World</h2>"));
    }

    @Test
    public void redirectView() {
        final RequestSpecification request = given();
        request.header("Content-Type", TEXT_HTML_VALUE);

        request.get("/redirectView")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.code())
               .body(containsString("<h2>Hello World</h2>"));
    }

    @Test
    public void redirectViewWithoutRedirect() {
        final RequestSpecification request = given();
        request.header("Content-Type", TEXT_HTML_VALUE);

        request.when().redirects().follow(false)
               .get("/redirectView")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.FOUND.code());
    }
}