package com.examsystem.network;

import com.examsystem.network.message.MessageType;
import com.examsystem.network.message.NetworkMessage;
import com.google.gson.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Network message integrity tests per context/TESTING_GUIDE.md
 */
public class NetworkMessageTest {

    @Test
    public void testMessageRoundTrip() {
        NetworkMessage original = new NetworkMessage(MessageType.LOGIN);
        original.setRequestId("req-1");
        JsonObject payload = new JsonObject();
        payload.addProperty("username", "student1");
        payload.addProperty("password", "student123");
        original.setPayload(payload);

        String json = original.toJson();
        NetworkMessage parsed = NetworkMessage.fromJson(json);

        assertEquals(MessageType.LOGIN, parsed.getType());
        assertEquals("req-1", parsed.getRequestId());
        assertEquals("student1", parsed.getPayload().get("username").getAsString());
    }

    @Test
    public void testPingPong() {
        NetworkMessage ping = NetworkMessage.ping();
        ping.setRequestId("ping-1");
        NetworkMessage pong = NetworkMessage.pong("ping-1");

        assertEquals(MessageType.PING, ping.getType());
        assertEquals(MessageType.PONG, pong.getType());
        assertEquals("ping-1", pong.getRequestId());
        assertTrue(pong.isSuccess());
    }
}
