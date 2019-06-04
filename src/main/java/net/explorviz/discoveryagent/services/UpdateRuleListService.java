package net.explorviz.discoveryagent.services;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TimerTask;
import net.explorviz.discoveryagent.procezz.discovery.strategies.RuleBasedEngineStrategy;
import net.explorviz.shared.config.annotations.Config;
import net.explorviz.shared.discovery.services.ClientService;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.mvel.MVELRuleFactory;
import org.jeasy.rules.support.JsonRuleDefinitionReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateRuleListService extends TimerTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateRuleListService.class);
  String url = "http://localhost:8085/v1/extension/dummy/test/rulelist";
  private final RuleBasedEngineStrategy strat;

  private static final String httpBase = "http://";
  @Config("updateIP")
  private static final String iP = "localhost";
  @Config("updatePort")
  private static final String port = "8085";
  @Config("updateURL")
  private static final String uRL = "v1/extension/dummy/test/rulelist";

  private static final String MediaType = "application/vnd.api+json";

  private static final MVELRuleFactory ruleFactoryJSON =
      new MVELRuleFactory(new JsonRuleDefinitionReader());



  public UpdateRuleListService(final RuleBasedEngineStrategy strat) {
    this.strat = strat;
  }



  @Override
  public void run() {
    final ClientService clienttest = new ClientService();
    // TODO registration of reader and writer maybe?
    final String s = clienttest.doGETRequest(String.class, url, null);

    final Rules ruleList = stringToRules(s);
    if (!ruleList.isEmpty()) {
      strat.updateRuleList(ruleList);
    }
  }

  /**
   *
   * @param ruleString List of rules in json representation
   * @return returns a List of rules in Rules-Objects
   */
  public Rules stringToRules(final String ruleString) {
    final JSONObject jObj = new JSONObject(ruleString);
    final JSONArray dataObj =
        jObj.getJSONObject("data").getJSONObject("attributes").getJSONArray("ruleList");

    FileWriter file;
    try {
      file = new FileWriter("ruleList.json");
      file.write(dataObj.toString());
      file.flush();
    } catch (final IOException e) {
      LOGGER.info("Problems getting into the file for the json. Please check ruleList.json.");
    }

    try {
      return ruleFactoryJSON.createRules(new FileReader("ruleList.json"));
    } catch (final Exception e) {
      LOGGER.info("Received faulty rulelist from Updater.");

      return null;
    }

  }



}
