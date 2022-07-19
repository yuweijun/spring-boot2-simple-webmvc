package com.example.simple.spring.web.mvc.http;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.specification.RequestSpecification;

import java.util.Collections;
import java.util.Map;

public final class RestAssuredUtil {

    private RestAssuredUtil() {
    }

    public static Map<String, String> getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers.toSingleValueMap();
    }

    public static RequestSpecification given() {
        RequestSpecification request = RestAssured.given();
        request.headers(getHttpHeaders())
               .header("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return request;
    }

}
