package com.jfrog.ide.eclipse.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.StringUtils;

import com.jfrog.ide.eclipse.log.Logger;

public class CLIUtils {
	
	public static final String JFROG_CLI_RELEASES_URL = "https://releases.jfrog.io/artifactory/jfrog-cli/v2-jf/";
	public static final String JFROG_CLI_FIXED_VERSION = "2.73.3";
	
	public static Logger logger = Logger.getInstance();
	
	public static void downloadCLIIfNeeded() throws IOException {
		String systemFolderPath;
		// get OS type 
		String osName = System.getProperty("os.name").toLowerCase();
		
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
        	downloadCliFromReleases(osName, JFROG_CLI_FIXED_VERSION);
        } else {
        	logger.info("'jf.exe' file is cached locally. File location: " + jfrogExeFilePath);
        	// run jf.exe --version in a process to verify correct CLI version
        }
	}
	
	
	public static void downloadCliFromReleases(String osName, String cliVersion) throws IOException {
		String osAndArch = getOSAndArc();
		String fullCLIPath = JFROG_CLI_RELEASES_URL + cliVersion + "/jfrog-cli-" + osAndArch + "/jf.exe";
		String destinationPath = ""; // where we want to save the file? 
		
		// download jf.exe from 'fullCLIPath' and save it at 'destinationPath'
		
	}
	
    public static String getOSAndArc() throws IOException {
        String arch = SystemUtils.OS_ARCH;
        // Windows
        if (SystemUtils.IS_OS_WINDOWS) {
            return "windows-amd64";
        }
        // Mac
        if (SystemUtils.IS_OS_MAC) {
            if (StringUtils.equalsAny(arch, "aarch64", "arm64")) {
                return "mac-arm64";
            } else {
                return "mac-amd64";
            }
        }
        // Linux
        if (SystemUtils.IS_OS_LINUX) {
            switch (arch) {
                case "i386":
                case "i486":
                case "i586":
                case "i686":
                case "i786":
                case "x86":
                    return "linux-386";
                case "amd64":
                case "x86_64":
                case "x64":
                    return "linux-amd64";
                case "arm":
                case "armv7l":
                    return "linux-arm";
                case "aarch64":
                    return "linux-arm64";
                case "ppc64":
                case "ppc64le":
                    return "linux-" + arch;
            }
        }
        throw new IOException(String.format("Unsupported OS: %s-%s", SystemUtils.OS_NAME, arch));
    }
}