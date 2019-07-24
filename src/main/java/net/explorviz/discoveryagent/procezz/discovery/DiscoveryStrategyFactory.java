package net.explorviz.discoveryagent.procezz.discovery;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.explorviz.discoveryagent.procezz.discovery.strategies.RuleBasedEngineStrategy;


public final class DiscoveryStrategyFactory {

  public static final String USE_OS_FLAG = "Use-OS-Exec-CMD";

  private static List<DiscoveryStrategy> strategies = new ArrayList<>();


  private final RuleBasedEngineStrategy ruleStrat;


  @Inject
  public DiscoveryStrategyFactory(final RuleBasedEngineStrategy ruleStrat) {
    this.ruleStrat = ruleStrat;
    ruleStrat.startRuleFetch();
  }

  /**
   * Returns a list of all strategies.
   *
   * @return all strategies.
   */

  public List<DiscoveryStrategy> giveAllStrategies() {
    synchronized (strategies) {

      if (strategies.isEmpty()) {
        // strategies.add(new KiekerSampleAppStrategy());
        // strategies.add(new TomcatStrategy());
        strategies.add(ruleStrat);
      }

    }

    return strategies;

  }

}
