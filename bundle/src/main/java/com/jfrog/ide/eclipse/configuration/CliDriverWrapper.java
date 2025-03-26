package com.jfrog.ide.eclipse.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


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
            this.showCliError(e);
        }
        // Initialize the cliDriver and download CLI if needed
        this.cliDriver = new JfrogCliDriver(null, Logger.getInstance());
        try {
            this.cliDriver.downloadCliIfNeeded(HOME_PATH.toString(), CLI_VERSION);
        } catch (IOException e) {
            this.showCliError(e);
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

    private void showCliError(Error e){
        Logger.getInstance().error(e.getMessage());
        IStatus status = new Status(IStatus.ERROR, "jfrog-eclipse-plugin", "An error occurred: " + e.getMessage(), e);
            Display.getDefault().asyncExec(() -> {
                Shell shell = Display.getDefault().getActiveShell();
                if (shell != null) {
                    ErrorDialog.openError(shell, "Error", "An error occurred", status);
                }
            });
        }
}

