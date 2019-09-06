package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.explorviz.discoveryagent.procezz.discovery.strategies.RuleBasedEngineStrategy;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.shared.discovery.model.Procezz;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.core.RuleBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RuleBasedEngineStrategyTest {
  private static final String CORRECT_CMD =
      "\"C:\\Program Files\\java.exe\" -cp . -jar sampleApplication.jar";

  private RuleBasedEngineStrategy strat;

  private Procezz procezz;
  @Mock
  private MonitoringFilesystemService service;

  @BeforeEach
  public void setUp() {

    strat = new RuleBasedEngineStrategy(service);

    procezz = new Procezz(1, CORRECT_CMD);
    procezz.setId("1");
    procezz.setAgentExecutionCommand(null);
    strat.updateRuleListTest(null);
  }

  @Test
  public void setPropAndAOP() {
    final Rules rules = new Rules();

    final Rule rule = new RuleBuilder().name("test").description("checks cmd")
        .when(facts -> facts.get("processInfo").equals(procezz))
        .then(facts -> procezz.setProposedExecutionCommand("Prop"))
        .then(facts -> procezz.setAopContent("newAOP")).build();
    rules.register(rule);
    strat.updateRuleListTest(rules);
    assertTrue(strat.applyEntireStrategy(procezz));

    assertEquals("Prop", procezz.getProposedExecutionCommand());
    assertEquals("newAOP", procezz.getAopContent());
  }

  @Test
  public void emptyRule() {
    strat.updateRuleListTest(null);
    assertFalse(strat.applyEntireStrategy(procezz));
  }

  @Test
  public void ruleFail() {
    final Rules rules = new Rules();

    final Rule rule = new RuleBuilder().name("test").description("checks cmd")
        .when(facts -> facts.get("processInfo").equals(service))
        .then(facts -> procezz.setProposedExecutionCommand("Prop"))
        .then(facts -> procezz.setAopContent("newAOP")).build();
    rules.register(rule);
    strat.updateRuleListTest(rules);
    assertFalse(strat.applyEntireStrategy(procezz));

  }
}
