package net.explorviz.discoveryagent.procezz.discovery.strategies;

import java.util.Map;
import java.util.Timer;
import javax.inject.Inject;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategy;
import net.explorviz.discoveryagent.procezz.discovery.strategies.util.ExecObject;
import net.explorviz.discoveryagent.procezz.discovery.strategies.util.RulesListenerExtend;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.discoveryagent.services.UpdateRuleListService;
import net.explorviz.shared.config.annotations.Config;
import net.explorviz.shared.discovery.model.Procezz;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.core.RulesEngineParameters;

public final class RuleBasedEngineStrategy implements DiscoveryStrategy {

  private String url;
  // Configuration of RuleEngine
  private final RulesEngineParameters parameters =
      new RulesEngineParameters().skipOnFirstAppliedRule(true);
  final RulesListenerExtend ruleListener = new RulesListenerExtend();

  // Parts of the rule based Engine
  private Facts facts;
  private Rules rules;

  private Timer updateTimer;
  private static final String httpBase = "http://";
  @Config("updateIP")
  private String iP;

  @Config("updatePort")
  private String port;

  @Config("updateRulesTimeRate")
  private int timer;

  @Config("updateURL")
  private String uRL;


  private final MonitoringFilesystemService fileSystem;

  @Inject
  public RuleBasedEngineStrategy(final MonitoringFilesystemService fileSystem) {
    this.fileSystem = fileSystem;
  }

  /**
   * Starts the updateService for the rules.
   */

  public void startRuleFetch() {
    url = httpBase + iP + ":" + port + "/" + uRL;
    System.out.println(url);
    updateTimer = new Timer(true);
    final UpdateRuleListService service = new UpdateRuleListService(this, url);
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
    facts = new Facts();
    facts.put("processInfo", newProcezz);
    facts.put("updateExec", new ExecObject(fileSystem));
    final DefaultRulesEngine rulesEngine = new DefaultRulesEngine(parameters);
    rulesEngine.registerRuleListener(ruleListener);
    boolean check = false;
    synchronized (rules) {
      final Map<org.jeasy.rules.api.Rule, Boolean> checkUp = rulesEngine.check(rules, facts);
      check = checkUp.containsValue(true);
      System.out
          .println("The check val is: " + check + " for " + newProcezz.getOsExecutionCommand());
      rulesEngine.fire(rules, facts);
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

  public void updateRuleList(final Rules rules) {
    this.rules = rules;
  }

}
