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
import org.jfrog.client.http.model.ProxyConfig;

import com.jfrog.ide.common.configuration.ServerConfig;

/**
 * @author yahavi
 */
@SuppressWarnings("restriction")
public class XrayServerConfigImpl implements ServerConfig {

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
	public ProxyConfig getProxyConfForTargetUrl(String xrayUrl) {
		xrayUrl = StringUtils.defaultIfBlank(xrayUrl, getUrl());
		IProxyService service = ProxyManager.getProxyManager();
		IProxyData[] proxyData = service.select(URI.create(xrayUrl));
		if (ArrayUtils.isEmpty(proxyData)) {
			return null;
		}

		ProxyConfig proxyConfig = new ProxyConfig();
		proxyConfig.setHost(trim(proxyData[0].getHost()));
		proxyConfig.setPort(proxyData[0].getPort());
		if (proxyData[0].isRequiresAuthentication()) {
			proxyConfig.setUsername(trim(proxyData[0].getUserId()));
			proxyConfig.setPassword(proxyData[0].getPassword());
		}
		return proxyConfig;
	}

	@Override
	public String getArtifactoryUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getConnectionRetries() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getConnectionTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SSLContext getSslContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXrayUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInsecureTls() {
		// TODO Auto-generated method stub
		return false;
	}
}
