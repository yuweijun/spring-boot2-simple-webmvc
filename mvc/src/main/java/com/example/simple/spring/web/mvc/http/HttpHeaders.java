package com.example.simple.spring.web.mvc.http;

import org.springframework.util.Assert;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class HttpHeaders implements MultiValueMap<String, String> {

    private static final String ACCEPT = "Accept";

    private static final String ACCEPT_CHARSET = "Accept-Charset";

    private static final String ALLOW = "Allow";

    private static final String CACHE_CONTROL = "Cache-Control";

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    private static final String CONTENT_LENGTH = "Content-Length";

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String DATE = "Date";

    private static final String ETAG = "ETag";

    private static final String EXPIRES = "Expires";

    private static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    private static final String IF_NONE_MATCH = "If-None-Match";

    private static final String LAST_MODIFIED = "Last-Modified";

    private static final String LOCATION = "Location";

    private static final String PRAGMA = "Pragma";

    private static final String[] DATE_FORMATS = new String[]{
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "EEE, dd-MMM-yy HH:mm:ss zzz",
        "EEE MMM dd HH:mm:ss yyyy"
    };

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private final Map<String, List<String>> headers;

    private HttpHeaders(Map<String, List<String>> headers, boolean readOnly) {
        Assert.notNull(headers, "'headers' must not be null");
        if (readOnly) {
            Map<String, List<String>> map =
                new LinkedCaseInsensitiveMap<>(headers.size(), Locale.ENGLISH);
            for (Entry<String, List<String>> entry : headers.entrySet()) {
                List<String> values = Collections.unmodifiableList(entry.getValue());
                map.put(entry.getKey(), values);
            }
            this.headers = Collections.unmodifiableMap(map);
        } else {
            this.headers = headers;
        }
    }

    public HttpHeaders() {
        this(new LinkedCaseInsensitiveMap<>(8, Locale.ENGLISH), false);
    }

    public static HttpHeaders readOnlyHttpHeaders(HttpHeaders headers) {
        return new HttpHeaders(headers, true);
    }

    public List<MediaType> getAccept() {
        String value = getFirst(ACCEPT);
        return (value != null ? MediaType.parseMediaTypes(value) : Collections.emptyList());
    }

    public void setAccept(List<MediaType> acceptableMediaTypes) {
        set(ACCEPT, MediaType.toString(acceptableMediaTypes));
    }

    public List<Charset> getAcceptCharset() {
        List<Charset> result = new ArrayList<>();
        String value = getFirst(ACCEPT_CHARSET);
        if (value != null) {
            String[] tokens = value.split(",\\s*");
            for (String token : tokens) {
                int paramIdx = token.indexOf(';');
                String charsetName;
                if (paramIdx == -1) {
                    charsetName = token;
                } else {
                    charsetName = token.substring(0, paramIdx);
                }
                if (!charsetName.equals("*")) {
                    result.add(Charset.forName(charsetName));
                }
            }
        }
        return result;
    }

    public void setAcceptCharset(List<Charset> acceptableCharsets) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<Charset> iterator = acceptableCharsets.iterator(); iterator.hasNext(); ) {
            Charset charset = iterator.next();
            builder.append(charset.name().toLowerCase(Locale.ENGLISH));
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        set(ACCEPT_CHARSET, builder.toString());
    }

    public Set<HttpMethod> getAllow() {
        String value = getFirst(ALLOW);
        if (value != null) {
            List<HttpMethod> allowedMethod = new ArrayList<>(5);
            String[] tokens = value.split(",\\s*");
            for (String token : tokens) {
                allowedMethod.add(HttpMethod.valueOf(token));
            }
            return EnumSet.copyOf(allowedMethod);
        } else {
            return EnumSet.noneOf(HttpMethod.class);
        }
    }

    public void setAllow(Set<HttpMethod> allowedMethods) {
        set(ALLOW, StringUtils.collectionToCommaDelimitedString(allowedMethods));
    }

    public String getCacheControl() {
        return getFirst(CACHE_CONTROL);
    }

    public void setCacheControl(String cacheControl) {
        set(CACHE_CONTROL, cacheControl);
    }

    public void setContentDispositionFormData(String name, String filename) {
        Assert.notNull(name, "'name' must not be null");
        StringBuilder builder = new StringBuilder("form-data; name=\"");
        builder.append(name).append('\"');
        if (filename != null) {
            builder.append("; filename=\"");
            builder.append(filename).append('\"');
        }
        set(CONTENT_DISPOSITION, builder.toString());
    }

    public long getContentLength() {
        String value = getFirst(CONTENT_LENGTH);
        return (value != null ? Long.parseLong(value) : -1);
    }

    public void setContentLength(long contentLength) {
        set(CONTENT_LENGTH, Long.toString(contentLength));
    }

    public MediaType getContentType() {
        String value = getFirst(CONTENT_TYPE);
        return (value != null ? MediaType.parseMediaType(value) : null);
    }

    public void setContentType(MediaType mediaType) {
        Assert.isTrue(!mediaType.isWildcardType(), "'Content-Type' cannot contain wildcard type '*'");
        Assert.isTrue(!mediaType.isWildcardSubtype(), "'Content-Type' cannot contain wildcard subtype '*'");
        set(CONTENT_TYPE, mediaType.toString());
    }

    public long getDate() {
        return getFirstDate(DATE);
    }

    public void setDate(long date) {
        setDate(DATE, date);
    }

    public String getETag() {
        return getFirst(ETAG);
    }

    public void setETag(String eTag) {
        if (eTag != null) {
            Assert.isTrue(eTag.startsWith("\"") || eTag.startsWith("W/"), "Invalid eTag, does not start with W/ or \"");
            Assert.isTrue(eTag.endsWith("\""), "Invalid eTag, does not end with \"");
        }
        set(ETAG, eTag);
    }

    public long getExpires() {
        return getFirstDate(EXPIRES);
    }

    public void setExpires(long expires) {
        setDate(EXPIRES, expires);
    }

    public void setIfModifiedSince(long ifModifiedSince) {
        setDate(IF_MODIFIED_SINCE, ifModifiedSince);
    }

    public long getIfNotModifiedSince() {
        return getFirstDate(IF_MODIFIED_SINCE);
    }

    public List<String> getIfNoneMatch() {
        List<String> result = new ArrayList<>();

        String value = getFirst(IF_NONE_MATCH);
        if (value != null) {
            String[] tokens = value.split(",\\s*");
            for (String token : tokens) {
                result.add(token);
            }
        }
        return result;
    }

    public void setIfNoneMatch(String ifNoneMatch) {
        set(IF_NONE_MATCH, ifNoneMatch);
    }

    public void setIfNoneMatch(List<String> ifNoneMatchList) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<String> iterator = ifNoneMatchList.iterator(); iterator.hasNext(); ) {
            String ifNoneMatch = iterator.next();
            builder.append(ifNoneMatch);
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        set(IF_NONE_MATCH, builder.toString());
    }

    public long getLastModified() {
        return getFirstDate(LAST_MODIFIED);
    }

    public void setLastModified(long lastModified) {
        setDate(LAST_MODIFIED, lastModified);
    }

    public URI getLocation() {
        String value = getFirst(LOCATION);
        return (value != null ? URI.create(value) : null);
    }

    public void setLocation(URI location) {
        set(LOCATION, location.toASCIIString());
    }

    public String getPragma() {
        return getFirst(PRAGMA);
    }

    public void setPragma(String pragma) {
        set(PRAGMA, pragma);
    }

    // Utility methods

    private long getFirstDate(String headerName) {
        String headerValue = getFirst(headerName);
        if (headerValue == null) {
            return -1;
        }
        for (String dateFormat : DATE_FORMATS) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
            simpleDateFormat.setTimeZone(GMT);
            try {
                return simpleDateFormat.parse(headerValue).getTime();
            } catch (ParseException e) {
                // ignore
            }
        }
        throw new IllegalArgumentException("Cannot parse date value \"" + headerValue +
            "\" for \"" + headerName + "\" header");
    }

    private void setDate(String headerName, long date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMATS[0], Locale.US);
        dateFormat.setTimeZone(GMT);
        set(headerName, dateFormat.format(new Date(date)));
    }

    // Single string methods

    public String getFirst(String headerName) {
        List<String> headerValues = headers.get(headerName);
        return headerValues != null ? headerValues.get(0) : null;
    }

    public void add(String headerName, String headerValue) {
        List<String> headerValues = headers.get(headerName);
        if (headerValues == null) {
            headerValues = new LinkedList<>();
            this.headers.put(headerName, headerValues);
        }
        headerValues.add(headerValue);
    }

    @Override
    public void addAll(String key, List<? extends String> values) {

    }

    @Override
    public void addAll(MultiValueMap<String, String> values) {

    }

    public void set(String headerName, String headerValue) {
        List<String> headerValues = new LinkedList<>();
        headerValues.add(headerValue);
        headers.put(headerName, headerValues);
    }

    public void setAll(Map<String, String> values) {
        for (Entry<String, String> entry : values.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, String> toSingleValueMap() {
        LinkedHashMap<String, String> singleValueMap = new LinkedHashMap<>(this.headers.size());
        for (Entry<String, List<String>> entry : headers.entrySet()) {
            singleValueMap.put(entry.getKey(), entry.getValue().get(0));
        }
        return singleValueMap;
    }

    // Map implementation

    public int size() {
        return this.headers.size();
    }

    public boolean isEmpty() {
        return this.headers.isEmpty();
    }

    public boolean containsKey(Object key) {
        return this.headers.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.headers.containsValue(value);
    }

    public List<String> get(Object key) {
        return this.headers.get(key);
    }

    public List<String> put(String key, List<String> value) {
        return this.headers.put(key, value);
    }

    public List<String> remove(Object key) {
        return this.headers.remove(key);
    }

    public void putAll(Map<? extends String, ? extends List<String>> m) {
        this.headers.putAll(m);
    }

    public void clear() {
        this.headers.clear();
    }

    public Set<String> keySet() {
        return this.headers.keySet();
    }

    public Collection<List<String>> values() {
        return this.headers.values();
    }

    public Set<Entry<String, List<String>>> entrySet() {
        return this.headers.entrySet();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HttpHeaders)) {
            return false;
        }
        HttpHeaders otherHeaders = (HttpHeaders) other;
        return this.headers.equals(otherHeaders.headers);
    }

    @Override
    public int hashCode() {
        return this.headers.hashCode();
    }

    @Override
    public String toString() {
        return this.headers.toString();
    }

}
