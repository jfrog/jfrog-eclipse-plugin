package com.jfrog.ide.eclipse.configuration;

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
	
	// Connection constants
	public static final int CONNECTION_TIMEOUT = 60;
	public static final int CONNECTION_RETRIES = 5;

	// Eclipse Buildship plugins
	public static final String GRADLE_PLUGIN_QUALIFIER = "org.eclipse.buildship.core";
	public static final String GRADLE_DISTRIBUTION = "gradle.distribution";
}
