package net.explorviz.discoveryagent.services;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import net.explorviz.shared.config.annotations.Config;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzMonitoringSettingsException;
import net.explorviz.shared.discovery.model.Procezz;

public final class MonitoringFilesystemService {

  private static final String MONITORING_CONFIGS_FOLDER_NAME = "monitoring-configurations";
  private static final String MONITORING_DEFAULT_CONF_PATH = "kieker";
  private static final String KIEKER_APP_NAME_PROPERTY = "kieker.monitoring.applicationName=";
  private static final String KIEKER_HOSTNAME_PROPERTY = "kieker.monitoring.hostname=";
  private static final String KIEKER_TCP_HOSTNAME_PROPERTY =
      "kieker.monitoring.writer.tcp.SingleSocketTcpWriter.hostname=";
  private static final String KIEKER_PROPS_FILENAME = "kieker.monitoring.properties";
  private static final String AOP_PROPS_FILENAME = "aop.xml";
  private static final String KIEKER_JAR_FILENAME = "kieker-1.14-SNAPSHOT-aspectj.jar";

  private Path configsPath;

  @Config("backendIP")
  private String backendIp;

  private static MonitoringFilesystemService fileSys;

  public void createMonitoringConfigsFolder() throws IOException {

    // Create temporary folder in temp directory of this OS
    final Path tempPathToDir = Files.createTempDirectory("explorviz-discovery-agent");

    final File tempDir = tempPathToDir.toFile();

    final String configsFolderPath = tempDir + File.separator + MONITORING_CONFIGS_FOLDER_NAME;

    configsPath = Files.createDirectory(Paths.get(configsFolderPath));


    copyDefaultKiekerProperties();
    updateDefaultKiekerProperties();
    fileSys = this;
    tempDir.deleteOnExit();
  }

  public static MonitoringFilesystemService getref() {
    return fileSys;
  }

