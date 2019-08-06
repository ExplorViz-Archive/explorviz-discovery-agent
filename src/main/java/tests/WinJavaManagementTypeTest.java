package tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.File;
import java.util.Locale;
import net.explorviz.discoveryagent.procezz.management.types.WinJavaManagementType;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.shared.discovery.model.Agent;
import net.explorviz.shared.discovery.model.Procezz;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WinJavaManagementTypeTest {
  private static final String CORRECT_CMD =
      "C:\\Program Files\\java.exe -cp . -jar sampleApplication.jar";


  private static final String KIEKER_PROPS_FILENAME = "kieker.monitoring.properties";
  private static final String AOP_PROPS_FILENAME = "aop.xml";
  private static final String KIEKER_JAR_FILENAME = "kieker-1.14-SNAPSHOT-aspectj.jar";
  private static final String path = "tmp\\test";

  @Mock
  Agent agent;

  @Mock
  MonitoringFilesystemService service;


  Procezz procezz = new Procezz(1, CORRECT_CMD);

  WinJavaManagementType type;



  @BeforeEach
  public void setUp() {
    type = new WinJavaManagementType(service);

    procezz = new Procezz(1, CORRECT_CMD);
    procezz.setId("1");
    procezz.setAgentExecutionCommand(null);
  }

  public boolean checkOs(final String os) {

    return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains(os);
  }

  @Test
  public void testIdentInject() {
    if (checkOs("windows")) {
      try {
        type.injectProcezzIdentificationProperty(procezz);
      } catch (final ProcezzStartException e) {
        fail("Cant be a correct java-process");
      }
      assertEquals(
          "C:\\Program Files\\java.exe -Dexplorviz.agent.model.id=1 -cp . -jar sampleApplication.jar",
          procezz.getAgentExecutionCommand());
    }
  }

  @Test
  public void testCorrectSplit() {
    if (checkOs("windows")) {
      final String[] splitted = type.splitter(CORRECT_CMD);
      final String[] expected =
          {"C:\\Program Files\\java.exe", " -cp . -jar sampleApplication.jar"};
      assertArrayEquals(splitted, expected);
    }
  }

  @Test
  public void testIdentRemove() {
    if (checkOs("windows")) {
      try {
        type.injectProcezzIdentificationProperty(procezz);
        type.removeMonitoringAgentInProcezz(procezz);
        assertEquals("C:\\Program Files\\java.exe -cp . -jar sampleApplication.jar",
            procezz.getAgentExecutionCommand());
      } catch (final ProcezzStartException e) {
        fail("Cant be a correct java-process");
      }
    }
  }

  @Test
  public void testMonitInject() {
    if (checkOs("windows")) {
      Mockito.when(service.getKiekerJarPath())
          .thenReturn(path + File.separator + KIEKER_JAR_FILENAME);
      Mockito.when(service.getKiekerConfigPathForProcezzID("1"))
          .thenReturn(path + File.separator + "1" + File.separator + KIEKER_PROPS_FILENAME);
      Mockito.when(service.getAopConfigPathForProcezzID("1"))
          .thenReturn(path + File.separator + "1" + File.separator + AOP_PROPS_FILENAME);
      // Mockito.when(service.getAopConfigPathForProcezzID("1")).thenReturn(value)
      try {
        type.injectMonitoringAgentInProcezz(procezz);
        assertEquals(procezz.getAgentExecutionCommand(),
            "C:\\Program Files\\java.exe -javaagent:tmp\\test\\kieker-1.14-SNAPSHOT-aspectj.jar -Dkieker.monitoring.configuration=tmp\\test\\1\\kieker.monitoring.properties -Dorg.aspectj.weaver.loadtime.configuration=file://tmp\\test\\1\\aop.xml -Dkieker.monitoring.skipDefaultAOPConfiguration=true -Dexplorviz.agent.model.id=1 -cp . -jar sampleApplication.jar");
      } catch (final ProcezzStartException e) {
        fail("Failed to create injected CMD for kieker.");
      }
    }
  }
}
