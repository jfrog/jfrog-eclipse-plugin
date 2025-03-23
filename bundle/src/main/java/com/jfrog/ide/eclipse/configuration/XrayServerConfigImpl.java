package com.jfrog.ide.eclipse.configuration;

import static org.apache.commons.lang3.StringUtils.trim;

import java.net.URI;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.internal.net.ProxyManager;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.jfrog.build.client.ProxyConfiguration;

import com.jfrog.ide.common.configuration.ServerConfig;

/**
 * @author yahavi
 */
@SuppressWarnings("restriction")
public class XrayServerConfigImpl implements ServerConfig {
	// TODO: change implementation to configure the server using JfrogCliDriver

	private static XrayServerConfigImpl instance;
	private IPreferencesService service = Platform.getPreferencesService();

	public static XrayServerConfigImpl getInstance() {
		if (instance == null) {
			instance = new XrayServerConfigImpl();
		}
		return instance;
	}

	@Override
	public String getUrl() {
		return getValue(PreferenceConstants.XRAY_URL);
	}

	@Override
	public String getUsername() {
		return getValue(PreferenceConstants.XRAY_USERNAME);
	}

	@Override
	public String getPassword() {
		return getValue(PreferenceConstants.XRAY_PASSWORD);
	}

	private String getValue(String key) {
		return trim(service.getString(PreferenceConstants.XRAY_QUALIFIER, key, "", null));
	}

	@Override
	public ProxyConfiguration getProxyConfForTargetUrl(String xrayUrl) {
		xrayUrl = StringUtils.defaultIfBlank(xrayUrl, getUrl());
		IProxyService service = ProxyManager.getProxyManager();
		IProxyData[] proxyData = service.select(URI.create(xrayUrl));
		if (ArrayUtils.isEmpty(proxyData)) {
			return null;
		}

		ProxyConfiguration proxyConfig = new ProxyConfiguration();
		proxyConfig.host = trim(proxyData[0].getHost());
		proxyConfig.port = proxyData[0].getPort();
		if (proxyData[0].isRequiresAuthentication()) {
			proxyConfig.username = trim(proxyData[0].getUserId());
			proxyConfig.password = proxyData[0].getPassword();
		}
		return proxyConfig;
	}

	@Override
	public String getXrayUrl() {
		String url = getUrl();
		String xrayUrl = url.endsWith("/") ? url + "xray" : url + "/xray";
		return xrayUrl;
	}

	@Override
	public String getArtifactoryUrl() {
		String url = getUrl();
		String artifactoryUrl = url.endsWith("/") ? url + "artifactory" : url + "/artifactory";
		return artifactoryUrl;
	}

	@Override
	public int getConnectionRetries() {
		return PreferenceConstants.CONNECTION_RETRIES;
	}

	@Override
	public int getConnectionTimeout() {
		return PreferenceConstants.CONNECTION_TIMEOUT_MILLISECONDS;
	}

	@Override
	public SSLContext getSslContext() {
		// This method is not used by the plug-in.
		return null;
	}

	@Override
	public boolean isInsecureTls() {
		// This method is not used by the plug-in.
		return false;
	}

	@Override
	public String getAccessToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getExternalResourcesRepo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PolicyType getPolicyType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWatches() {
		// TODO Auto-generated method stub
		return null;
	}

}
