package com.example.simple.spring.v2.web.util;

import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public abstract class UriUtils {

    public static String decode(String source, String encoding) throws UnsupportedEncodingException {
        Assert.notNull(source, "'source' must not be null");
        Assert.hasLength(encoding, "'encoding' must not be empty");
        int length = source.length();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            int ch = source.charAt(i);
            if (ch == '%') {
                if ((i + 2) < length) {
                    char hex1 = source.charAt(i + 1);
                    char hex2 = source.charAt(i + 2);
                    int u = Character.digit(hex1, 16);
                    int l = Character.digit(hex2, 16);
                    if (u == -1 || l == -1) {
                        throw new IllegalArgumentException("Invalid encoded sequence \"" + source.substring(i) + "\"");
                    }
                    bos.write((char) ((u << 4) + l));
                    i += 2;
                    changed = true;
                } else {
                    throw new IllegalArgumentException("Invalid encoded sequence \"" + source.substring(i) + "\"");
                }
            } else {
                bos.write(ch);
            }
        }
        return changed ? bos.toString(encoding) : source;
    }

}
