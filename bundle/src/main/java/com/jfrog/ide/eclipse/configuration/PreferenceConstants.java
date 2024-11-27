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

	// Eclipse Buildship plugins
	public static final String GRADLE_PLUGIN_QUALIFIER = "org.eclipse.buildship.core";
	public static final String GRADLE_DISTRIBUTION = "gradle.distribution";
	
	// Default exluded paths pattern
	public static final String DEFAULT_EXCLUSIONS = "**/*{.idea,test,node_modules}*";
	
}
