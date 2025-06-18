package com.jfrog.ide.eclipse.ui.webview;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.jfrog.ide.common.nodes.FileIssueNode;
import com.jfrog.ide.common.nodes.SastIssueNode;
import com.jfrog.ide.common.nodes.ScaIssueNode;
import com.jfrog.ide.common.webview.events.WebviewEvent;
import com.jfrog.ide.common.webview.events.WebviewEvent.Type;
import com.jfrog.ide.eclipse.log.Logger;

/**
 * WebviewManager provides a high-level abstraction for managing webview operations.
 * It aggregates all webview commands and provides a unified interface for:
 * - Browser initialization and lifecycle management
 * - Event communication between IDE and webview
 * - Message handling and debugging
 * - Issue display operations
 */
public class WebviewManager {
	private static final Logger log = Logger.getInstance();
	
	// Core components
	private EventManager eventManager;
	private CefBrowser cefBrowser;
	private CefClient client;
	private CefApp cefApp;
	private Frame frame;
	
	// State management
	private final AtomicBoolean browserInitialized = new AtomicBoolean(false);
	private final CountDownLatch initLatch = new CountDownLatch(1);
	private final AtomicBoolean isDisposed = new AtomicBoolean(false);
	
	
	/**
	 * Creates a new WebviewManager instance.
	 * Note: The manager must be initialized before use.
	 */
	public WebviewManager() {
		// Constructor - initialization will be done separately
	}
	
	/**
	 * Initializes the CEF browser and webview components.
	 * This method should be called before any webview operations.
	 */
	public void initialize() {
		if (browserInitialized.get()) {
			log.warn("WebviewManager already initialized");
			return;
		}
		
		try {
			initializeCef();
			log.debug("WebviewManager initialized successfully");
		} catch (Exception e) {
			log.error("Failed to initialize WebviewManager: " + e.getMessage(), e);
			throw new RuntimeException("WebviewManager initialization failed", e);
		}
	}
	
