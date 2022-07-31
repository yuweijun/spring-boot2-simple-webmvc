package com.example.simple.spring.web.mvc.http.converter.json;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import org.springframework.lang.Nullable;

public class MappingJacksonValue {

    private Object value;

    
    private Class<?> serializationView;

    
    private FilterProvider filters;

    public MappingJacksonValue(Object value) {
        this.value = value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    public void setSerializationView( Class<?> serializationView) {
        this.serializationView = serializationView;
    }

    
    public Class<?> getSerializationView() {
        return this.serializationView;
    }

    public void setFilters( FilterProvider filters) {
        this.filters = filters;
    }

    
    public FilterProvider getFilters() {
        return this.filters;
    }

}
