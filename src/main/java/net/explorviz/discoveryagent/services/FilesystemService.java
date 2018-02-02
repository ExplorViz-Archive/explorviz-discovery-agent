package net.explorviz.discoveryagent.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.servlet.ServletContext;

import net.explorviz.discovery.model.Procezz;

public final class FilesystemService {

	public static ServletContext servletContext;

	public static final String MONITORING_CONFIGS_FOLDER_NAME = "/monitoring-configurations";
	public static Path configsPath;

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

	public static void createConfigFolderForProcezz(final Procezz procezz) throws IOException {

		createIfNotExistsMonitoringConfigsFolder();

		final String folderOfPassedIDString = configsPath + "/" + procezz.getId();

		final File folderOfPassedID = new File(folderOfPassedIDString);

		if (!folderOfPassedID.exists()) {
			Files.createDirectory(Paths.get(folderOfPassedIDString));
		}

		final String configPathString = servletContext.getResource("/WEB-INF/kieker/kieker.monitoring.properties")
				.getPath();
		final String aopPathString = servletContext.getResource("/WEB-INF/kieker/aop.xml").getPath();

		final Path sourceKiekerConfigPath = Paths.get(configPathString);
		final Path sourceAOPPath = Paths.get(aopPathString);

		final Path targetKiekerConfigPath = Paths
				.get(folderOfPassedIDString + "/" + sourceKiekerConfigPath.getFileName());
		final Path targetAOPPath = Paths.get(folderOfPassedIDString + "/" + sourceAOPPath.getFileName());

		if (!targetKiekerConfigPath.toFile().exists()) {
			Files.copy(sourceKiekerConfigPath,
					Paths.get(folderOfPassedIDString + "/" + sourceKiekerConfigPath.getFileName()));
		}

		if (!targetAOPPath.toFile().exists()) {
			Files.copy(sourceAOPPath, Paths.get(folderOfPassedIDString + "/" + sourceAOPPath.getFileName()));
		}

		final String aopFileContent = new String(Files.readAllBytes(targetAOPPath));

		final String kiekerConfigFileContent = new String(Files.readAllBytes(targetKiekerConfigPath));

		procezz.setAopContent(aopFileContent);
		procezz.setKiekerConfigContent(kiekerConfigFileContent);

	}

	public static void updateAOPFileContentForProcezz(final Procezz procezz) throws IOException {
		final String folderOfPassedIDString = configsPath + "/" + procezz.getId();
		final Path aopPath = Paths.get(folderOfPassedIDString + "/aop.xml");

		Files.write(aopPath, procezz.getAopContent().getBytes());
	}

	public static void removeMonitoringConfigs() throws IOException {

		final String monitoringConfigString = servletContext.getResource("/WEB-INF" + MONITORING_CONFIGS_FOLDER_NAME)
				.getPath();
		final Path monitoringConfigDir = Paths.get(monitoringConfigString);

		Files.walkFileTree(monitoringConfigDir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

		});

	}

}
