package com.example.simple.spring.web.mvc.bind;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.validation.DataBinder;

import java.lang.reflect.Array;

public class WebDataBinder extends DataBinder {

    public static final String DEFAULT_FIELD_MARKER_PREFIX = "_";

    public static final String DEFAULT_FIELD_DEFAULT_PREFIX = "!";

    private String fieldMarkerPrefix = DEFAULT_FIELD_MARKER_PREFIX;

    private String fieldDefaultPrefix = DEFAULT_FIELD_DEFAULT_PREFIX;

    private boolean bindEmptyMultipartFiles = true;

    public WebDataBinder(Object target) {
        super(target);
    }

    public WebDataBinder(Object target, String objectName) {
        super(target, objectName);
    }

    public String getFieldMarkerPrefix() {
        return this.fieldMarkerPrefix;
    }

    public void setFieldMarkerPrefix(String fieldMarkerPrefix) {
        this.fieldMarkerPrefix = fieldMarkerPrefix;
    }

    public String getFieldDefaultPrefix() {
        return this.fieldDefaultPrefix;
    }

    public void setFieldDefaultPrefix(String fieldDefaultPrefix) {
        this.fieldDefaultPrefix = fieldDefaultPrefix;
    }

    public boolean isBindEmptyMultipartFiles() {
        return this.bindEmptyMultipartFiles;
    }

    public void setBindEmptyMultipartFiles(boolean bindEmptyMultipartFiles) {
        this.bindEmptyMultipartFiles = bindEmptyMultipartFiles;
    }

    @Override
    protected void doBind(MutablePropertyValues mpvs) {
        checkFieldDefaults(mpvs);
        checkFieldMarkers(mpvs);
        super.doBind(mpvs);
    }

    protected void checkFieldDefaults(MutablePropertyValues mpvs) {
        if (getFieldDefaultPrefix() != null) {
            String fieldDefaultPrefix = getFieldDefaultPrefix();
            PropertyValue[] pvArray = mpvs.getPropertyValues();
            for (PropertyValue pv : pvArray) {
                if (pv.getName().startsWith(fieldDefaultPrefix)) {
                    String field = pv.getName().substring(fieldDefaultPrefix.length());
                    if (getPropertyAccessor().isWritableProperty(field) && !mpvs.contains(field)) {
                        mpvs.add(field, pv.getValue());
                    }
                    mpvs.removePropertyValue(pv);
                }
            }
        }
    }

    protected void checkFieldMarkers(MutablePropertyValues mpvs) {
        if (getFieldMarkerPrefix() != null) {
            String fieldMarkerPrefix = getFieldMarkerPrefix();
            PropertyValue[] pvArray = mpvs.getPropertyValues();
            for (PropertyValue pv : pvArray) {
                if (pv.getName().startsWith(fieldMarkerPrefix)) {
                    String field = pv.getName().substring(fieldMarkerPrefix.length());
                    if (getPropertyAccessor().isWritableProperty(field) && !mpvs.contains(field)) {
                        Class fieldType = getPropertyAccessor().getPropertyType(field);
                        mpvs.add(field, getEmptyValue(field, fieldType));
                    }
                    mpvs.removePropertyValue(pv);
                }
            }
        }
    }

    protected Object getEmptyValue(String field, Class fieldType) {
        if (fieldType != null && boolean.class.equals(fieldType) || Boolean.class.equals(fieldType)) {
            // Special handling of boolean property.
            return Boolean.FALSE;
        } else if (fieldType != null && fieldType.isArray()) {
            // Special handling of array property.
            return Array.newInstance(fieldType.getComponentType(), 0);
        } else {
            // Default value: try null.
            return null;
        }
    }

}
