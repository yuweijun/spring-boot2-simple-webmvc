package com.example.simple.spring.web.mvc.servlet.view;

import com.example.simple.spring.web.mvc.contex.support.WebApplicationObjectSupport;
import com.example.simple.spring.web.mvc.servlet.support.RequestContext;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public abstract class AbstractView extends WebApplicationObjectSupport implements View, BeanNameAware {

    public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=ISO-8859-1";

    private static final int OUTPUT_BYTE_ARRAY_INITIAL_SIZE = 4096;
    private final Map<String, Object> staticAttributes = new HashMap<>();
    private String beanName;
    private String contentType = DEFAULT_CONTENT_TYPE;
    private String requestContextAttribute;
    private boolean exposePathVariables = true;

    public String getBeanName() {
        return this.beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getRequestContextAttribute() {
        return this.requestContextAttribute;
    }

    public void setRequestContextAttribute(String requestContextAttribute) {
        this.requestContextAttribute = requestContextAttribute;
    }

    public void setAttributesCSV(String propString) throws IllegalArgumentException {
        if (propString != null) {
            StringTokenizer st = new StringTokenizer(propString, ",");
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                int eqIdx = tok.indexOf("=");
                if (eqIdx == -1) {
                    throw new IllegalArgumentException("Expected = in attributes CSV string '" + propString + "'");
                }
                if (eqIdx >= tok.length() - 2) {
                    throw new IllegalArgumentException(
                        "At least 2 characters ([]) required in attributes CSV string '" + propString + "'");
                }
                String name = tok.substring(0, eqIdx);
                String value = tok.substring(eqIdx + 1);

                // Delete first and last characters of value: { and }
                value = value.substring(1);
                value = value.substring(0, value.length() - 1);

                addStaticAttribute(name, value);
            }
        }
    }

    public void setAttributes(Properties attributes) {
        CollectionUtils.mergePropertiesIntoMap(attributes, this.staticAttributes);
    }

    public Map<String, Object> getAttributesMap() {
        return this.staticAttributes;
    }

    public void setAttributesMap(Map<String, ?> attributes) {
        if (attributes != null) {
            for (Map.Entry<String, ?> entry : attributes.entrySet()) {
                addStaticAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    public void addStaticAttribute(String name, Object value) {
        this.staticAttributes.put(name, value);
    }

    public Map<String, Object> getStaticAttributes() {
        return Collections.unmodifiableMap(this.staticAttributes);
    }

    public boolean isExposePathVariables() {
        return exposePathVariables;
    }

    public void setExposePathVariables(boolean exposePathVariables) {
        this.exposePathVariables = exposePathVariables;
    }

    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (logger.isTraceEnabled()) {
            logger.trace("Rendering view with name '" + this.beanName + "' with model " + model +
                " and static attributes " + this.staticAttributes);
        }

        Map<String, Object> mergedModel = createMergedOutputModel(model, request, response);

        prepareResponse(request, response);
        renderMergedOutputModel(mergedModel, request, response);
    }

    protected Map<String, Object> createMergedOutputModel(Map<String, ?> model, HttpServletRequest request,
        HttpServletResponse response) {
        @SuppressWarnings("unchecked")
        Map<String, Object> pathVars = this.exposePathVariables ?
            (Map<String, Object>) request.getAttribute(View.PATH_VARIABLES) : null;

        // Consolidate static and dynamic model attributes.
        int size = this.staticAttributes.size();
        size += (model != null) ? model.size() : 0;
        size += (pathVars != null) ? pathVars.size() : 0;
        Map<String, Object> mergedModel = new HashMap<>(size);
        mergedModel.putAll(this.staticAttributes);
        if (pathVars != null) {
            mergedModel.putAll(pathVars);
        }
        if (model != null) {
            mergedModel.putAll(model);
        }

        // Expose RequestContext?
        if (this.requestContextAttribute != null) {
            mergedModel.put(this.requestContextAttribute, createRequestContext(request, response, mergedModel));
        }

        return mergedModel;
    }

    protected RequestContext createRequestContext(
        HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {

        return new RequestContext(request, response, getServletContext(), model);
    }

    protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
        if (generatesDownloadContent()) {
            response.setHeader("Pragma", "private");
            response.setHeader("Cache-Control", "private, must-revalidate");
        }
    }

    protected boolean generatesDownloadContent() {
        return false;
    }

    protected abstract void renderMergedOutputModel(
        Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception;

    protected void exposeModelAsRequestAttributes(Map<String, Object> model, HttpServletRequest request) throws Exception {
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            String modelName = entry.getKey();
            Object modelValue = entry.getValue();
            if (modelValue != null) {
                request.setAttribute(modelName, modelValue);
                if (logger.isDebugEnabled()) {
                    logger.debug("Added model object '" + modelName + "' of type [" + modelValue.getClass().getName() +
                        "] to request in view with name '" + getBeanName() + "'");
                }
            } else {
                request.removeAttribute(modelName);
                if (logger.isDebugEnabled()) {
                    logger.debug("Removed model object '" + modelName +
                        "' from request in view with name '" + getBeanName() + "'");
                }
            }
        }
    }

    protected ByteArrayOutputStream createTemporaryOutputStream() {
        return new ByteArrayOutputStream(OUTPUT_BYTE_ARRAY_INITIAL_SIZE);
    }

    protected void writeToResponse(HttpServletResponse response, ByteArrayOutputStream baos) throws IOException {
        // Write content type and also length (determined via byte array).
        response.setContentType(getContentType());
        response.setContentLength(baos.size());

        // Flush byte array to servlet output stream.
        ServletOutputStream out = response.getOutputStream();
        baos.writeTo(out);
        out.flush();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        if (getBeanName() != null) {
            sb.append(": name '").append(getBeanName()).append("'");
        } else {
            sb.append(": unnamed");
        }
        return sb.toString();
    }

}
