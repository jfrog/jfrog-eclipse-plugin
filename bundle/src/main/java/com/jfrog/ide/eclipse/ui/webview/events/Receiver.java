package com.jfrog.ide.eclipse.ui.webview.events;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.eclipse.core.resources.IProject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfrog.ide.common.webview.events.IdeEvent;
import com.jfrog.ide.eclipse.log.Logger;

import static com.jfrog.ide.common.utils.Utils.createMapper;


/**
 * The Receiver class is responsible for handling events received from the webview in the IDE.
 * It sets up the necessary query handling and provides a mechanism to process the received events.
 */
public class Receiver {
	 private final CefMessageRouter messageRouter;
    CefBrowser browser;
    IProject project;

    /**
     * @param browser The JBCefBrowser associated with the webview.
     * @param project   The Project associated with the IDE.
     */
    public Receiver(CefBrowser browser, IProject project) {
        this.project = project;
        this.browser = browser;
        this.messageRouter = CefMessageRouter.create();
        this.messageRouter.addHandler(new MessageHandler(), true);
    }
    
    public CefMessageRouter getMessageRouter() {
        return messageRouter;
    }

    /**
     * Unpacks the raw JSON string into an IdeEvent object.
     *
     * @param raw The raw JSON string to unpack.
     * @return The unpacked IdeEvent.
     * @throws JsonProcessingException If an error occurs during JSON processing.
     */
    public static IdeEvent unpack(String raw) throws JsonProcessingException {
        ObjectMapper ow = createMapper();
        return ow.readValue(raw, IdeEvent.class);
    }

    /**
     * Creates the body of the IDE send function with the specified function name.
     *
     * @param ideSendFunctionName The name of the IDE send function.
     * @return The body of the IDE send function as a string.
     */
    public String createIdeSendFuncBody(String ideSendFunctionName) {
        return "window['" + ideSendFunctionName + "'] = obj => { let raw = JSON.stringify(obj);  " + "cefQuery({ request: raw }); };";
    }

    /**
     * Handles the received IdeEvent.
     *
     * @param event The received IdeEvent to handle.
     */
    private void handler(IdeEvent event) {
        Logger.getInstance().debug("Received event from the webview: " + event.getType());
    }
    
    private class MessageHandler extends CefMessageRouterHandlerAdapter {
    	@Override
    	public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
            try {
                IdeEvent event = unpack(request);
                handler(event);
                callback.success("Received");
            } catch (Exception e) {
                callback.failure(500, e.getMessage());
            }
            return true;
        }
    }
}