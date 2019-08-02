package net.explorviz.discoveryagent.procezz.discovery.strategies.util;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.RuleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of the rule Engine, for sending a extra message for potential Invalid rules.
 */
public class RulesListenerExtend implements RuleListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(RulesListenerExtend.class);

  @Override
  public boolean beforeEvaluate(final Rule rule, final Facts facts) {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public void afterEvaluate(final Rule rule, final Facts facts, final boolean evaluationResult) {
    LOGGER.info("AFTER EVAL GOT TRIGGERED");
  }

  @Override
  public void beforeExecute(final Rule rule, final Facts facts) {
    // TODO Auto-generated method stub
  }

  @Override
  public void onSuccess(final Rule rule, final Facts facts) {}

  @Override
  public void onFailure(final Rule rule, final Facts facts, final Exception exception) {
    LOGGER.info("Faulty rule " + rule.getName() + ". Please remove it!");

  }

}
