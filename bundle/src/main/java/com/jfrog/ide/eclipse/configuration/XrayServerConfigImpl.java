package com.jfrog.ide.eclipse.configuration;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

import com.jfrog.ide.common.configuration.XrayServerConfig;

/**
 * @author yahavi
 */
public class XrayServerConfigImpl implements XrayServerConfig {

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
		return getString(PreferenceConstants.XRAY_URL);
	}

	@Override
	public String getUsername() {
		return getString(PreferenceConstants.XRAY_USERNAME);
	}

	@Override
	public String getPassword() {
		return getString(PreferenceConstants.XRAY_PASSWORD);
	}

	private String getString(String key) {
		String str = service.getString(PreferenceConstants.XRAY_QUALIFIER, key, "", null);
		return StringUtils.trim(str);
	}
}
