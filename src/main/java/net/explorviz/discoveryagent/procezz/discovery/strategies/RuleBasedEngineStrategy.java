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
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.core.RulesEngineParameters;

public final class RuleBasedEngineStrategy implements DiscoveryStrategy {

  String url;
  // Configuration of RuleEngine
  private final RulesEngineParameters parameters =
      new RulesEngineParameters().skipOnFirstAppliedRule(true);
  private Facts facts;
  private Rules rules;
  private Timer updateTimer;
  private boolean exec = false;
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

  public boolean isExec() {
    return exec;
  }

  public void setExec(final boolean exec) {
    this.exec = exec;
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
    exec = false;
    // Create Facts
    facts = new Facts();
    facts.put("processInfo", newProcezz);
    facts.put("updateExec", new ExecObject(this, fileSystem));

    final RulesEngine rulesEngine = new DefaultRulesEngine(parameters);
    synchronized (rules) {
      rulesEngine.fire(rules, facts);
    }
    return exec;

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
