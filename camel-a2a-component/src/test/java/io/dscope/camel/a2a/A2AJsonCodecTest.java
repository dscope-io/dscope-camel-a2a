package io.dscope.camel.a2a;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class A2AJsonCodecTest {

    private record Sample(String value, int count) {}

    @Test
    void serializeAndDeserializeRoundTrip() throws Exception {
        A2AJsonCodec codec = new A2AJsonCodec();
        Sample sample = new Sample("test", 42);

        String json = codec.serialize(sample);
        assertTrue(json.contains("\"value\":\"test\""));

        Sample restored = codec.deserialize(json, Sample.class);
        assertEquals(sample, restored);
    }
}
