package com.jfrog.ide.eclipse.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jfrog.build.client.Version;

import com.jfrog.ide.common.configuration.JfrogCliDriver;
import com.jfrog.ide.eclipse.log.Logger;

public class CliDriverWrapper {

    // Private static instance of the class (Singleton)
    private static CliDriverWrapper instance;

    // Instance of JfrogCliDriver
    private JfrogCliDriver cliDriver;
    
    // Constants
    public static final String CLIENT_ID_SERVER = "eclipse";
    public static final String CLI_VERSION = "2.74.1";
	public static final Path HOME_PATH = Paths.get(System.getProperty("user.home"), ".jfrog-eclipse-plugin");


    // Private constructor to prevent instantiation from outside
    private CliDriverWrapper() {
		try {
			Files.createDirectories(HOME_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	Version RequestedVersion = new Version(this.CLI_VERSION);
        // Initialize the cliDriver and download CLI if needed
        this.cliDriver = new JfrogCliDriver(null,Logger.getInstance());
        try {
			this.cliDriver.downloadCliIfNeeded(HOME_PATH.toString(), RequestedVersion);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public static CliDriverWrapper getInstance() {
        if (instance == null) {
            synchronized (CliDriverWrapper.class) {
                if (instance == null) {
                    instance = new CliDriverWrapper();
                }
            }
        }
        return instance;
    }

    public JfrogCliDriver getCliDriver() {
        return cliDriver;
    }
}
