package com.jfrog.ide.eclipse.webview;

import java.awt.Component;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefDevToolsClient;
import org.cef.browser.CefFrame;
import org.cef.callback.CefPdfPrintCallback;
import org.cef.callback.CefRunFileDialogCallback;
import org.cef.callback.CefStringVisitor;
import org.cef.handler.CefDialogHandler.FileDialogMode;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefWindowHandler;
import org.cef.misc.CefPdfPrintSettings;
import org.cef.network.CefRequest;

class CefBrowserStub implements CefBrowser {
    public String lastJs; public int jsCallCount = 0;
    @Override public void executeJavaScript(String code, String url, int line) { lastJs = code; jsCallCount++; }
    @Override public CefClient getClient() { throw new UnsupportedOperationException(); }
    @Override public String getURL() { throw new UnsupportedOperationException(); }
    @Override public void loadURL(String url) { throw new UnsupportedOperationException(); }
    @Override public void reload() { throw new UnsupportedOperationException(); }
    @Override public void stopLoad() { throw new UnsupportedOperationException(); }
    @Override public void goBack() { throw new UnsupportedOperationException(); }
    @Override public void goForward() { throw new UnsupportedOperationException(); }
    @Override public boolean isLoading() { throw new UnsupportedOperationException(); }
    @Override public void close(boolean force) { throw new UnsupportedOperationException(); }
    @Override public void setFocus(boolean enable) { throw new UnsupportedOperationException(); }
    @Override public void setZoomLevel(double zoomLevel) { throw new UnsupportedOperationException(); }
    @Override public double getZoomLevel() { throw new UnsupportedOperationException(); }
    @Override public void startDownload(String url) { throw new UnsupportedOperationException(); }
    @Override public void print() { throw new UnsupportedOperationException(); }
    @Override public void stopFinding(boolean clearSelection) { throw new UnsupportedOperationException(); }
    @Override public boolean canGoBack() { return false; }
    @Override public boolean canGoForward() { return false; }
    @Override public void closeDevTools() { }
    @Override public void createImmediately() { }
    @Override public CompletableFuture<BufferedImage> createScreenshot(boolean arg0) { return null; }
    @Override public boolean doClose() { return false; }
    @Override public void find(String arg0, boolean arg1, boolean arg2, boolean arg3) { }
    @Override public CefDevToolsClient getDevToolsClient() { return null; }
    @Override public CefFrame getFocusedFrame() { return null; }
    @Override public CefFrame getFrameByIdentifier(String arg0) { return null; }
    @Override public CefFrame getFrameByName(String arg0) { return null; }
    @Override public int getFrameCount() { return 0; }
    @Override public Vector<String> getFrameIdentifiers() { return null; }
    @Override public Vector<String> getFrameNames() { return null; }
    @Override public int getIdentifier() { return 0; }
    @Override public CefFrame getMainFrame() { return null; }
    @Override public CefRenderHandler getRenderHandler() { return null; }
    @Override public void getSource(CefStringVisitor arg0) { }
    @Override public void getText(CefStringVisitor arg0) { }
    @Override public Component getUIComponent() { return null; }
    @Override public CefWindowHandler getWindowHandler() { return null; }
    @Override public CompletableFuture<Integer> getWindowlessFrameRate() { return null; }
    @Override public boolean hasDocument() { return false; }
    @Override public boolean isPopup() { return false; }
    @Override public void loadRequest(CefRequest arg0) { }
    @Override public void onBeforeClose() { }
    @Override public void openDevTools() { }
    @Override public void openDevTools(Point arg0) { }
    @Override public void printToPDF(String arg0, CefPdfPrintSettings arg1, CefPdfPrintCallback arg2) { }
    @Override public void reloadIgnoreCache() { }
    @Override public void replaceMisspelling(String arg0) { }
    @Override public void runFileDialog(FileDialogMode arg0, String arg1, String arg2, Vector<String> arg3, int arg4, CefRunFileDialogCallback arg5) { }
    @Override public void setCloseAllowed() { }
    @Override public void setWindowVisibility(boolean arg0) { }
    @Override public void setWindowlessFrameRate(int arg0) { }
    @Override public void viewSource() { }
}