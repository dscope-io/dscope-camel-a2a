package io.dscope.camel.a2a.service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON codec for serializing and deserializing A2A payloads.
 */
public class A2AJsonCodec {

    private final ObjectMapper mapper = new ObjectMapper();

    public String serialize(Object o) throws Exception {
        return mapper.writeValueAsString(o);
    }

    public <T> T deserialize(String s, Class<T> c) throws Exception {
        return mapper.readValue(s, c);
    }
}
