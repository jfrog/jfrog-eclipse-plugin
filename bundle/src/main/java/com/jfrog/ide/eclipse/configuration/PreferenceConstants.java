package com.jfrog.ide.eclipse.configuration;

import java.util.HashMap;
import java.util.Map;
/**
 * Constant definitions for plug-in preferences
 * 
 * @author yahavi
 */
public class PreferenceConstants {

	// JFrog Eclipse plugin
	public static final String XRAY_QUALIFIER = "org.jfrog.xray.configuration";
	public static final String XRAY_URL = "URL";
	public static final String XRAY_USERNAME = "Username";
	public static final String XRAY_PASSWORD = "Password";
	public static final String DEBUG_LOGS = "DebugLogs";
	
	// Connection constants
	public static final int CONNECTION_TIMEOUT_MILLISECONDS = 300 * 1000;
	public static final int CONNECTION_RETRIES = 5;

	// Eclipse Buildship plugins
	public static final String GRADLE_PLUGIN_QUALIFIER = "org.eclipse.buildship.core";
	public static final String GRADLE_DISTRIBUTION = "gradle.distribution";
	
	
	public static Map<String, String> getCliDebugLogsEnvVars(){
		Map<String, String> envVars = new HashMap<>();
		envVars.put("JFROG_CLI_LOG_LEVEL", "DEBUG");
		envVars.put("CI", "true");
		
		return envVars;
	}
}
