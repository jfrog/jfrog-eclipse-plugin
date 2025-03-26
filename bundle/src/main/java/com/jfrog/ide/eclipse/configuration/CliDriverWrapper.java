package com.jfrog.ide.eclipse.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.jfrog.ide.common.configuration.JfrogCliDriver;
import com.jfrog.ide.eclipse.log.Logger;

public class CliDriverWrapper {

    private static CliDriverWrapper instance;

    public static final String CLIENT_ID_SERVER = "eclipse";
    public static final String CLI_VERSION = "2.74.1";
    public static final Path HOME_PATH = Paths.get(System.getProperty("user.home"), ".jfrog-eclipse-plugin");

    private JfrogCliDriver cliDriver;

    private CliDriverWrapper() {
        try {
            Files.createDirectories(HOME_PATH);
        } catch (Exception e) {
            showCliError(e);
        }
        // Initialize the cliDriver and download CLI if needed
        this.cliDriver = new JfrogCliDriver(null, Logger.getInstance());
        try {
            this.cliDriver.downloadCliIfNeeded(HOME_PATH.toString(), CLI_VERSION);
        } catch (IOException e) {
            showCliError(e);
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

    public void showCliError(Exception e) {
        Logger.getInstance().error(e.getMessage(), e);
        IStatus status = new Status(IStatus.ERROR, "jfrog-eclipse-plugin", "An error occurred: " + e.getMessage(), e);

        // Run UI-related code on the main UI thread
        Display.getDefault().asyncExec(() -> {
            Shell shell = Display.getDefault().getActiveShell();
            if (shell != null) {
                ErrorDialog.openError(shell, "Error", "An error occurred", status);
            }
        });
    }
}
