package com.jfrog.ide.eclipse.ui.webview.events;

import org.cef.browser.CefBrowser;
import com.jfrog.ide.common.webview.events.WebviewEvent;

/**
 * The EventManager is responsible for managing events between the IDE and the Webview.
 * It handles the creation of a receiver and sender, allowing communication between the components.
 */
public class EventManager {
    private final static String ideSendFuncName = "sendMessageToIdeFunc";
    private final Receiver receiver;
    private final Sender sender;

    /**
     * Constructs a new EventManager with the provided CefBrowser.
     * Note: The eventManager must be created before the webview is initialized.
     *
     * @param cefBrowser The JBCefBrowser associated with the webview.
     * @param project   The Project associated with the IDE.
     */
    public EventManager(CefBrowser cefBrowser) {
        this.receiver = new Receiver(cefBrowser);
        this.sender = new Sender(cefBrowser);
    }

    /**
     * Invoked when the webview finishes loading.
     * Creates the IDE send function body and sends it to the webview.
     * Finally, it runs onLoadEvent, if provided.
     *
     * @param onLoadEnd A {@link Runnable} to run when the webview finishes loading.
     */
    public void onWebviewLoadEnd() {
        String ideSendFuncBody = this.receiver.createIdeSendFuncBody(ideSendFuncName);
        this.sender.sendIdeSendFunc(ideSendFuncName, ideSendFuncBody);
    }

    /**
     * Sends an event of the specified type and data to the webview.
     *
     * @param type The type of the webview event.
     * @param data The data associated with the event.
     */
    public void send(WebviewEvent.Type type, Object data) {
        this.sender.sendEvent(type, data);
    }
}
