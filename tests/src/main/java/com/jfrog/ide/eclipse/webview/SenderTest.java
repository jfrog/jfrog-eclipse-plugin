package com.jfrog.ide.eclipse.webview;

import com.jfrog.ide.eclipse.ui.webview.events.Sender;
import com.jfrog.ide.common.webview.events.WebviewEvent;
import junit.framework.TestCase;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.swing.SwingUtilities;

public class SenderTest extends TestCase {
    public void testPack() throws JsonProcessingException {
        String type = "SET_EMITTER";
        String data = "testData";
        // Use reflection or a mock WebviewEvent if needed, here we just check JSON structure
        String json = Sender.pack(WebviewEvent.Type.SET_EMITTER, data);
        assertTrue(json.contains(type));
        assertTrue(json.contains(data));
    }

    public void testSend() throws Exception {
        CefBrowserStub browser = new CefBrowserStub();
        Sender sender = new Sender(browser);
        String alert = "alert('message')";
        // SwingUtilities.invokeLater is async, so wait for it
        sender.send(alert);
        SwingUtilities.invokeAndWait(() -> {}); // Wait for EDT
        assertEquals(alert, browser.lastJs);
        assertEquals(1, browser.jsCallCount);
    }

    public void testPackThrowsException() {
        try {
            // Jackson cannot serialize objects with circular references
            Object circular = new Object() {
                public Object self = this;
            };
            Sender.pack(WebviewEvent.Type.SET_EMITTER, circular);
            fail("Expected JsonProcessingException");
        } catch (JsonProcessingException e) {
            // Expected
        }
    }

    public void testPackWithNullData() throws Exception {
        String json = Sender.pack(WebviewEvent.Type.SET_EMITTER, null);
        assertTrue(json.contains("SET_EMITTER"));
        assertFalse(json.contains("data"));
    }

    public void testPackWithPrimitiveData() throws Exception {
        String json = Sender.pack(WebviewEvent.Type.SET_EMITTER, 123);
        assertTrue(json.contains("123"));
    }

    public void testPackWithComplexData() throws Exception {
        class Data { public String data = "message"; }
        String json = Sender.pack(WebviewEvent.Type.SET_EMITTER, new Data());
        assertTrue(json.contains("data"));
        assertTrue(json.contains("message"));
    }

    public void testSendWithEmptyString() throws Exception {
        CefBrowserStub browser = new CefBrowserStub();
        Sender sender = new Sender(browser);
        sender.send("");
        SwingUtilities.invokeAndWait(() -> {});
        assertEquals("", browser.lastJs);
        assertEquals(1, browser.jsCallCount);
    }

    public void testSendWithNullString() throws Exception {
        CefBrowserStub browser = new CefBrowserStub();
        Sender sender = new Sender(browser);
        sender.send(null);
        SwingUtilities.invokeAndWait(() -> {});
        assertNull(browser.lastJs);
        assertEquals(1, browser.jsCallCount);
    }

} 