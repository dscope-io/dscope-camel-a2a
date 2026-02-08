package io.dscope.camel.a2a;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON codec for serializing and deserializing A2A messages.
 * Uses Jackson ObjectMapper for JSON processing.
 */
public class A2AJsonCodec {

    /** Jackson ObjectMapper for JSON operations */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Serializes an object to JSON string.
     *
     * @param o the object to serialize
     * @return JSON string representation
     * @throws Exception if serialization fails
     */
    public String serialize(Object o) throws Exception {
        return mapper.writeValueAsString(o);
    }

    /**
     * Deserializes a JSON string to an object of the specified type.
     *
     * @param <T> the type of the object
     * @param s the JSON string
     * @param c the class of the target type
     * @return the deserialized object
     * @throws Exception if deserialization fails
     */
    public <T> T deserialize(String s, Class<T> c) throws Exception {
        return mapper.readValue(s, c);
    }
}
