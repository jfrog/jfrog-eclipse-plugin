package com.jfrog.ide.eclipse.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.jfrog.ide.eclipse.log.Logger;

public class CLIUtils {
	
	public static final String JFROG_CLI_RELEASES_URL = "https://releases.jfrog.io/artifactory/jfrog-cli/v2-jf/";
	public static final String JFROG_CLI_VERSION = "2.73.4";
	
	public static Logger logger = Logger.getInstance();
	
	public static void downloadCLIIfNeeded() {
		String systemFolderPath;
		// get OS type and architecture
		String osName = System.getProperty("os.name").toLowerCase();
		String osVersion = System.getProperty("os.version");
		String osArch = System.getProperty("os.arch");
		
        if (osName.contains("win")) {
            // For Windows, use SystemRoot environment variable
            systemFolderPath = System.getenv("SystemRoot") + "\\System32";
        } else if (osName.contains("mac")) {
            // For macOS, typically system folders are under /System
            systemFolderPath = "/System/Library";
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            // For Unix/Linux, system folders can be different, typically /usr or /usr/local
            systemFolderPath = "/usr/bin";
        } else {
            systemFolderPath = "Unsupported Operating System";
        }
        
        Path jfrogExeFilePath = Paths.get(systemFolderPath, "jf.exe");
        
        // check if jfrog.exe file exist in system folder. if not download it
        if (!Files.exists(jfrogExeFilePath)) {
        	logger.info("'jf.exe' file is not cached locally. Downloading it now...");
        	downloadCliFromReleases(osName);
        } else {
        	logger.info("'jf.exe' file is cached locally. File location: " + jfrogExeFilePath);
        	// run jf.exe --version in a process to verify correct CLI version
        }
	}
	
	public static String getCLIPath() {
		
		return "";
	}
	
	
	public static void downloadCliFromReleases(String osName) {
		String cliVersion = "";

		// download the relevant jf.exe version according to the defined version & OS type.
		if (osName.contains("win")) {
			logger.info("Downloading CLI for windows");
		} else {
			logger.info("Downloading CLI for linux/macOS");
		}
	}
}