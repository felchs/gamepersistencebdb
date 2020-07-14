package com.gametac.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesConfig {
	private static Properties properties;
	
	public static Properties loadProperties() {
		properties = new Properties();
		
		InputStream inStream = null;
		try {
			inStream = new FileInputStream("config/properties");
			loadProperties(inStream);
		} catch (FileNotFoundException e) {
			inStream = PropertiesConfig.class.getClassLoader().getResourceAsStream("dbproperties");
			loadProperties(inStream);
		}
		
		return properties;
	}

	private static void loadProperties(InputStream inStream) {
		try {
			properties.load(inStream);
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
