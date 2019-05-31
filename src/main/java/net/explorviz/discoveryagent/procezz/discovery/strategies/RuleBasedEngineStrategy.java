package net.explorviz.discoveryagent.procezz.discovery.strategies;

import java.io.FileNotFoundException;
import java.io.FileReader;
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
import org.jeasy.rules.mvel.MVELRuleFactory;
import org.jeasy.rules.support.YamlRuleDefinitionReader;

public class RuleBasedEngineStrategy implements DiscoveryStrategy {

  // Configuration of RuleEngine
  private final RulesEngineParameters parameters =
      new RulesEngineParameters().skipOnFirstAppliedRule(true);
  private Facts facts;
  private Rules rules;
  @Config("watch.timer")
  private final Timer updateTimer;
  private boolean exec = false;


  public boolean isExec() {
    return exec;
  }

  public void setExec(final boolean exec) {
    this.exec = exec;
  }


  public RuleBasedEngineStrategy() {
    updateTimer = new Timer(true);
    final UpdateRuleListService service = new UpdateRuleListService(this);

    // updateTimer.scheduleAtFixedRate(service, 0, time);

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
    exec = false;
    // Create Facts
    facts = new Facts();
    facts.put("processInfo", new InfoContainer(this, newProcezz));
    // System.out.println(newProcezz.getOsExecutionCommand());
    final MVELRuleFactory ruleFactory = new MVELRuleFactory(new YamlRuleDefinitionReader());
    // System.out.println(newProcezz.getId());

    try {
      rules = ruleFactory.createRules(new FileReader("rules2.yml"));
    } catch (final FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


    final RulesEngine rulesEngine = new DefaultRulesEngine(parameters);
    rulesEngine.fire(rules, facts);

    System.out.println("YEAH YEAH: " + exec);

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
    synchronized (this.rules) {
      this.rules = rules;
    }
  }

}
