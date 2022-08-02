package com.example.simple.spring.web.mvc.http.converter.json;

import com.fasterxml.jackson.databind.ser.FilterProvider;

public class MappingJacksonValue {

    private Object value;

    private Class<?> serializationView;

    private FilterProvider filters;

    public MappingJacksonValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Class<?> getSerializationView() {
        return this.serializationView;
    }

    public void setSerializationView(Class<?> serializationView) {
        this.serializationView = serializationView;
    }

    public FilterProvider getFilters() {
        return this.filters;
    }

    public void setFilters(FilterProvider filters) {
        this.filters = filters;
    }

}
