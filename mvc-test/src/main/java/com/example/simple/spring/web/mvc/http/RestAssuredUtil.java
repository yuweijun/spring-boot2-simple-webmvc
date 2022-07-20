package com.example.simple.spring.web.mvc.http;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

public final class RestAssuredUtil {

    private static final String APPLICATION_JSON_VALUE = "application/json";

    private RestAssuredUtil() {
    }

    public static Map<String, String> getHttpHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", APPLICATION_JSON_VALUE);
        return headers;
    }

    public static RequestSpecification given() {
        RequestSpecification request = RestAssured.given();
        request.headers(getHttpHeaders());
        return request;
    }

}
