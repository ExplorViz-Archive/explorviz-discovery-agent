package net.explorviz.discoveryagent.services;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import net.explorviz.discovery.exceptions.procezz.ProcezzMonitoringSettingsException;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discovery.services.PropertyService;

public final class MonitoringFilesystemService {

  public static final String MONITORING_CONFIGS_FOLDER_NAME = "monitoring-configurations";
  private static final String MONITORING_DEFAULT_CONF_PATH = "kieker";
  private static final String KIEKER_APP_NAME_PROPERTY = "kieker.monitoring.applicationName=";
  private static final String KIEKER_HOSTNAME_PROPERTY = "kieker.monitoring.hostname=";
  private static final String KIEKER_TCP_HOSTNAME_PROPERTY =
      "kieker.monitoring.writer.tcp.SingleSocketTcpWriter.hostname=";
  private static final String KIEKER_FILENAME = "kieker.monitoring.properties";
  private static final String AOP_FILENAME = "aop.xml";

  private Path configsPath;

  public void createMonitoringConfigsFolder() throws IOException {

    // Create temporary folder in temp directory of this OS

    final Path tempPathToDir = Files.createTempDirectory("explorviz-discovery-agent");

    final File tempDir = tempPathToDir.toFile();

    final String configsFolderPath = tempDir + File.separator + MONITORING_CONFIGS_FOLDER_NAME;

    configsPath = Files.createDirectory(Paths.get(configsFolderPath));

    copyDefaultKiekerProperties();
    updateDefaultKiekerProperties();

    tempDir.deleteOnExit();
  }

  private void copyDefaultKiekerProperties() throws IOException {

    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    final URL urlToDefaultKiekerProps =
        classLoader.getResource(MONITORING_DEFAULT_CONF_PATH + File.separator + KIEKER_FILENAME);

    final URL urlToDefaultAopProps =
        classLoader.getResource(MONITORING_DEFAULT_CONF_PATH + File.separator + AOP_FILENAME);

    final Path kiekerDefaultConfigPath = Paths.get(urlToDefaultKiekerProps.getFile());
    final Path kiekerDefaultAopPath = Paths.get(urlToDefaultAopProps.getFile());

    Files.copy(kiekerDefaultConfigPath,
        Paths.get(configsPath.toString() + File.separator + KIEKER_FILENAME));
    Files.copy(kiekerDefaultAopPath,
        Paths.get(configsPath.toString() + File.separator + AOP_FILENAME));

  }

  private void updateDefaultKiekerProperties() throws IOException {
    final Path kiekerConfigPath =
        Paths.get(configsPath.toString() + File.separator + KIEKER_FILENAME);

    final String backendUrl = PropertyService.getStringProperty("backendIP");

    final List<String> kiekerConfigNewContent = Files.lines(kiekerConfigPath).map(line -> {
      if (line.startsWith(KIEKER_TCP_HOSTNAME_PROPERTY)) {
        return KIEKER_TCP_HOSTNAME_PROPERTY + backendUrl;
      } else {
        return line;
      }
    }).collect(Collectors.toList());

    Files.write(kiekerConfigPath, kiekerConfigNewContent);
  }

  public void createConfigFolderForProcezz(final Procezz procezz) throws IOException {

    createMonitoringConfigsFolder();

    final String folderOfPassedIdString = configsPath + "/" + procezz.getId();

    final File folderOfPassedId = new File(folderOfPassedIdString);

    if (!folderOfPassedId.exists()) {
      Files.createDirectory(Paths.get(folderOfPassedIdString));
    }

    final String configPathString = configsPath + File.separator + KIEKER_FILENAME;

    final String aopPathString = configsPath + File.separator + AOP_FILENAME;

    final Path sourceKiekerConfigPath = Paths.get(configPathString);
    final Path sourceAopPath = Paths.get(aopPathString);

    final Path targetKiekerConfigPath =
        Paths.get(folderOfPassedIdString + "/" + sourceKiekerConfigPath.getFileName());
    final Path targetAopPath =
        Paths.get(folderOfPassedIdString + "/" + sourceAopPath.getFileName());

    if (!targetKiekerConfigPath.toFile().exists()) {
      Files.copy(sourceKiekerConfigPath,
          Paths.get(folderOfPassedIdString + "/" + sourceKiekerConfigPath.getFileName()));
    }

    if (!targetAopPath.toFile().exists()) {
      Files.copy(sourceAopPath,
          Paths.get(folderOfPassedIdString + "/" + sourceAopPath.getFileName()));
    }

    final String aopFileContent = new String(Files.readAllBytes(targetAopPath));

    final String kiekerConfigFileContent = new String(Files.readAllBytes(targetKiekerConfigPath));

    procezz.setAopContent(aopFileContent);
    procezz.setKiekerConfigContent(kiekerConfigFileContent);

  }

  public void updateAopFileContentForProcezz(final Procezz procezz)
      throws ProcezzMonitoringSettingsException {
    final String folderOfPassedIdString = configsPath + "/" + procezz.getId();
    final Path aopPath = Paths.get(folderOfPassedIdString + "/aop.xml");

    try {
      Files.write(aopPath, procezz.getAopContent().getBytes());
    } catch (final IOException e) {
      throw new ProcezzMonitoringSettingsException(
          "There was an error while updating the aop.xml for the passed procezz (ID: "
              + procezz.getId() + ")",
          e, procezz);
    }
  }

  public void updateKiekerConfigForProcezz(final Procezz procezzInCache, final String hostname)
      throws ProcezzMonitoringSettingsException {
    final String folderOfPassedIdString = configsPath + "/" + procezzInCache.getId();
    final Path kiekerConfigPath =
        Paths.get(folderOfPassedIdString + "/kieker.monitoring.properties");

    final String appName =
        procezzInCache.getName() == null ? String.valueOf(procezzInCache.getPid())
            : procezzInCache.getName();
    try {

      final List<String> kiekerConfigNewContent = Files.lines(kiekerConfigPath).map(line -> {
        if (line.startsWith(KIEKER_APP_NAME_PROPERTY)) {
          return KIEKER_APP_NAME_PROPERTY + appName;
        } else if (line.startsWith(KIEKER_HOSTNAME_PROPERTY)) {
          return KIEKER_HOSTNAME_PROPERTY + hostname;
        } else {
          return line;
        }
      }).collect(Collectors.toList());

      Files.write(kiekerConfigPath, kiekerConfigNewContent);

    } catch (final IOException e) {
      throw new ProcezzMonitoringSettingsException(
          "There was an error while updating the kieker.config for the passed procezz (ID: "
              + procezzInCache.getId() + ")",
          e, procezzInCache);
    }
  }

  /*
   * public void removeIfExistsMonitoringConfigs() throws IOException {
   *
   * final Path monitoringConfigDir = Paths.get(MONITORING_CONFIGS_FOLDER_NAME);
   *
   * if (!Files.exists(monitoringConfigDir)) { return; }
   *
   * Files.walkFileTree(monitoringConfigDir, new SimpleFileVisitor<Path>() {
   *
   * @Override public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
   * throws IOException { Files.delete(file); return FileVisitResult.CONTINUE; }
   *
   * @Override public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
   * throws IOException { Files.delete(dir); return FileVisitResult.CONTINUE; }
   *
   * });
   *
   *
   * }
   */
}
