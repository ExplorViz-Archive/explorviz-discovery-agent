package net.explorviz.discoveryagent.util;

import java.util.HashMap;
import java.util.Map;

public class TypeService {

	public static Map<String, Class<?>> typeMap = new HashMap<>();

	public static Class<?> giveClassForString(final String stringThatContainsClassName) {

		for (final Map.Entry<String, Class<?>> entry : typeMap.entrySet()) {

			if (stringThatContainsClassName.contains(entry.getKey())) {
				return entry.getValue();
			}
		}

		return null;

	}

}
