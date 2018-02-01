package net.explorviz.discoveryagent.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

public final class FilesystemService {

	public static ServletContext servletContext;

	private static final String MONITORING_CONFIGS_FOLDER_NAME = "/monitoring-configurations";
	private static Path configsPath;

	private FilesystemService() {
		// no need to instantiate
	}

	public static void createIfNotExistsMonitoringConfigsFolder() throws IOException {

		final String webINFFolder = servletContext.getResource("/WEB-INF").getPath();
		final String configsFolderPath = webINFFolder + MONITORING_CONFIGS_FOLDER_NAME;

		final File folderToCreate = new File(configsFolderPath);

		if (!folderToCreate.exists()) {
			configsPath = Files.createDirectory(Paths.get(configsFolderPath));
		}

		configsPath = Paths.get(configsFolderPath);
	}

	public static void createSubfolderForID(final long id) throws IOException {

		createIfNotExistsMonitoringConfigsFolder();

		final String folderOfPassedIDString = configsPath + "/" + id;

		final File folderOfPassedID = new File(folderOfPassedIDString);

		if (!folderOfPassedID.exists()) {
			Files.createDirectory(Paths.get(folderOfPassedIDString));
		}

		final String configPathString = servletContext.getResource("/WEB-INF/kieker/kieker.monitoring.properties")
				.getPath();
		final String aopPathString = servletContext.getResource("/WEB-INF/kieker/aop.xml").getPath();

		final Path configPath = Paths.get(configPathString);
		final Path aopPath = Paths.get(aopPathString);

		Files.copy(configPath, Paths.get(folderOfPassedIDString + "/" + configPath.getFileName()));
		Files.copy(aopPath, Paths.get(folderOfPassedIDString + "/" + aopPath.getFileName()));

	}

}