	/**
	 * Creates the browser component and embeds it in the provided composite.
	 * 
	 * @param parent The SWT composite to embed the browser in
	 * @param webviewUrl The URL of the webview HTML file
	 */
	public void createBrowser(Composite parent, String webviewUrl) {
		if (!browserInitialized.get()) {
			log.error("WebviewManager not initialized. Call initialize() first.");
			return;
		}
		
		try {
			// Wait for CEF initialization
			initLatch.await();
			
			if (!browserInitialized.get()) {
				log.error("CEF initialization failed");
				return;
			}
			
			// Create frame on SWT thread
			Display.getDefault().syncExec(() -> {
				frame = SWT_AWT.new_Frame(parent);
			});
			
			// Create browser on Swing thread
			SwingUtilities.invokeLater(() -> {
				try {
					// Create browser
					cefBrowser = client.createBrowser(webviewUrl, false, false);
					
					// Initialize event manager after browser is created
					eventManager = new EventManager(cefBrowser);
					
					// Set up load handler
					setupLoadHandler(() -> eventManager.onWebviewLoadEnd());
					
					// Add browser to frame
					frame.add(cefBrowser.getUIComponent(), BorderLayout.CENTER);
					frame.validate();
					
					log.info("Webview browser created and embedded successfully");
					
				} catch (Exception e) {
					log.error("Error creating browser: " + e.getMessage(), e);
				}
			});
			
		} catch (Exception e) {
			log.error("Error in createBrowser: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Sends a message to the webview.
	 * 
	 * @param type The type of webview event
	 * @param data The data to send
	 */
	public void sendMessage(WebviewEvent.Type type, Object data) {
		if (eventManager == null) {
			log.error("EventManager not initialized. Cannot send message.");
			return;
		}
		eventManager.send(type, data);
	}
	
	/**
	 * Displays an issue in the webview.
	 * 
	 * @param node The FileIssueNode to display
	 */
	public void displayIssue(FileIssueNode node) {
		if (!browserInitialized.get()) {
			log.warn("Browser not initialized yet");
			return;
		}
		
		if (eventManager == null) {
			log.error("EventManager not initialized. Cannot display issue.");
			return;
		}
		
		try {
			if (node instanceof ScaIssueNode) {
				eventManager.send(Type.SHOW_PAGE, 
					WebviewObjectConverter.convertScaIssueToDepPage((ScaIssueNode) node));
			} else if (node instanceof SastIssueNode) {
				eventManager.send(Type.SHOW_PAGE, 
					WebviewObjectConverter.convertSastIssueToSastIssuePage((SastIssueNode) node));
			} else {
				// IAC or Secrets
				eventManager.send(Type.SHOW_PAGE, 
					WebviewObjectConverter.convertFileIssueToIssuePage(node));
			}
			log.debug("Issue displayed successfully: " + node.getTitle());
		} catch (Exception e) {
			log.error("Error displaying issue: " + e.getMessage(), e);
		}
	}
	
	
	/**
	 * Refreshes the webview panel.
	 */
	public void refresh() {
		if (frame != null) {
			SwingUtilities.invokeLater(() -> {
				frame.validate();
				frame.repaint();
			});
		}
	}
	
	/**
	 * Checks if the browser is initialized and ready.
	 * 
	 * @return true if the browser is ready, false otherwise
	 */
	public boolean isReady() {
		return browserInitialized.get() && eventManager != null;
	}
	
	/**
	 * Disposes of all webview resources.
	 */
	public void dispose() {
		if (isDisposed.getAndSet(true)) {
			return; // Already disposed
		}
		
		SwingUtilities.invokeLater(() -> {
			try {
				if (cefBrowser != null) {
					cefBrowser.close(true);
					cefBrowser = null;
				}
				if (client != null) {
					client.dispose();
					client = null;
				}
				if (frame != null) {
					frame.dispose();
					frame = null;
				}
			} catch (Exception e) {
				log.error("Error disposing browser: " + e.getMessage());
			}
		});
		
		if (cefApp != null) {
			cefApp.dispose();
			cefApp = null;
		}
		
		log.debug("WebviewManager disposed successfully");
	}
	
	private void initializeCef() {
		if (cefApp != null) return;
		
		new Thread(() -> {
			try {
				CefSettings settings = new CefSettings();
				settings.windowless_rendering_enabled = false;
				settings.log_severity = CefSettings.LogSeverity.LOGSEVERITY_VERBOSE;
				
				// Initialize CEF
				CefApp.startup(new String[0]);
				cefApp = CefApp.getInstance(settings);
				
				// Create client
				client = cefApp.createClient();
				
				// Add message router for console logging
				CefMessageRouter messageRouter = CefMessageRouter.create();
				client.addMessageRouter(messageRouter);
				
				browserInitialized.set(true);
				initLatch.countDown();
				log.debug("CEF initialized successfully");
				
			} catch (Exception e) {
				log.error("Failed to initialize CEF: " + e.getMessage());
				e.printStackTrace();
				initLatch.countDown();
			}
		}, "CEF-Init").start();
	}
	
	private void setupLoadHandler(Runnable onLoadEnd) {
		cefBrowser.getClient().addLoadHandler(new CefLoadHandlerAdapter() {
			@Override
			public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
				log.debug("Webview loading ended with status code " + httpStatusCode);
				super.onLoadEnd(browser, frame, httpStatusCode);
				if (onLoadEnd != null) {
					onLoadEnd.run();
				}
			}
			
			@Override
			public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode, 
								  String errorText, String failedUrl) {
				super.onLoadError(browser, frame, errorCode, errorText, failedUrl);
				// When opening links in external browser, JBCef cancels the page redirection
				// and opens the page in a new browser window.
				// This cancellation causes CEF to throw an ERR_ABORTED error.
//				if (errorCode == ErrorCode.ERR_ABORTED) {
//					return;
//				}
				log.error("An error occurred while loading the webview: " + errorText);
			}
		});
	}
	
	public EventManager getEventManager() {
		return eventManager;
	}
	
	public CefBrowser getCefBrowser() {
		return cefBrowser;
	}
	
	public boolean isDisposed() {
		return isDisposed.get();
	}
}