  private void copyDefaultKiekerProperties() throws IOException {

    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    final URL urlToDefaultKiekerProps =
        classLoader.getResource(MONITORING_DEFAULT_CONF_PATH + "/" + KIEKER_PROPS_FILENAME);


    final URL urlToDefaultAopProps =
        classLoader.getResource(MONITORING_DEFAULT_CONF_PATH + "/" + AOP_PROPS_FILENAME);

    final URL urlToDefaultKiekerJar =
        classLoader.getResource(MONITORING_DEFAULT_CONF_PATH + "/" + KIEKER_JAR_FILENAME);
    Path kiekerDefaultConfigPath;
    try {
      kiekerDefaultConfigPath = Paths.get(urlToDefaultKiekerProps.toURI());
      /*
       * System.out.println("From classloader: " + urlToDefaultKiekerProps); System.out.println(
       * "Paths with uri + to file: " + Paths.get(urlToDefaultKiekerProps.toURI()).toFile());
       * System.out.println("getURI output: " + urlToDefaultKiekerProps.toURI());
       * System.out.println("getFile output: " + urlToDefaultKiekerProps.getFile()); System.out
       * .println(Paths.get(configsPath.toString() + File.separator + KIEKER_PROPS_FILENAME));
       */
      Files.copy(kiekerDefaultConfigPath,
          Paths.get(configsPath.toString() + File.separator + KIEKER_PROPS_FILENAME));
    } catch (final URISyntaxException e) {
      e.printStackTrace();
    }
    Path kiekerDefaultAopPath;
    try {
      kiekerDefaultAopPath = Paths.get(urlToDefaultAopProps.toURI());

      Files.copy(kiekerDefaultAopPath,
          Paths.get(configsPath.toString() + File.separator + AOP_PROPS_FILENAME));
    } catch (final URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Path kiekerDefaultJarPath;
    try {
      kiekerDefaultJarPath = Paths.get(urlToDefaultKiekerJar.toURI());
      Files.copy(kiekerDefaultJarPath,
          Paths.get(configsPath.toString() + File.separator + KIEKER_JAR_FILENAME));
    } catch (final URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }



  }

  private void updateDefaultKiekerProperties() throws IOException {
    final Path kiekerConfigPath =
        Paths.get(configsPath.toString() + File.separator + KIEKER_PROPS_FILENAME);

    final List<String> kiekerConfigNewContent = Files.lines(kiekerConfigPath).map(line -> {
      if (line.startsWith(KIEKER_TCP_HOSTNAME_PROPERTY)) {
        return KIEKER_TCP_HOSTNAME_PROPERTY + backendIp;
      } else {
        return line;
      }
    }).collect(Collectors.toList());

    Files.write(kiekerConfigPath, kiekerConfigNewContent);
  }

  public void createConfigFolderForProcezz(final Procezz procezz) throws IOException {

    final String folderOfPassedIdString = configsPath.toString() + File.separator + procezz.getId();

    final File folderOfPassedId = new File(folderOfPassedIdString);

    if (!folderOfPassedId.exists()) {
      Files.createDirectory(Paths.get(folderOfPassedIdString));
    }

    final String configPathString = configsPath.toString() + File.separator + KIEKER_PROPS_FILENAME;

    final String aopPathString = configsPath.toString() + File.separator + AOP_PROPS_FILENAME;

    final Path sourceKiekerConfigPath = Paths.get(configPathString);
    final Path sourceAopPath = Paths.get(aopPathString);

    final Path targetKiekerConfigPath =
        Paths.get(folderOfPassedIdString + File.separator + sourceKiekerConfigPath.getFileName());
    final Path targetAopPath =
        Paths.get(folderOfPassedIdString + File.separator + sourceAopPath.getFileName());

    if (!targetKiekerConfigPath.toFile().exists()) {
      Files.copy(sourceKiekerConfigPath, Paths
          .get(folderOfPassedIdString + File.separator + sourceKiekerConfigPath.getFileName()));
    }

    if (!targetAopPath.toFile().exists()) {
      Files.copy(sourceAopPath,
          Paths.get(folderOfPassedIdString + File.separator + sourceAopPath.getFileName()));
    }

    final String aopFileContent = new String(Files.readAllBytes(targetAopPath));

    final String kiekerConfigFileContent = new String(Files.readAllBytes(targetKiekerConfigPath));

    procezz.setAopContent(aopFileContent);
    procezz.setKiekerConfigContent(kiekerConfigFileContent);

  }

  public void updateAopFileContentForProcezz(final Procezz procezz)
      throws ProcezzMonitoringSettingsException {
    final String folderOfPassedIdString = configsPath + File.separator + procezz.getId();
    final Path aopPath = Paths.get(folderOfPassedIdString + File.separator + "aop.xml");


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
    final String folderOfPassedIdString = configsPath + File.separator + procezzInCache.getId();
    final Path kiekerConfigPath =
        Paths.get(folderOfPassedIdString + File.separator + "kieker.monitoring.properties");

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

  public String getKiekerJarPath() {
    return configsPath.toAbsolutePath().toString() + File.separator + KIEKER_JAR_FILENAME;
  }

  public String getKiekerConfigPath() {
    return configsPath.toAbsolutePath().toString() + File.separator + KIEKER_PROPS_FILENAME;
  }

  public String getKiekerConfigPathForProcezzID(final String entityID) {
    return configsPath.toAbsolutePath().toString() + File.separator + entityID + File.separator
        + KIEKER_PROPS_FILENAME;
  }

  public String getAopConfigPath() {
    return configsPath.toAbsolutePath().toString() + File.separator + AOP_PROPS_FILENAME;
  }

  public String getAopConfigPathForProcezzID(final String entityID) {
    return configsPath.toAbsolutePath().toString() + File.separator + entityID + File.separator
        + AOP_PROPS_FILENAME;
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
