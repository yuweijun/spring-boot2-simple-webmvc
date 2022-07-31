package com.example.simple.spring.web.mvc.servlet.view;

import org.springframework.ui.ModelMap;
import org.springframework.validation.DataBinder;

import java.util.Collection;
import java.util.Map;

public class RedirectAttributesModelMap extends ModelMap implements RedirectAttributes {

    private final DataBinder dataBinder;

    private final ModelMap flashAttributes = new ModelMap();

    public RedirectAttributesModelMap() {
        this(null);
    }

    public RedirectAttributesModelMap(DataBinder dataBinder) {
        this.dataBinder = dataBinder;
    }

    @Override
    public Map<String, ?> getFlashAttributes() {
        return this.flashAttributes;
    }

    @Override
    public RedirectAttributesModelMap addAttribute(String attributeName, Object attributeValue) {
        super.addAttribute(attributeName, formatValue(attributeValue));
        return this;
    }

    private String formatValue(Object value) {
        if (value == null) {
            return null;
        }
        return (this.dataBinder != null ? this.dataBinder.convertIfNecessary(value, String.class) : value.toString());
    }

    @Override
    public RedirectAttributesModelMap addAttribute(Object attributeValue) {
        super.addAttribute(attributeValue);
        return this;
    }

    @Override
    public RedirectAttributesModelMap addAllAttributes(Collection<?> attributeValues) {
        super.addAllAttributes(attributeValues);
        return this;
    }

    @Override
    public RedirectAttributesModelMap addAllAttributes(Map<String, ?> attributes) {
        if (attributes != null) {
            attributes.forEach(this::addAttribute);
        }
        return this;
    }

    @Override
    public RedirectAttributesModelMap mergeAttributes(Map<String, ?> attributes) {
        if (attributes != null) {
            attributes.forEach((key, attribute) -> {
                if (!containsKey(key)) {
                    addAttribute(key, attribute);
                }
            });
        }
        return this;
    }

    @Override
    public Map<String, Object> asMap() {
        return this;
    }

    @Override
    public Object put(String key, Object value) {
        return super.put(key, formatValue(value));
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        if (map != null) {
            map.forEach((key, value) -> put(key, formatValue(value)));
        }
    }

    @Override
    public RedirectAttributes addFlashAttribute(String attributeName, Object attributeValue) {
        this.flashAttributes.addAttribute(attributeName, attributeValue);
        return this;
    }

    @Override
    public RedirectAttributes addFlashAttribute(Object attributeValue) {
        this.flashAttributes.addAttribute(attributeValue);
        return this;
    }

}
