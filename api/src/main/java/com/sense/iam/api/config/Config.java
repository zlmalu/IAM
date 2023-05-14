package com.sense.iam.api.config;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Config {
	private static Map<String, String> Config;
	
	static {
		Config = new HashMap<String, String>();
		ResourceBundle mapping = ResourceBundle.getBundle("config");
		Enumeration<?> en = mapping.getKeys();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			String value = mapping.getString(key);
			Config.put(key, value);
		}
	}

	public static String getConfig(String parameter) {
		if (Config.containsKey(parameter))
			return Config.get(parameter);
		return parameter;
	}
}
