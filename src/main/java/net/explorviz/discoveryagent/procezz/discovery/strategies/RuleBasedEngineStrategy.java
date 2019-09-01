package net.explorviz.discoveryagent.procezz.discovery.strategies;

import java.util.Timer;
import javax.inject.Inject;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategy;
import net.explorviz.discoveryagent.procezz.discovery.strategies.util.ExecObject;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.discoveryagent.services.UpdateRuleListService;
import net.explorviz.shared.config.annotations.Config;
import net.explorviz.shared.discovery.model.Procezz;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.core.RulesEngineParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategy based on rule based engine.
 */
public final class RuleBasedEngineStrategy implements DiscoveryStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(RuleBasedEngineStrategy.class);
  private static final String HTTPBASE = "http://";
  // Configuration of RuleEngine
  private final RulesEngineParameters parameters =
      new RulesEngineParameters().skipOnFirstAppliedRule(true);

  // private final RulesListenerExtend ruleListener = new RulesListenerExtend();

  // Parts of the rule based Engine
  private Rules rules = new Rules();

  @Config("updateIP")
  private String ip;

  @Config("updatePort")
  private String port;

  @Config("updateRulesTimeRate")
  private int timer;

  @Config("updateURL")
  private String url;


  private final MonitoringFilesystemService fileSystem;


  @Inject
  public RuleBasedEngineStrategy(final MonitoringFilesystemService fileSystem) {

    this.fileSystem = fileSystem;
  }

  /**
   * Starts sending requests for receiving rules from the UpdateService the rules.
   */

  public void startRuleFetch() {


    final String urlComplete = HTTPBASE + ip + ":" + port + "/" + url;
    final Timer updateTimer = new Timer(true);
    final UpdateRuleListService service = new UpdateRuleListService(this, urlComplete);
    updateTimer.scheduleAtFixedRate(service, 0, timer);
  }



  @Override
  public boolean isDesiredApplication(final Procezz newProcezz) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void detectAndSetName(final Procezz newProcezz) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean applyEntireStrategy(final Procezz newProcezz) {
    if (rules == null || rules.isEmpty()) {
      return false;
    }
    // Create Facts
    final Facts facts = new Facts();
    facts.put("processInfo", newProcezz);
    facts.put("updateExec", new ExecObject(fileSystem));
    final DefaultRulesEngine rulesEngine = new DefaultRulesEngine(parameters);
    // rulesEngine.registerRuleListener(ruleListener);
    boolean check = false;
    synchronized (rules) {
      // Check for firing rules.
      check = rulesEngine.check(rules, facts).containsValue(true);
      if (check) {
        // Fire.
        rulesEngine.fire(rules, facts);
      }
    }
    return check;

  }

  @Override
  public void detectAndSetProposedExecCMD(final Procezz newProcezz) {
    // TODO Auto-generated method stub

  }

  @Override
  public void detectAndSetProperties(final Procezz newProcezz) {
    // TODO Auto-generated method stub

  }

  public void updateRuleList(final Rules ruleList) {
    synchronized (rules) {
      rules = ruleList;
    }

  }

  /**
   * Method for test purposes.
   *
   * @param ruleList Got set.
   */
  public void updateRuleListTest(final Rules ruleList) {
    rules = ruleList;
  }

}
