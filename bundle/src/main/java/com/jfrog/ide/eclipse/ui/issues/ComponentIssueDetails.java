package com.jfrog.ide.eclipse.ui.issues;

import org.eclipse.swt.widgets.Composite;

import com.jfrog.ide.common.nodes.FileIssueNode;
import com.jfrog.ide.eclipse.log.Logger;
import com.jfrog.ide.eclipse.ui.ComponentDetails;
import com.jfrog.ide.eclipse.ui.webview.WebviewManager;

/**
 * ComponentIssueDetails provides a detailed view of security issues using a webview.
 * It uses WebviewManager to handle all webview operations and communication.
 */
public class ComponentIssueDetails extends ComponentDetails {

	private static ComponentIssueDetails instance;
	private static final Logger log = Logger.getInstance();
	
	private WebviewManager webviewManager;

	public static ComponentIssueDetails createComponentIssueDetails(Composite parent) {
		instance = new ComponentIssueDetails(parent);
		return instance;
	}

	public static ComponentIssueDetails getInstance() {
		return instance;
	}

	private ComponentIssueDetails(Composite parent) {
		super(parent, "Issue Details");
		initializeWebviewManager();
	}

	@Override
	public void createDetailsView(FileIssueNode node) {
		if (webviewManager == null || !webviewManager.isReady()) {
			log.warn("WebviewManager not ready. Cannot display issue.");
			return;
		}
		
		try {
			webviewManager.displayIssue(node);
			refreshPanel();
		} catch (Exception e) {
			log.error("Error creating details view: " + e.getMessage(), e);
		}
	}

	@Override
	protected void createBrowserJCEF(Composite parent) {
		try {
			if (webviewManager == null) {
				log.error("WebviewManager not initialized");
				return;
			}
			
			// Get the webview URL
			String webviewUrl = getWebviewUrl();
			if (webviewUrl == null) {
				log.error("Could not find webview resources");
				return;
			}
			
			// Create browser using WebviewManager
			webviewManager.createBrowser(parent, webviewUrl);
			
			log.debug("Webview browser created successfully");
			
		} catch (Exception e) {
			log.error("Error in createBrowserJCEF: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Initializes the WebviewManager with proper configuration.
	 */
	private void initializeWebviewManager() {
		try {
			webviewManager = new WebviewManager();
			webviewManager.initialize();
				
			log.debug("WebviewManager initialized successfully");
			
		} catch (Exception e) {
			log.error("Failed to initialize WebviewManager: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Gets the webview URL. This method can be customized to load from different sources.
	 */
	private String getWebviewUrl() {
		// For now, using the hardcoded path. This can be made more flexible
		return "C:\\Users\\Keren Reshef\\Projects\\jfrog-eclipse-plugin\\bundle\\src\\main\\resources\\jfrog-ide-webview\\index.html";
	}

	public static void disposeComponentDetails() {
		if (instance != null) {
			instance.dispose();
		}
	}

	@Override
	public void dispose() {
		if (webviewManager != null) {
			webviewManager.dispose();
			webviewManager = null;
		}
		super.dispose();
	}
}
