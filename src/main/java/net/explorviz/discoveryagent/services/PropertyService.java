package net.explorviz.discoveryagent.services;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PropertyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyService.class);

	private static final Properties PROP = new Properties();

	private PropertyService() {
		// don't instantiate
	}

	static {
		try {
			PROP.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("explorviz.properties"));
		} catch (final IOException e) {
			LOGGER.error("Couldn't load properties file. Is WEB-INF/classes/explorviz.properties a valid file? ");
		}
	}

	public static String getStringProperty(final String propName) {
		return (String) PROP.get(propName);
	}

}
