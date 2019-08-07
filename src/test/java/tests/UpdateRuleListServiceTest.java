package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import net.explorviz.discoveryagent.procezz.discovery.strategies.RuleBasedEngineStrategy;
import net.explorviz.discoveryagent.services.UpdateRuleListService;
import org.jeasy.rules.api.Rules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class UpdateRuleListServiceTest {
  String testStringValid =
      "{\"data\":{\"type\":\"rulelistholder\",\"id\":\"dummy-53-4\",\"attributes\":{\"ruleList\":[{\"name\":\"ruletest2\",\"description\":\"when age is greater then 18, then mark as adult\",\"priority\":1,\"condition\":\"procesfo.getOsExecutionCommand().toLowerCase().contains(\\\"sample\\\");\",\"actions\":[\"execObj.getStrat().setExec(true);\",\"System.out.println(\\\" Give me money \\\");\"]}]}}}";


  String testStringInvAPI =
      "{\"daa\":{\"type\":\"rulelistholder\",\"id\":\"dummy-53-4\",\"attributes\":{\"ruleList\":[{\"name\":\"ruletest2\",\"description\":\"when age is greater then 18, then mark as adult\",\"priority\":1,\"condition\":\"procesfo.getOsExecutionCommand().toLowerCase().contains(\\\"sample\\\");\",\"actions\":[\"execObj.getStrat().setExec(true);\",\"System.out.println(\\\" Give me money \\\");\"]}]}}}";


  String testStringInvJSON =
      "{\"data\":{\"type\":\"rulelistholder\",\"id\"\"dummy-53-4\",\"attributes\":{\"ruleList\":[{\"name\":\"ruletest2\",\"description\":\"when age is greater then 18, then mark as adult\",\"priority\":1,\"condition\":\"procesfo.getOsExecutionCommand().toLowerCase().contains(\\\"sample\\\");\",\"actions\":[\"execObj.getStrat().setExec(true);\",\"System.out.println(\\\" Give me money \\\");\"]}]}}}";

  String testStringInvRule =
      "{\"data\":{\"type\":\"rulelistholder\",\"id\":\"dummy-53-4\",\"attributes\":{\"ruleList\":[{\" \":,\"description\":\"when age is greater then 18, then mark as adult\",\"priority\":1,\"condition\":\"procesfo.getOsExecutionCommand().toLowerCase().contains(\\\"sample\\\");\",\"actions\":[\"execObj.getStrat().setExec(true);\",\"System.out.println(\\\" Give me money \\\");\"]}]}}}";

  UpdateRuleListService service;

  @Mock
  RuleBasedEngineStrategy strat;

  @BeforeEach
  public void setUp() {
    service = new UpdateRuleListService(strat, "bla");
  }

  @Test
  public void validRule() {
    final Rules rules = service.stringToRules(testStringValid);
    rules.forEach(rule -> assertEquals("ruletest2", rule.getName()));
  }

  @Test
  public void invalidRuleJSONAPI() {
    assertNull(service.stringToRules(testStringInvAPI));


  }

  @Test
  public void invJSON() {
    assertNull(service.stringToRules(testStringInvJSON));

  }

  @Test
  public void invRule() {
    assertNull(service.stringToRules(testStringInvRule));

  }
}
