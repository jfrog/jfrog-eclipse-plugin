package com.jfrog.ide.eclipse.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import org.apache.commons.lang3.SystemUtils;
import org.jfrog.build.extractor.executor.CommandExecutor;
import org.jfrog.build.extractor.executor.CommandResults;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;

import com.jfrog.ide.eclipse.log.Logger;

public class CLIUtils {
	
	public static final String JFROG_CLI_RELEASES_URL = "https://releases.jfrog.io/artifactory/jfrog-cli/v2-jf/";
	public static final String MINIMUM_JFROG_CLI_VERSION = "2.69.0"; // TODO: TBD
	public static final String MAXIMUM_JFROG_CLI_VERSION = "2.73.3"; // TODO: TBD
	public static final String DEFAULT_CLI_DESTINATION_PATH = ""; // TODO: determine where we would like to download and save the cli exe
	
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
            logger.error("Unsupported Operating System " + osName);
            throw new UnsupportedOperationException("Unsupported Operating System " + osName);
        }
        
        Path jfrogExeFilePath = Paths.get(systemFolderPath, "jf.exe");
        
        // Check if 'jf.exe' exists locally
        if (Files.exists(jfrogExeFilePath)) {
        	logger.info("'jf.exe' file is cached locally. File location: " + jfrogExeFilePath);
        	
        	// Execute 'jf.exe --version' to verify CLI version
        	CommandExecutor commandExecutor = new CommandExecutor(jfrogExeFilePath.toString(), null);
        	List<String> versionCommand = Arrays.asList("--version");
        	
        	try {
				CommandResults versionCommandOutput = commandExecutor.exeCommand(null, versionCommand, null, logger);
				String cliVersion = extractVersion(versionCommandOutput.getRes());
				
				if (validateCLIVersion(cliVersion)) {
					logger.debug("Local CLI version is: " + cliVersion);
					logger.info("Local 'jf.exe' file version has been verified and is compatible. Proceeding with its usage.");
				} else {
					logger.info("Local 'jf.exe' file version is not compatible. Downloading v" + MAXIMUM_JFROG_CLI_VERSION);
					downloadCliFromReleases(osName, MAXIMUM_JFROG_CLI_VERSION, systemFolderPath);
				}
			} catch (InterruptedException | IOException e) {
				// TODO: should we fail in case of error or download a new cli exe ?
				logger.error("Failed to verify CLI version. Downloading v"+ MAXIMUM_JFROG_CLI_VERSION);
				downloadCliFromReleases(osName, MAXIMUM_JFROG_CLI_VERSION, systemFolderPath);
			}
        } else {
        	logger.info("'jf.exe' file is not cached locally. Downloading it now...");
        	downloadCliFromReleases(osName, MAXIMUM_JFROG_CLI_VERSION, systemFolderPath);
        }
	}
	
	
	public static void downloadCliFromReleases(String osName, String cliVersion, String destinationPath) throws IOException {
		String osAndArch = getOSAndArc();
		String fullCLIPath = JFROG_CLI_RELEASES_URL + cliVersion + "/jfrog-cli-" + osAndArch + "/jf.exe";
		
		// TODO: download jf.exe from 'fullCLIPath' and save it at 'destinationPath'
		
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
    
    public static String extractVersion(String input) {
        String regex = "\\d+(\\.\\d+)*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
    
    public static Boolean validateCLIVersion(String cliVersion) {
    	 ComparableVersion currentCLIVersion = new ComparableVersion(cliVersion);
    	 ComparableVersion maxCLIVersion = new ComparableVersion(MAXIMUM_JFROG_CLI_VERSION);
    	 ComparableVersion minCLIVersion = new ComparableVersion(MINIMUM_JFROG_CLI_VERSION);
    	 
    	 return currentCLIVersion.compareTo(minCLIVersion) >=0 && currentCLIVersion.compareTo(maxCLIVersion) <= 0;
    }
}