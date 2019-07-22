package net.explorviz.discoveryagent.services;

import com.github.jasminb.jsonapi.ResourceConverter;
import java.io.StringReader;
import java.util.TimerTask;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import net.explorviz.discoveryagent.procezz.discovery.strategies.RuleBasedEngineStrategy;
import net.explorviz.discoveryagent.server.provider.JSONAPIProvider;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;
import net.explorviz.shared.discovery.services.ClientService;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.mvel.MVELRuleFactory;
import org.jeasy.rules.support.JsonRuleDefinitionReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateRuleListService extends TimerTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateRuleListService.class);
  private final String url;
  private final RuleBasedEngineStrategy strat;
  private static ResourceConverter converter;

  private static final MVELRuleFactory ruleFactoryJSON =
      new MVELRuleFactory(new JsonRuleDefinitionReader());

  public UpdateRuleListService(final RuleBasedEngineStrategy strat, final String url) {
    this.strat = strat;
    this.url = url;
  }

  @Override
  public void run() {
    final ClientService clientService = new ClientService();
    converter = new ResourceConverterFactory().provide();

    clientService.registerProviderReader(new JSONAPIProvider<>(converter));
    clientService.registerProviderWriter(new JSONAPIProvider<>(converter));

    // TODO registration of reader and writer maybe?
    final String ruleString;
    try {
      ruleString = clientService.doGETRequest(String.class, url, null);
      final Rules ruleList = stringToRules(ruleString);
      if (!ruleList.isEmpty() || ruleList != null) {
        strat.updateRuleList(ruleList);
      }
    } catch (final ProcessingException e) {
      LOGGER.info("Connection with the URL " + url + " failed in UpdateRuleListService.");
    } catch (final WebApplicationException w) {
      LOGGER.info("Connection with the URL " + url + " failed in UpdateRuleListService.");
    }
  }

  /**
   * Creates list of rules from a given String.
   *
   * @param ruleString List of rules in json representation.
   * @return returns a List of rules in Rules-Objects. Returns @null by faulty String.
   */
  public Rules stringToRules(final String ruleString) {
    final JSONObject jObj;
    JSONArray dataObj = new JSONArray();
    try {
      jObj = new JSONObject(ruleString);
      dataObj = jObj.getJSONObject("data").getJSONObject("attributes").getJSONArray("ruleList");
    } catch (final JSONException e) {
      LOGGER.info("Received faulty JSON-File from Update-Service.");
    }
    try {
      return ruleFactoryJSON.createRules(new StringReader(dataObj.toString()));
    } catch (final Exception e) {
      LOGGER.info("Received faulty rulelist from Updater.");

      return null;
    }

  }



}
