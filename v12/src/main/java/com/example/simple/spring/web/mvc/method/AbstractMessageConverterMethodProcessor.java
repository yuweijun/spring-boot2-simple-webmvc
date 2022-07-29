package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.http.HttpInputMessage;
import com.example.simple.spring.web.mvc.http.HttpMediaTypeNotAcceptableException;
import com.example.simple.spring.web.mvc.http.MediaType;
import com.example.simple.spring.web.mvc.http.converter.HttpMessageConverter;
import com.example.simple.spring.web.mvc.http.server.ServletServerHttpRequest;
import com.example.simple.spring.web.mvc.http.server.ServletServerHttpResponse;
import com.example.simple.spring.web.mvc.servlet.HandlerMapping;
import org.springframework.core.MethodParameter;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMessageConverterMethodProcessor extends AbstractMessageConverterMethodArgumentResolver implements HandlerMethodReturnValueHandler {

    private static final MediaType MEDIA_TYPE_APPLICATION = new MediaType("application");

    protected AbstractMessageConverterMethodProcessor(List<HttpMessageConverter<?>> messageConverters) {
        super(messageConverters);
    }

    protected ServletServerHttpResponse createOutputMessage(HttpServletResponse response) {
        return new ServletServerHttpResponse(response);
    }

    protected <T> void writeWithMessageConverters(T returnValue, MethodParameter returnType, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletServerHttpRequest inputMessage = createInputMessage(request);
        ServletServerHttpResponse outputMessage = createOutputMessage(response);
        writeWithMessageConverters(returnValue, returnType, inputMessage, outputMessage);
    }

    protected <T> void writeWithMessageConverters(T returnValue, MethodParameter returnType, ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage)
        throws IOException, HttpMediaTypeNotAcceptableException {

        Class<?> returnValueClass = returnValue.getClass();

        List<MediaType> acceptableMediaTypes = getAcceptableMediaTypes(inputMessage);
        List<MediaType> producibleMediaTypes = getProducibleMediaTypes(inputMessage.getServletRequest(), returnValueClass);

        Set<MediaType> compatibleMediaTypes = new LinkedHashSet<>();
        for (MediaType a : acceptableMediaTypes) {
            for (MediaType p : producibleMediaTypes) {
                if (a.isCompatibleWith(p)) {
                    compatibleMediaTypes.add(getMostSpecificMediaType(a, p));
                }
            }
        }
        if (compatibleMediaTypes.isEmpty()) {
            throw new HttpMediaTypeNotAcceptableException(allSupportedMediaTypes);
        }

        List<MediaType> mediaTypes = new ArrayList<>(compatibleMediaTypes);
        MediaType.sortBySpecificity(mediaTypes);

        MediaType selectedMediaType = null;
        for (MediaType mediaType : mediaTypes) {
            if (mediaType.isConcrete()) {
                selectedMediaType = mediaType;
                break;
            } else if (mediaType.equals(MediaType.ALL) || mediaType.equals(MEDIA_TYPE_APPLICATION)) {
                selectedMediaType = MediaType.APPLICATION_OCTET_STREAM;
                break;
            }
        }

        if (selectedMediaType != null) {
            for (HttpMessageConverter<?> messageConverter : messageConverters) {
                if (messageConverter.canWrite(returnValueClass, selectedMediaType)) {
                    ((HttpMessageConverter<T>) messageConverter).write(returnValue, selectedMediaType, outputMessage);
                    logger.debug("Written [" + returnValue + "] as \"" + selectedMediaType + "\" using [" + messageConverter + "]");
                    return;
                }
            }
        }
        throw new HttpMediaTypeNotAcceptableException(allSupportedMediaTypes);
    }

    @SuppressWarnings("unchecked")
    protected List<MediaType> getProducibleMediaTypes(HttpServletRequest request, Class<?> returnValueClass) {
        Set<MediaType> mediaTypes = (Set<MediaType>) request.getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
        if (!CollectionUtils.isEmpty(mediaTypes)) {
            return new ArrayList<>(mediaTypes);
        } else if (!allSupportedMediaTypes.isEmpty()) {
            List<MediaType> result = new ArrayList<>();
            for (HttpMessageConverter<?> converter : messageConverters) {
                if (converter.canWrite(returnValueClass, null)) {
                    result.addAll(converter.getSupportedMediaTypes());
                }
            }
            return result;
        } else {
            return Collections.singletonList(MediaType.ALL);
        }
    }

    private List<MediaType> getAcceptableMediaTypes(HttpInputMessage inputMessage) {
        List<MediaType> result = inputMessage.getHeaders().getAccept();
        return result.isEmpty() ? Collections.singletonList(MediaType.ALL) : result;
    }

    private MediaType getMostSpecificMediaType(MediaType type1, MediaType type2) {
        double quality = type1.getQualityValue();
        Map<String, String> params = Collections.singletonMap("q", String.valueOf(quality));
        MediaType t1 = new MediaType(type1, params);
        MediaType t2 = new MediaType(type2, params);
        return MediaType.SPECIFICITY_COMPARATOR.compare(t1, t2) <= 0 ? type1 : type2;
    }

}
