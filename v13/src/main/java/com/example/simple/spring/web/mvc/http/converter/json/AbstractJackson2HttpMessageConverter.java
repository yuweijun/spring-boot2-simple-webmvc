package com.example.simple.spring.web.mvc.http.converter.json;

import com.example.simple.spring.web.mvc.http.HttpInputMessage;
import com.example.simple.spring.web.mvc.http.HttpOutputMessage;
import com.example.simple.spring.web.mvc.http.MediaType;
import com.example.simple.spring.web.mvc.http.converter.AbstractGenericHttpMessageConverter;
import com.example.simple.spring.web.mvc.http.converter.HttpMessageException;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;
import org.springframework.util.TypeUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractJackson2HttpMessageConverter extends AbstractGenericHttpMessageConverter<Object> {

    private static final Map<String, JsonEncoding> ENCODINGS;

    static {
        ENCODINGS = new HashMap<>(JsonEncoding.values().length + 1);
        for (JsonEncoding encoding : JsonEncoding.values()) {
            ENCODINGS.put(encoding.getJavaName(), encoding);
        }
        ENCODINGS.put("US-ASCII", JsonEncoding.UTF8);
    }

    protected ObjectMapper objectMapper;

    private Boolean prettyPrint;

    private final PrettyPrinter ssePrettyPrinter;

    protected AbstractJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentObjectsWith(new DefaultIndenter("  ", "\ndata:"));
        this.ssePrettyPrinter = prettyPrinter;
    }

    protected AbstractJackson2HttpMessageConverter(ObjectMapper objectMapper, MediaType supportedMediaType) {
        this(objectMapper);
        setSupportedMediaTypes(Collections.singletonList(supportedMediaType));
    }

    protected AbstractJackson2HttpMessageConverter(ObjectMapper objectMapper, MediaType... supportedMediaTypes) {
        this(objectMapper);
        setSupportedMediaTypes(Arrays.asList(supportedMediaTypes));
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "ObjectMapper must not be null");
        this.objectMapper = objectMapper;
        configurePrettyPrint();
    }

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        configurePrettyPrint();
    }

    private void configurePrettyPrint() {
        if (this.prettyPrint != null) {
            this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, this.prettyPrint);
        }
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return canRead(clazz, null, mediaType);
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        if (!canRead(mediaType)) {
            return false;
        }
        JavaType javaType = getJavaType(type, contextClass);
        AtomicReference<Throwable> causeRef = new AtomicReference<>();
        if (this.objectMapper.canDeserialize(javaType, causeRef)) {
            return true;
        }
        logWarningIfNecessary(javaType, causeRef.get());
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        if (!canWrite(mediaType)) {
            return false;
        }
        if (mediaType != null && mediaType.getCharset() != null) {
            Charset charset = mediaType.getCharset();
            if (!ENCODINGS.containsKey(charset.name())) {
                return false;
            }
        }
        AtomicReference<Throwable> causeRef = new AtomicReference<>();
        if (this.objectMapper.canSerialize(clazz, causeRef)) {
            return true;
        }
        logWarningIfNecessary(clazz, causeRef.get());
        return false;
    }

    protected void logWarningIfNecessary(Type type, Throwable cause) {
        if (cause == null) {
            return;
        }

        // Do not log warning for serializer not found (note: different message wording on Jackson 2.9)
        boolean debugLevel = (cause instanceof JsonMappingException && cause.getMessage().startsWith("Cannot find"));

        if (debugLevel ? logger.isDebugEnabled() : logger.isWarnEnabled()) {
            String msg = "Failed to evaluate Jackson " + (type instanceof JavaType ? "de" : "") +
                "serialization for type [" + type + "]";
            if (debugLevel) {
                logger.debug(msg, cause);
            } else if (logger.isDebugEnabled()) {
                logger.warn(msg, cause);
            } else {
                logger.warn(msg + ": " + cause);
            }
        }
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
        throws IOException, HttpMessageException {

        JavaType javaType = getJavaType(clazz, null);
        return readJavaType(javaType, inputMessage);
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
        throws IOException, HttpMessageException {

        JavaType javaType = getJavaType(type, contextClass);
        return readJavaType(javaType, inputMessage);
    }

    private Object readJavaType(JavaType javaType, HttpInputMessage inputMessage) throws IOException {
        MediaType contentType = inputMessage.getHeaders().getContentType();
        Charset charset = getCharset(contentType);

        boolean isUnicode = ENCODINGS.containsKey(charset.name());
        try {
            if (inputMessage instanceof MappingJacksonInputMessage) {
                Class<?> deserializationView = ((MappingJacksonInputMessage) inputMessage).getDeserializationView();
                if (deserializationView != null) {
                    ObjectReader objectReader = this.objectMapper.readerWithView(deserializationView).forType(javaType);
                    if (isUnicode) {
                        return objectReader.readValue(inputMessage.getBody());
                    } else {
                        Reader reader = new InputStreamReader(inputMessage.getBody(), charset);
                        return objectReader.readValue(reader);
                    }
                }
            }
            if (isUnicode) {
                return this.objectMapper.readValue(inputMessage.getBody(), javaType);
            } else {
                Reader reader = new InputStreamReader(inputMessage.getBody(), charset);
                return this.objectMapper.readValue(reader, javaType);
            }
        } catch (InvalidDefinitionException ex) {
            throw new HttpMessageException("Type definition error: " + ex.getType(), ex);
        } catch (JsonProcessingException ex) {
            throw new HttpMessageException("JSON parse error: " + ex.getOriginalMessage(), ex);
        }
    }

    private static Charset getCharset(MediaType contentType) {
        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        } else {
            return StandardCharsets.UTF_8;
        }
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
        throws IOException, HttpMessageException {

        MediaType contentType = outputMessage.getHeaders().getContentType();
        JsonEncoding encoding = getJsonEncoding(contentType);
        JsonGenerator generator = this.objectMapper.getFactory().createGenerator(outputMessage.getBody(), encoding);
        try {
            writePrefix(generator, object);

            Object value = object;
            Class<?> serializationView = null;
            FilterProvider filters = null;
            JavaType javaType = null;

            if (object instanceof MappingJacksonValue) {
                MappingJacksonValue container = (MappingJacksonValue) object;
                value = container.getValue();
                serializationView = container.getSerializationView();
                filters = container.getFilters();
            }
            if (type != null && TypeUtils.isAssignable(type, value.getClass())) {
                javaType = getJavaType(type, null);
            }

            ObjectWriter objectWriter = (serializationView != null ?
                this.objectMapper.writerWithView(serializationView) : this.objectMapper.writer());
            if (filters != null) {
                objectWriter = objectWriter.with(filters);
            }
            if (javaType != null && javaType.isContainerType()) {
                objectWriter = objectWriter.forType(javaType);
            }
            SerializationConfig config = objectWriter.getConfig();
            if (contentType != null && contentType.isCompatibleWith(MediaType.TEXT_EVENT_STREAM) &&
                config.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
                objectWriter = objectWriter.with(this.ssePrettyPrinter);
            }
            objectWriter.writeValue(generator, value);

            writeSuffix(generator, object);
            generator.flush();
        } catch (InvalidDefinitionException ex) {
            throw new HttpMessageException("Type definition error: " + ex.getType(), ex);
        } catch (JsonProcessingException ex) {
            throw new HttpMessageException("Could not write JSON: " + ex.getOriginalMessage(), ex);
        }
    }

    protected void writePrefix(JsonGenerator generator, Object object) throws IOException {
    }

    protected void writeSuffix(JsonGenerator generator, Object object) throws IOException {
    }

    protected JavaType getJavaType(Type type, Class<?> contextClass) {
        TypeFactory typeFactory = this.objectMapper.getTypeFactory();
        return typeFactory.constructType(GenericTypeResolver.resolveType(type, contextClass));
    }

    protected JsonEncoding getJsonEncoding(MediaType contentType) {
        if (contentType != null && contentType.getCharset() != null) {
            Charset charset = contentType.getCharset();
            JsonEncoding encoding = ENCODINGS.get(charset.name());
            if (encoding != null) {
                return encoding;
            }
        }
        return JsonEncoding.UTF8;
    }

    @Override
    protected MediaType getDefaultContentType(Object object) throws IOException {
        if (object instanceof MappingJacksonValue) {
            object = ((MappingJacksonValue) object).getValue();
        }
        return super.getDefaultContentType(object);
    }

    @Override
    protected Long getContentLength(Object object, MediaType contentType) throws IOException {
        if (object instanceof MappingJacksonValue) {
            object = ((MappingJacksonValue) object).getValue();
        }
        return super.getContentLength(object, contentType);
    }

}
