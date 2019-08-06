package tests;

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import net.explorviz.discoveryagent.procezz.management.types.util.WinAbstraction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


/**
 * Check path of sampleApplication
 * 
 * @author enes
 *
 */
@ExtendWith(MockitoExtension.class)
class WinAbstractionTest {


  public boolean checkOs(final String os) {

    return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains(os);
  }

  @Test
  void test() {
    if (checkOs("windows")) {
      try {
        final Map<Long, String> map = WinAbstraction.findProzzeses();

        map.forEach((id, cmd) -> {
          if (!cmd.toLowerCase(Locale.ENGLISH).contains("java")) {
            fail("Not expected to contain a not-Java processes in the list.");
          }
        });

      } catch (final IOException e) {
        fail("Could not create list of processes.");
      }
    }
  }

  @Test
  public void startAndKillTestSucc() {
    if (checkOs("windows")) {
      try {
        WinAbstraction.startProcessCmd("java -cp . -jar sampleApplication.jar");
      } catch (final IOException e) {
        fail("Failed to start process");
      }

      try {
        WinAbstraction.findProzzeses().forEach((pid, cmd) -> {
          if (cmd.contains("sample")) {
            WinAbstraction.killProcessPid(pid);
            // assertTrue(true);
          }
        });
      } catch (final IOException e) {
        fail("Failed to start process");
      }
    }
  }

  @Test
  public void startTestFail() {
    if (checkOs("windows")) {

      try {
        WinAbstraction.startProcessCmd("ja -cp . -jar sampleApplication.jar");
      } catch (final IOException e) {
        // assertTrue(true);
      }
      try {
        WinAbstraction.findProzzeses().forEach((pid, cmd) -> {
          if (cmd.contains("sample")) {
            fail("Process runs despite faulty cmd");
          }
        });
      } catch (final IOException e) {

      }

    }
  }


}
