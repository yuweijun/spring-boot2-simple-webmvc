package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.controller.dto.UserDTO;
import com.example.simple.spring.web.mvc.http.HttpStatus;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

public class AttributesControllerTest {

    @Test
    public void create() {
        CookieFilter filter = new CookieFilter();

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("test name");
        userDTO.setId(11111);
        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE)
               .filter(filter)
               .when()
               .redirects()
               .follow(true)
               .body(userDTO)
               .post("/create")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.FOUND.code());

        // https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.3
        // If the 302 status code is received in response to a request other than GET or HEAD,
        // the user agent MUST NOT automatically redirect the request
        // unless it can be confirmed by the user,
        // since this might change the conditions under which the request was issued.
        request.header("Content-Type", APPLICATION_JSON_VALUE)
               .filter(filter)
               .get("/show")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.code())
               .body("session_attributes_1", equalTo("session_attributes_1_value"));
    }

    @Test
    public void show() {
        final RequestSpecification request = given();
        request.header("Content-Type", APPLICATION_JSON_VALUE)
               .get("/show")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.code());
    }

}