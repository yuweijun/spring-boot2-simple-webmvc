package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMethod;

import java.util.Arrays;
import java.util.Map;

public class RequestMappingInfo {

    private String name;

    private String[] value;

    private String[] path;

    private RequestMethod[] method;

    private String[] params;

    private String[] headers;

    private String[] consumes;

    private String[] produces;

    private Map<String, Object> map;

    public RequestMappingInfo(Map<String, Object> map) {
        this.map = map;
    }

    public Object get(String key) {
        return map.get(key);
    }

    public String getName() {
        return (String) map.get("name");
    }

    public String[] getValue() {
        return (String[]) map.get("value");
    }

    public String[] getPath() {
        return (String[]) map.get("path");
    }

    public RequestMethod[] getMethod() {
        return (RequestMethod[]) map.get("method");
    }

    public String[] getParams() {
        return (String[]) map.get("params");
    }

    public String[] getHeaders() {
        return (String[]) map.get("headers");
    }

    public String[] getConsumes() {
        return (String[]) map.get("consumes");
    }

    public String[] getProduces() {
        return (String[]) map.get("produces");
    }

    @Override
    public String toString() {
        return "RequestMappingInfo{" +
            "name='" + getName() + "'" +
            ", value=" + Arrays.toString(getValue()) +
            ", path=" + Arrays.toString(getPath()) +
            ", method=" + Arrays.toString(getMethod()) +
            ", params=" + Arrays.toString(getParams()) +
            ", headers=" + Arrays.toString(getHeaders()) +
            ", consumes=" + Arrays.toString(getConsumes()) +
            ", produces=" + Arrays.toString(getProduces()) +
            "}";
    }
}
