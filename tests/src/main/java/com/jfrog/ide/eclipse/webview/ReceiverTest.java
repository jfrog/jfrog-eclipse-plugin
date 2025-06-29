package com.jfrog.ide.eclipse.webview;

import com.jfrog.ide.eclipse.ui.webview.events.Receiver;
import com.jfrog.ide.common.webview.events.IdeEvent;
import junit.framework.TestCase;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ReceiverTest extends TestCase {
    public void testUnpack() throws JsonProcessingException {
        String json = "{\"type\":\"JUMP_TO_CODE\",\"data\":{\"filePath\":\"/path/to/file.java\",\"lineNumber\":42}}";
        IdeEvent event = Receiver.unpack(json);
        assertNotNull(event);
        assertEquals("JUMP_TO_CODE", event.getType().toString());
        assertNotNull(event.getData());
        assertTrue((event.getData().toString()).contains("filePath"));
    }

    public void testUnpackWithNullData() throws JsonProcessingException {
        String json = "{\"type\":\"JUMP_TO_CODE\",\"data\":null}";
        IdeEvent event = Receiver.unpack(json);
        assertNotNull(event);
        assertEquals("JUMP_TO_CODE", event.getType().toString());
        assertNull(event.getData());
    }

    public void testUnpackWithExtraFields() throws JsonProcessingException {
        String json = "{\"type\":\"JUMP_TO_CODE\",\"data\":{\"filePath\":\"path\"},\"extra\":\"data\"}";
        IdeEvent event = Receiver.unpack(json);
        assertNotNull(event);
        assertEquals("JUMP_TO_CODE", event.getType().toString());
        assertNotNull(event.getData());
        assertTrue(event.getData().toString().contains("filePath"));
    }

    public void testUnpackWithInvalidJson() {
        String json = "{type:BAD_JSON}";
        try {
            Receiver.unpack(json);
            fail("Expected JsonProcessingException");
        } catch (JsonProcessingException e) {
            // Expected
        }
    }

    public void testUnpackWithMissingType() throws JsonProcessingException {
        String json = "{\"data\":{}}";
    	IdeEvent event = Receiver.unpack(json);
        assertNotNull(event);
        assertTrue(json.contains("data"));
        assertNull(event.getType());
        
    }

    public void testUnpackWithDifferentEventType() throws JsonProcessingException {
        String json = "{\"type\":\"SOME_EVENT\",\"data\":{\"value\":42}}";
        try
        {
            Receiver.unpack(json);
            fail("Expected JsonProcessingException");
        } catch (JsonProcessingException e) {
        	// Expected
        }
    }
} 