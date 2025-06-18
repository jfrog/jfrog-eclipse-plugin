package com.jfrog.ide.eclipse.ui.webview.events;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;

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
    
    public Receiver(CefBrowser cefBrowser) {
        this.messageRouter = CefMessageRouter.create();
        this.messageRouter.addHandler(new CefMessageRouterHandlerAdapter() {
            @Override
            public boolean onQuery(CefBrowser browser, CefFrame frame, long query_id, String request, boolean persistent, CefQueryCallback callback) {
                try {
                    IdeEvent event = unpack(request);
                    handler(event);
                    callback.success("");
                } catch (JsonProcessingException e) {
                    Logger.getInstance().error(e.getMessage());
                    callback.failure(500, e.getMessage());
                }
                return true;
            }
        }, true);
        cefBrowser.getClient().addMessageRouter(messageRouter);
    }

    public static IdeEvent unpack(String raw) throws JsonProcessingException {
        ObjectMapper ow = createMapper();
        return ow.readValue(raw, IdeEvent.class);
    }

    public String createIdeSendFuncBody(String ideSendFunctionName) {
        // This JS function sends a message to the Java side using the message router.
        return "window['" + ideSendFunctionName + "'] = obj => { " +
                "let raw = JSON.stringify(obj); " +
                "cefQuery({request: raw}); " +
                "}";
    }

    /**
     * Handles the received IdeEvent.
     *
     * @param event The received IdeEvent to handle.
     */
    private void handler(IdeEvent event) {
    	// TODO: add logic for handling Webview events such as: JUMP_TO_CODE
        Logger.getInstance().info("Received event from the webview: " + event.getType());
    }
    
}