package com.example.simple.spring.web.mvc.servlet.view;

import com.example.simple.spring.web.mvc.http.HttpStatus;
import com.example.simple.spring.web.mvc.servlet.HandlerMapping;
import com.example.simple.spring.web.mvc.servlet.support.RequestContext;
import com.example.simple.spring.web.util.WebUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedirectView extends AbstractUrlBasedView implements SmartView {

    private static final Pattern URI_TEMPLATE_VARIABLE_PATTERN = Pattern.compile("\\{([^/]+?)\\}");

    private boolean contextRelative = false;

    private boolean http10Compatible = true;

    private boolean exposeModelAttributes = true;

    private String encodingScheme;

    private HttpStatus statusCode;

    public RedirectView() {
        setExposePathVariables(false);
    }

    public RedirectView(String url) {
        super(url);
        setExposePathVariables(false);
    }

    public RedirectView(String url, boolean contextRelative) {
        super(url);
        this.contextRelative = contextRelative;
        setExposePathVariables(false);
    }

    public RedirectView(String url, boolean contextRelative, boolean http10Compatible) {
        super(url);
        this.contextRelative = contextRelative;
        this.http10Compatible = http10Compatible;
        setExposePathVariables(false);
    }

    public RedirectView(String url, boolean contextRelative, boolean http10Compatible, boolean exposeModelAttributes) {
        super(url);
        this.contextRelative = contextRelative;
        this.http10Compatible = http10Compatible;
        this.exposeModelAttributes = exposeModelAttributes;
        setExposePathVariables(false);
    }

    public void setContextRelative(boolean contextRelative) {
        this.contextRelative = contextRelative;
    }

    public void setHttp10Compatible(boolean http10Compatible) {
        this.http10Compatible = http10Compatible;
    }

    public void setExposeModelAttributes(final boolean exposeModelAttributes) {
        this.exposeModelAttributes = exposeModelAttributes;
    }

    public void setEncodingScheme(String encodingScheme) {
        this.encodingScheme = encodingScheme;
    }

    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isRedirectView() {
        return true;
    }

    @Override
    protected boolean isContextRequired() {
        return false;
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String targetUrl = createTargetUrl(model, request);
        targetUrl = updateTargetUrl(targetUrl, model, request, response);

        // FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        // if (!CollectionUtils.isEmpty(flashMap)) {
        // 	UriComponents uriComponents = UriComponentsBuilder.fromUriString(targetUrl).build();
        // 	flashMap.setTargetRequestPath(uriComponents.getPath());
        // 	flashMap.addTargetRequestParams(uriComponents.getQueryParams());
        // }

        sendRedirect(request, response, targetUrl, this.http10Compatible);
    }

    protected final String createTargetUrl(Map<String, Object> model, HttpServletRequest request) throws UnsupportedEncodingException {

        // Prepare target URL.
        StringBuilder targetUrl = new StringBuilder();
        if (this.contextRelative && getUrl().startsWith("/")) {
            // Do not apply context path to relative URLs.
            targetUrl.append(request.getContextPath());
        }
        targetUrl.append(getUrl());

        String enc = this.encodingScheme;
        if (enc == null) {
            enc = request.getCharacterEncoding();
        }
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }

        if (StringUtils.hasText(targetUrl)) {
            Map<String, String> variables = getCurrentRequestUriVariables(request);
            targetUrl = replaceUriTemplateVariables(targetUrl.toString(), model, variables, enc);
        }

        if (this.exposeModelAttributes) {
            appendQueryProperties(targetUrl, model, enc);
        }

        return targetUrl.toString();
    }

    protected StringBuilder replaceUriTemplateVariables(String targetUrl, Map<String, Object> model, Map<String, String> currentUriVariables, String encodingScheme) throws UnsupportedEncodingException {

        StringBuilder result = new StringBuilder();
        Matcher m = URI_TEMPLATE_VARIABLE_PATTERN.matcher(targetUrl);
        int endLastMatch = 0;
        while (m.find()) {
            String name = m.group(1);
            Object value = model.containsKey(name) ? model.remove(name) : currentUriVariables.get(name);
            Assert.notNull(value, "Model has no value for '" + name + "'");
            result.append(targetUrl, endLastMatch, m.start());
            result.append(value);
            endLastMatch = m.end();
        }
        result.append(targetUrl.substring(endLastMatch));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getCurrentRequestUriVariables(HttpServletRequest request) {
        Map<String, String> uriVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return (uriVars != null) ? uriVars : Collections.emptyMap();
    }

    protected void appendQueryProperties(StringBuilder targetUrl, Map<String, Object> model, String encodingScheme) throws UnsupportedEncodingException {

        // Extract anchor fragment, if any.
        String fragment = null;
        int anchorIndex = targetUrl.indexOf("#");
        if (anchorIndex > -1) {
            fragment = targetUrl.substring(anchorIndex);
            targetUrl.delete(anchorIndex, targetUrl.length());
        }

        // If there aren't already some parameters, we need a "?".
        boolean first = (targetUrl.toString().indexOf('?') < 0);
        for (Map.Entry<String, Object> entry : queryProperties(model).entrySet()) {
            Object rawValue = entry.getValue();
            Iterator<Object> valueIter;
            if (rawValue != null && rawValue.getClass().isArray()) {
                valueIter = Arrays.asList(ObjectUtils.toObjectArray(rawValue)).iterator();
            } else if (rawValue instanceof Collection) {
                valueIter = ((Collection) rawValue).iterator();
            } else {
                valueIter = Collections.singleton(rawValue).iterator();
            }
            while (valueIter.hasNext()) {
                Object value = valueIter.next();
                if (first) {
                    targetUrl.append('?');
                    first = false;
                } else {
                    targetUrl.append('&');
                }
                String encodedKey = urlEncode(entry.getKey(), encodingScheme);
                String encodedValue = (value != null ? urlEncode(value.toString(), encodingScheme) : "");
                targetUrl.append(encodedKey).append('=').append(encodedValue);
            }
        }

        // Append anchor fragment, if any, to end of URL.
        if (fragment != null) {
            targetUrl.append(fragment);
        }
    }

    protected Map<String, Object> queryProperties(Map<String, Object> model) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            if (isEligibleProperty(entry.getKey(), entry.getValue())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    protected boolean isEligibleProperty(String key, Object value) {
        if (value == null) {
            return false;
        }
        if (isEligibleValue(value)) {
            return true;
        }

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            if (length == 0) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                Object element = Array.get(value, i);
                if (!isEligibleValue(element)) {
                    return false;
                }
            }
            return true;
        }

        if (value instanceof Collection) {
            Collection coll = (Collection) value;
            if (coll.isEmpty()) {
                return false;
            }
            for (Object element : coll) {
                if (!isEligibleValue(element)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    protected boolean isEligibleValue(Object value) {
        return (value != null && BeanUtils.isSimpleValueType(value.getClass()));
    }

    protected String urlEncode(String input, String encodingScheme) throws UnsupportedEncodingException {
        return (input != null ? URLEncoder.encode(input, encodingScheme) : null);
    }

    protected String updateTargetUrl(String targetUrl, Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

        RequestContext requestContext = null;
        if (getWebApplicationContext() != null) {
            requestContext = createRequestContext(request, response, model);
        }

        // else {
        // 	SimpleWebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        // 	if (wac != null && wac.getServletContext() != null) {
        // 		requestContext = new RequestContext(request, response, wac.getServletContext(), model);
        // 	}
        // }

        // if (requestContext != null) {
        // 	RequestDataValueProcessor processor = requestContext.getRequestDataValueProcessor();
        // 	if (processor != null) {
        // 		targetUrl = processor.processUrl(request, targetUrl);
        // 	}
        // }

        return targetUrl;
    }

    protected void sendRedirect(HttpServletRequest request, HttpServletResponse response, String targetUrl, boolean http10Compatible) throws IOException {
        String encodedRedirectURL = response.encodeRedirectURL(targetUrl);

        if (http10Compatible) {
            if (this.statusCode != null) {
                response.setStatus(this.statusCode.code());
                response.setHeader("Location", encodedRedirectURL);
            } else {
                // Send status code 302 by default.
                response.sendRedirect(encodedRedirectURL);
            }
        } else {
            HttpStatus statusCode = getHttp11StatusCode(request, response, targetUrl);
            response.setStatus(statusCode.code());
            response.setHeader("Location", encodedRedirectURL);
        }
    }

    protected HttpStatus getHttp11StatusCode(HttpServletRequest request, HttpServletResponse response, String targetUrl) {

        if (this.statusCode != null) {
            return this.statusCode;
        }
        HttpStatus attributeStatusCode = (HttpStatus) request.getAttribute(View.RESPONSE_STATUS_ATTRIBUTE);
        if (attributeStatusCode != null) {
            return attributeStatusCode;
        }
        return HttpStatus.SEE_OTHER;
    }

}
