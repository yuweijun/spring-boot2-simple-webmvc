package com.example.simple.spring.web.mvc.util;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

public abstract class WebUtils {

    public static final String INCLUDE_REQUEST_URI_ATTRIBUTE = "javax.servlet.include.request_uri";
    public static final String INCLUDE_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.include.context_path";
    public static final String INCLUDE_SERVLET_PATH_ATTRIBUTE = "javax.servlet.include.servlet_path";
    public static final String INCLUDE_PATH_INFO_ATTRIBUTE = "javax.servlet.include.path_info";
    public static final String INCLUDE_QUERY_STRING_ATTRIBUTE = "javax.servlet.include.query_string";

    public static final String FORWARD_REQUEST_URI_ATTRIBUTE = "javax.servlet.forward.request_uri";
    public static final String FORWARD_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.forward.context_path";
    public static final String FORWARD_SERVLET_PATH_ATTRIBUTE = "javax.servlet.forward.servlet_path";
    public static final String FORWARD_PATH_INFO_ATTRIBUTE = "javax.servlet.forward.path_info";
    public static final String FORWARD_QUERY_STRING_ATTRIBUTE = "javax.servlet.forward.query_string";

    public static final String ERROR_STATUS_CODE_ATTRIBUTE = "javax.servlet.error.status_code";
    public static final String ERROR_EXCEPTION_TYPE_ATTRIBUTE = "javax.servlet.error.exception_type";
    public static final String ERROR_MESSAGE_ATTRIBUTE = "javax.servlet.error.message";
    public static final String ERROR_EXCEPTION_ATTRIBUTE = "javax.servlet.error.exception";
    public static final String ERROR_REQUEST_URI_ATTRIBUTE = "javax.servlet.error.request_uri";
    public static final String ERROR_SERVLET_NAME_ATTRIBUTE = "javax.servlet.error.servlet_name";

    public static final String CONTENT_TYPE_CHARSET_PREFIX = ";charset=";

    public static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";

    public static final String TEMP_DIR_CONTEXT_ATTRIBUTE = "javax.servlet.context.tempdir";

    public static final String HTML_ESCAPE_CONTEXT_PARAM = "defaultHtmlEscape";

    public static final String WEB_APP_ROOT_KEY_PARAM = "webAppRootKey";

    public static final String DEFAULT_WEB_APP_ROOT_KEY = "webapp.root";

    public static final String[] SUBMIT_IMAGE_SUFFIXES = {".x", ".y"};

    public static final String SESSION_MUTEX_ATTRIBUTE = WebUtils.class.getName() + ".MUTEX";

    public static void setWebAppRootSystemProperty(ServletContext servletContext) throws IllegalStateException {
        Assert.notNull(servletContext, "ServletContext must not be null");
        String root = servletContext.getRealPath("/");
        if (root == null) {
            throw new IllegalStateException(
                "Cannot set web app root system property when WAR file is not expanded");
        }
        String param = servletContext.getInitParameter(WEB_APP_ROOT_KEY_PARAM);
        String key = (param != null ? param : DEFAULT_WEB_APP_ROOT_KEY);
        String oldValue = System.getProperty(key);
        if (oldValue != null && !StringUtils.pathEquals(oldValue, root)) {
            throw new IllegalStateException(
                "Web app root system property already set to different value: '" +
                    key + "' = [" + oldValue + "] instead of [" + root + "] - " +
                    "Choose unique values for the 'webAppRootKey' context-param in your web.xml files!");
        }
        System.setProperty(key, root);
        servletContext.log("Set web app root system property: '" + key + "' = [" + root + "]");
    }

    public static void removeWebAppRootSystemProperty(ServletContext servletContext) {
        Assert.notNull(servletContext, "ServletContext must not be null");
        String param = servletContext.getInitParameter(WEB_APP_ROOT_KEY_PARAM);
        String key = (param != null ? param : DEFAULT_WEB_APP_ROOT_KEY);
        System.getProperties().remove(key);
    }

    public static boolean isDefaultHtmlEscape(ServletContext servletContext) {
        if (servletContext == null) {
            return false;
        }
        String param = servletContext.getInitParameter(HTML_ESCAPE_CONTEXT_PARAM);
        return Boolean.valueOf(param);
    }

    public static Boolean getDefaultHtmlEscape(ServletContext servletContext) {
        if (servletContext == null) {
            return null;
        }
        Assert.notNull(servletContext, "ServletContext must not be null");
        String param = servletContext.getInitParameter(HTML_ESCAPE_CONTEXT_PARAM);
        return (StringUtils.hasText(param) ? Boolean.valueOf(param) : null);
    }

