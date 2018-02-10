package net.explorviz.discoveryagent.procezz.management;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzManagementTypeNotFoundException;
import net.explorviz.discoveryagent.procezz.management.types.JavaCLIManagementType;

public final class ProcezzManagementTypeFactory {

	private static ConcurrentMap<String, ProcezzManagementType> managementTypes = new ConcurrentHashMap<>();

	private ProcezzManagementTypeFactory() {
		// no need to instantiate
	}

	private static void createManagementTypes() {

		if (managementTypes.isEmpty()) {

			final ProcezzManagementType type = new JavaCLIManagementType();
			managementTypes.put(type.getManagementTypeDescriptor(), type);
		}

	}

	public static ProcezzManagementType getProcezzManagement(final String identifier)
			throws ProcezzManagementTypeNotFoundException {

		String possibleKey = null;

		for (final String key : managementTypes.keySet()) {
			if (key.equalsIgnoreCase(identifier)) {
				possibleKey = key;
				break;
			}
		}

		if (possibleKey == null) {
			throw new ProcezzManagementTypeNotFoundException("ProcezzManagementType not found",
					new Exception("No ProcezzManagementType found for the passed key: " + identifier));
		} else {
			return managementTypes.get(possibleKey);
		}

	}

	public static List<ProcezzManagementType> getAllProcezzManagementTypes() {

		createManagementTypes();

		final List<ProcezzManagementType> list = new ArrayList<>();
		list.addAll(managementTypes.values());

		return list;

	}

}
