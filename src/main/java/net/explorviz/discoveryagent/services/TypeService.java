package net.explorviz.discoveryagent.services;

import java.util.HashMap;
import java.util.Map;

public final class TypeService {

	public static Map<String, Class<?>> typeMap = new HashMap<>();

	private TypeService() {
		// no need to instantiate
	}

	public static Class<?> giveClassForString(final String stringThatContainsClassName) {

		for (final Map.Entry<String, Class<?>> entry : typeMap.entrySet()) {

			if (stringThatContainsClassName.contains(entry.getKey())) {
				return entry.getValue();
			}
		}

		return null;

	}

}