    public static File getTempDir(ServletContext servletContext) {
        Assert.notNull(servletContext, "ServletContext must not be null");
        return (File) servletContext.getAttribute(TEMP_DIR_CONTEXT_ATTRIBUTE);
    }

    
    public static String getRealPath(ServletContext servletContext, String path) throws FileNotFoundException {
        Assert.notNull(servletContext, "ServletContext must not be null");
        // Interpret location as relative to the web application root directory.
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String realPath = servletContext.getRealPath(path);
        if (realPath == null) {
            throw new FileNotFoundException(
                "ServletContext resource [" + path + "] cannot be resolved to absolute file path - " +
                    "web application archive not expanded?");
        }
        return realPath;
    }

    
    public static String getSessionId(HttpServletRequest request) {
        Assert.notNull(request, "Request must not be null");
        HttpSession session = request.getSession(false);
        return (session != null ? session.getId() : null);
    }

    
    public static Object getSessionAttribute(HttpServletRequest request, String name) {
        Assert.notNull(request, "Request must not be null");
        HttpSession session = request.getSession(false);
        return (session != null ? session.getAttribute(name) : null);
    }

    
    public static Object getRequiredSessionAttribute(HttpServletRequest request, String name)
        throws IllegalStateException {

        Object attr = getSessionAttribute(request, name);
        if (attr == null) {
            throw new IllegalStateException("No session attribute '" + name + "' found");
        }
        return attr;
    }

    
    public static void setSessionAttribute(HttpServletRequest request, String name, Object value) {
        Assert.notNull(request, "Request must not be null");
        if (value != null) {
            request.getSession().setAttribute(name, value);
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(name);
            }
        }
    }

    
    public static Object getOrCreateSessionAttribute(HttpSession session, String name, Class clazz)
        throws IllegalArgumentException {

        Assert.notNull(session, "Session must not be null");
        Object sessionObject = session.getAttribute(name);
        if (sessionObject == null) {
            try {
                sessionObject = clazz.newInstance();
            } catch (InstantiationException ex) {
                throw new IllegalArgumentException(
                    "Could not instantiate class [" + clazz.getName() +
                        "] for session attribute '" + name + "': " + ex.getMessage());
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException(
                    "Could not access default constructor of class [" + clazz.getName() +
                        "] for session attribute '" + name + "': " + ex.getMessage());
            }
            session.setAttribute(name, sessionObject);
        }
        return sessionObject;
    }

    
    public static Object getSessionMutex(HttpSession session) {
        Assert.notNull(session, "Session must not be null");
        Object mutex = session.getAttribute(SESSION_MUTEX_ATTRIBUTE);
        if (mutex == null) {
            mutex = session;
        }
        return mutex;
    }

    
    @SuppressWarnings("unchecked")
    public static <T> T getNativeRequest(ServletRequest request, Class<T> requiredType) {
        if (requiredType != null) {
            if (requiredType.isInstance(request)) {
                return (T) request;
            } else if (request instanceof ServletRequestWrapper) {
                return getNativeRequest(((ServletRequestWrapper) request).getRequest(), requiredType);
            }
        }
        return null;
    }

    
    @SuppressWarnings("unchecked")
    public static <T> T getNativeResponse(ServletResponse response, Class<T> requiredType) {
        if (requiredType != null) {
            if (requiredType.isInstance(response)) {
                return (T) response;
            } else if (response instanceof ServletResponseWrapper) {
                return getNativeResponse(((ServletResponseWrapper) response).getResponse(), requiredType);
            }
        }
        return null;
    }

    
    public static boolean isIncludeRequest(ServletRequest request) {
        return (request.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE) != null);
    }

    
    public static void exposeForwardRequestAttributes(HttpServletRequest request) {
        exposeRequestAttributeIfNotPresent(request, FORWARD_REQUEST_URI_ATTRIBUTE, request.getRequestURI());
        exposeRequestAttributeIfNotPresent(request, FORWARD_CONTEXT_PATH_ATTRIBUTE, request.getContextPath());
        exposeRequestAttributeIfNotPresent(request, FORWARD_SERVLET_PATH_ATTRIBUTE, request.getServletPath());
        exposeRequestAttributeIfNotPresent(request, FORWARD_PATH_INFO_ATTRIBUTE, request.getPathInfo());
        exposeRequestAttributeIfNotPresent(request, FORWARD_QUERY_STRING_ATTRIBUTE, request.getQueryString());
    }

    
    public static void exposeErrorRequestAttributes(HttpServletRequest request, Throwable ex, String servletName) {
        exposeRequestAttributeIfNotPresent(request, ERROR_STATUS_CODE_ATTRIBUTE, HttpServletResponse.SC_OK);
        exposeRequestAttributeIfNotPresent(request, ERROR_EXCEPTION_TYPE_ATTRIBUTE, ex.getClass());
        exposeRequestAttributeIfNotPresent(request, ERROR_MESSAGE_ATTRIBUTE, ex.getMessage());
        exposeRequestAttributeIfNotPresent(request, ERROR_EXCEPTION_ATTRIBUTE, ex);
        exposeRequestAttributeIfNotPresent(request, ERROR_REQUEST_URI_ATTRIBUTE, request.getRequestURI());
        exposeRequestAttributeIfNotPresent(request, ERROR_SERVLET_NAME_ATTRIBUTE, servletName);
    }

    
    private static void exposeRequestAttributeIfNotPresent(ServletRequest request, String name, Object value) {
        if (request.getAttribute(name) == null) {
            request.setAttribute(name, value);
        }
    }

    
    public static void clearErrorRequestAttributes(HttpServletRequest request) {
        request.removeAttribute(ERROR_STATUS_CODE_ATTRIBUTE);
        request.removeAttribute(ERROR_EXCEPTION_TYPE_ATTRIBUTE);
        request.removeAttribute(ERROR_MESSAGE_ATTRIBUTE);
        request.removeAttribute(ERROR_EXCEPTION_ATTRIBUTE);
        request.removeAttribute(ERROR_REQUEST_URI_ATTRIBUTE);
        request.removeAttribute(ERROR_SERVLET_NAME_ATTRIBUTE);
    }

    
    public static void exposeRequestAttributes(ServletRequest request, Map<String, ?> attributes) {
        Assert.notNull(request, "Request must not be null");
        Assert.notNull(attributes, "Attributes Map must not be null");
        for (Map.Entry<String, ?> entry : attributes.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Assert.notNull(request, "Request must not be null");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    
    public static boolean hasSubmitParameter(ServletRequest request, String name) {
        Assert.notNull(request, "Request must not be null");
        if (request.getParameter(name) != null) {
            return true;
        }
        for (String suffix : SUBMIT_IMAGE_SUFFIXES) {
            if (request.getParameter(name + suffix) != null) {
                return true;
            }
        }
        return false;
    }

    
    @SuppressWarnings("unchecked")
    public static String findParameterValue(ServletRequest request, String name) {
        return findParameterValue(request.getParameterMap(), name);
    }

    
    public static String findParameterValue(Map<String, ?> parameters, String name) {
        // First try to get it as a normal name=value parameter
        Object value = parameters.get(name);
        if (value instanceof String[]) {
            String[] values = (String[]) value;
            return (values.length > 0 ? values[0] : null);
        } else if (value != null) {
            return value.toString();
        }
        // If no value yet, try to get it as a name_value=xyz parameter
        String prefix = name + "_";
        for (String paramName : parameters.keySet()) {
            if (paramName.startsWith(prefix)) {
                // Support images buttons, which would submit parameters as name_value.x=123
                for (String suffix : SUBMIT_IMAGE_SUFFIXES) {
                    if (paramName.endsWith(suffix)) {
                        return paramName.substring(prefix.length(), paramName.length() - suffix.length());
                    }
                }
                return paramName.substring(prefix.length());
            }
        }
        // We couldn't find the parameter value...
        return null;
    }

    
    public static Map<String, Object> getParametersStartingWith(ServletRequest request, String prefix) {
        Assert.notNull(request, "Request must not be null");
        Enumeration paramNames = request.getParameterNames();
        Map<String, Object> params =  new TreeMap<>();
        if (prefix == null) {
            prefix = "";
        }
        while (paramNames != null && paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            if ("".equals(prefix) || paramName.startsWith(prefix)) {
                String unprefixed = paramName.substring(prefix.length());
                String[] values = request.getParameterValues(paramName);
                if (values == null || values.length == 0) {
                    // Do nothing, no values found at all.
                } else if (values.length > 1) {
                    params.put(unprefixed, values);
                } else {
                    params.put(unprefixed, values[0]);
                }
            }
        }
        return params;
    }

    
    public static int getTargetPage(ServletRequest request, String paramPrefix, int currentPage) {
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            if (paramName.startsWith(paramPrefix)) {
                for (int i = 0; i < WebUtils.SUBMIT_IMAGE_SUFFIXES.length; i++) {
                    String suffix = WebUtils.SUBMIT_IMAGE_SUFFIXES[i];
                    if (paramName.endsWith(suffix)) {
                        paramName = paramName.substring(0, paramName.length() - suffix.length());
                    }
                }
                return Integer.parseInt(paramName.substring(paramPrefix.length()));
            }
        }
        return currentPage;
    }

    
    public static String extractFilenameFromUrlPath(String urlPath) {
        String filename = extractFullFilenameFromUrlPath(urlPath);
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex != -1) {
            filename = filename.substring(0, dotIndex);
        }
        return filename;
    }

    
    public static String extractFullFilenameFromUrlPath(String urlPath) {
        int end = urlPath.indexOf(';');
        if (end == -1) {
            end = urlPath.indexOf('?');
            if (end == -1) {
                end = urlPath.length();
            }
        }
        int begin = urlPath.lastIndexOf('/', end) + 1;
        return urlPath.substring(begin, end);
    }

}
