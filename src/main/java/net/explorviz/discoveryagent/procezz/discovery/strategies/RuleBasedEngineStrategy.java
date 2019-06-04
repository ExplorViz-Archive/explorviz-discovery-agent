package net.explorviz.discoveryagent.procezz.discovery.strategies;

import java.util.Timer;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategy;
import net.explorviz.discoveryagent.services.UpdateRuleListService;
import net.explorviz.shared.config.annotations.Config;
import net.explorviz.shared.discovery.model.Procezz;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.core.RulesEngineParameters;

public class RuleBasedEngineStrategy implements DiscoveryStrategy {

  // Configuration of RuleEngine
  private final RulesEngineParameters parameters =
      new RulesEngineParameters().skipOnFirstAppliedRule(true);
  private Facts facts;
  private Rules rules;

  private final Timer updateTimer;
  private boolean exec = false;

  @Config("updateRulesTimeRate")
  private final int timer = 15000;


  public boolean isExec() {
    return exec;
  }

  public void setExec(final boolean exec) {
    this.exec = exec;
  }


  public RuleBasedEngineStrategy() {
    updateTimer = new Timer(true);
    final UpdateRuleListService service = new UpdateRuleListService(this);

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
    exec = false;
    // Create Facts
    facts = new Facts();
    facts.put("processInfo", new InfoContainer(this, newProcezz));

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
