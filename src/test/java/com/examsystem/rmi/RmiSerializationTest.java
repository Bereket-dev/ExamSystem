package com.examsystem.rmi;

import com.examsystem.rmi.remote.LoginResult;
import com.examsystem.rmi.remote.MonitoringSummary;
import com.examsystem.rmi.remote.RemoteAnswerPayload;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * RMI serialization tests per context/TESTING_GUIDE.md
 */
public class RmiSerializationTest {

    @Test
    public void testLoginResultSerialization() throws Exception {
        LoginResult original = LoginResult.success(1, "student1", "Alice Johnson", "STUDENT");
        LoginResult copy = serializeAndDeserialize(original);

        assertTrue(copy.isSuccess());
        assertEquals(1, copy.getUserId());
        assertEquals("student1", copy.getUsername());
        assertEquals("Alice Johnson", copy.getFullName());
        assertEquals("STUDENT", copy.getRole());
    }

    @Test
    public void testRemoteAnswerPayloadSerialization() throws Exception {
        RemoteAnswerPayload original = new RemoteAnswerPayload();
        original.setAttemptId(10);
        original.setQuestionId(3);
        original.setSelectedOptionId(7);
        original.setCorrect(true);
        original.setMarksObtained(2);

        RemoteAnswerPayload copy = serializeAndDeserialize(original);

        assertEquals(10, copy.getAttemptId());
        assertEquals(3, copy.getQuestionId());
        assertEquals(Integer.valueOf(7), copy.getSelectedOptionId());
        assertTrue(copy.getCorrect());
        assertEquals(2, copy.getMarksObtained());
    }

    @Test
    public void testMonitoringSummarySerialization() throws Exception {
        MonitoringSummary original = new MonitoringSummary(4, 12);
        MonitoringSummary copy = serializeAndDeserialize(original);

        assertEquals(4, copy.getActiveAttemptCount());
        assertEquals(12, copy.getSubmittedReportCount());
    }

    private <T> T serializeAndDeserialize(T object) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        try (ObjectInputStream in = new ObjectInputStream(bis)) {
            @SuppressWarnings("unchecked")
            T result = (T) in.readObject();
            return result;
        }
    }
}
