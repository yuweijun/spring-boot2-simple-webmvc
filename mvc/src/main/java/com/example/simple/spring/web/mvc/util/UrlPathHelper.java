package com.example.simple.spring.web.mvc.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;

public class UrlPathHelper {

    private static final String WEBSPHERE_URI_ATTRIBUTE = "com.ibm.websphere.servlet.uri_non_decoded";

    private static final Log logger = LogFactory.getLog(UrlPathHelper.class);

    static volatile Boolean websphereComplianceFlag;

    private boolean alwaysUseFullPath = false;

    private boolean urlDecode = true;

    private String defaultEncoding = WebUtils.DEFAULT_CHARACTER_ENCODING;

    public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
        this.alwaysUseFullPath = alwaysUseFullPath;
    }

    public void setUrlDecode(boolean urlDecode) {
        this.urlDecode = urlDecode;
    }

    protected String getDefaultEncoding() {
        return this.defaultEncoding;
    }

    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    public String getLookupPathForRequest(HttpServletRequest request) {
        // Always use full path within current servlet context?
        if (this.alwaysUseFullPath) {
            return getPathWithinApplication(request);
        }
        // Else, use path within current servlet mapping if applicable
        String rest = getPathWithinServletMapping(request);
        if (!"".equals(rest)) {
            return rest;
        } else {
            return getPathWithinApplication(request);
        }
    }

    public String getPathWithinServletMapping(HttpServletRequest request) {
        String pathWithinApp = getPathWithinApplication(request);
        String servletPath = getServletPath(request);
        if (pathWithinApp.startsWith(servletPath)) {
            // Normal case: URI contains servlet path.
            return pathWithinApp.substring(servletPath.length());
        } else {
            // Special case: URI is different from servlet path.
            // Can happen e.g. with index page: URI="/", servletPath="/index.html"
            // Use path info if available, as it indicates an index page within
            // a servlet mapping. Otherwise, use the full servlet path.
            String pathInfo = request.getPathInfo();
            return (pathInfo != null ? pathInfo : servletPath);
        }
    }

    public String getPathWithinApplication(HttpServletRequest request) {
        String contextPath = getContextPath(request);
        String requestUri = getRequestUri(request);
        if (StringUtils.startsWithIgnoreCase(requestUri, contextPath)) {
            // Normal case: URI contains context path.
            String path = requestUri.substring(contextPath.length());
            return (StringUtils.hasText(path) ? path : "/");
        } else {
            // Special case: rather unusual.
            return requestUri;
        }
    }

    public String getRequestUri(HttpServletRequest request) {
        String uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
        if (uri == null) {
            uri = request.getRequestURI();
        }
        return decodeAndCleanUriString(request, uri);
    }

    public String getContextPath(HttpServletRequest request) {
        String contextPath = (String) request.getAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE);
        if (contextPath == null) {
            contextPath = request.getContextPath();
        }
        if ("/".equals(contextPath)) {
            // Invalid case, but happens for includes on Jetty: silently adapt it.
            contextPath = "";
        }
        return decodeRequestString(request, contextPath);
    }

    public String getServletPath(HttpServletRequest request) {
        String servletPath = (String) request.getAttribute(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE);
        if (servletPath == null) {
            servletPath = request.getServletPath();
        }
        if (servletPath.length() > 1 && servletPath.endsWith("/") &&
            shouldRemoveTrailingServletPathSlash(request)) {
            // On WebSphere, in non-compliant mode, for a "/foo/" case that would be "/foo"
            // on all other servlet containers: removing trailing slash, proceeding with
            // that remaining slash as final lookup path...
            servletPath = servletPath.substring(0, servletPath.length() - 1);
        }
        return servletPath;
    }

    public String getOriginatingRequestUri(HttpServletRequest request) {
        String uri = (String) request.getAttribute(WEBSPHERE_URI_ATTRIBUTE);
        if (uri == null) {
            uri = (String) request.getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE);
            if (uri == null) {
                uri = request.getRequestURI();
            }
        }
        return decodeAndCleanUriString(request, uri);
    }

    public String getOriginatingContextPath(HttpServletRequest request) {
        String contextPath = (String) request.getAttribute(WebUtils.FORWARD_CONTEXT_PATH_ATTRIBUTE);
        if (contextPath == null) {
            contextPath = request.getContextPath();
        }
        return decodeRequestString(request, contextPath);
    }

    /**
     * Return the servlet path for the given request, detecting an include request URL if called within a RequestDispatcher include.
     *
     * @param request current HTTP request
     * @return the servlet path
     */
    public String getOriginatingServletPath(HttpServletRequest request) {
        String servletPath = (String) request.getAttribute(WebUtils.FORWARD_SERVLET_PATH_ATTRIBUTE);
        if (servletPath == null) {
            servletPath = request.getServletPath();
        }
        return servletPath;
    }

    /**
     * Return the query string part of the given request's URL. If this is a forwarded request, correctly resolves to the query string of the original request.
     *
     * @param request current HTTP request
     * @return the query string
     */
    public String getOriginatingQueryString(HttpServletRequest request) {
        if ((request.getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE) != null) ||
            (request.getAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE) != null)) {
            return (String) request.getAttribute(WebUtils.FORWARD_QUERY_STRING_ATTRIBUTE);
        } else {
            return request.getQueryString();
        }
    }

    private String decodeAndCleanUriString(HttpServletRequest request, String uri) {
        uri = decodeRequestString(request, uri);
        int semicolonIndex = uri.indexOf(';');
        return (semicolonIndex != -1 ? uri.substring(0, semicolonIndex) : uri);
    }

    /**
     * Decode the given source string with a URLDecoder. The encoding will be taken from the request, falling back to the default "ISO-8859-1".
     * <p>The default implementation uses <code>URLDecoder.decode(input, enc)</code>.
     *
     * @param request current HTTP request
     * @param source  the String to decode
     * @return the decoded String
     * @see WebUtils#DEFAULT_CHARACTER_ENCODING
     * @see javax.servlet.ServletRequest#getCharacterEncoding
     * @see URLDecoder#decode(String, String)
     * @see URLDecoder#decode(String)
     */
    public String decodeRequestString(HttpServletRequest request, String source) {
        if (this.urlDecode) {
            String enc = determineEncoding(request);
            try {
                return UriUtils.decode(source, enc);
            } catch (UnsupportedEncodingException ex) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Could not decode request string [" + source + "] with encoding '" + enc +
                        "': falling back to platform default encoding; exception message: " + ex.getMessage());
                }
                return URLDecoder.decode(source);
            }
        }
        return source;
    }

    protected String determineEncoding(HttpServletRequest request) {
        String enc = request.getCharacterEncoding();
        if (enc == null) {
            enc = getDefaultEncoding();
        }
        return enc;
    }

    private boolean shouldRemoveTrailingServletPathSlash(HttpServletRequest request) {
        if (request.getAttribute(WEBSPHERE_URI_ATTRIBUTE) == null) {
            // Regular servlet container: behaves as expected in any case,
            // so the trailing slash is the result of a "/" url-pattern mapping.
            // Don't remove that slash.
            return false;
        }
        if (websphereComplianceFlag == null) {
            ClassLoader classLoader = UrlPathHelper.class.getClassLoader();
            String className = "com.ibm.ws.webcontainer.WebContainer";
            String methodName = "getWebContainerProperties";
            String propName = "com.ibm.ws.webcontainer.removetrailingservletpathslash";
            boolean flag = false;
            try {
                Class<?> cl = classLoader.loadClass(className);
                Properties prop = (Properties) cl.getMethod(methodName).invoke(null);
                flag = Boolean.parseBoolean(prop.getProperty(propName));
            } catch (Throwable ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not introspect WebSphere web container properties: " + ex);
                }
            }
            websphereComplianceFlag = flag;
        }
        // Don't bother if WebSphere is configured to be fully Servlet compliant.
        // However, if it is not compliant, do remove the improper trailing slash!
        return !websphereComplianceFlag;
    }

}
