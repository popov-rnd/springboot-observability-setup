package com.popovrnd.springbootobservabilitysetup.service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CodecImpl implements Codec {

    public static final Map<String, String> SHORT_TO_LONG_STORAGE = new ConcurrentHashMap<>();

    public static final Map<String, String> LONG_TO_SHORT_STORAGE = new ConcurrentHashMap<>();

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final char[] BASE62_CHARS = BASE62.toCharArray();

    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private Tracer tracer;

    //@Observed(name = "shortener.encode.lookup", contextualName = "Encode method")
    //@Timed(value = "shortener.encode.execution", description = "Time taken by myMethod")
    @Override
    public String encode(String longUrl) {

        Span span = tracer.spanBuilder("encode").startSpan();
        try (Scope scope = span.makeCurrent()) {
            if (LONG_TO_SHORT_STORAGE.containsKey(longUrl)) {
                return LONG_TO_SHORT_STORAGE.get(longUrl);
            }

            int baseLength = 6;
            int missesCounter = 0;
            String randomShort = null;

            do {
                randomShort = generate(baseLength);
                missesCounter++;
                // For many collisions, increase number of letters;
                if (missesCounter > 10) {
                    baseLength++;
                    missesCounter = 0;
                }
            } while (SHORT_TO_LONG_STORAGE.containsKey(randomShort));

            SHORT_TO_LONG_STORAGE.put(randomShort, longUrl);
            LONG_TO_SHORT_STORAGE.put(longUrl, randomShort);

            return randomShort;
        } finally {
            span.end();
        }
    }

    private String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(BASE62_CHARS.length);
            sb.append(BASE62_CHARS[index]);
        }
        return sb.toString();
    }
}
