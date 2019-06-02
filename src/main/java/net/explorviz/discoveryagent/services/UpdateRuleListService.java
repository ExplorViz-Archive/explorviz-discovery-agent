package net.explorviz.discoveryagent.services;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TimerTask;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import net.explorviz.discoveryagent.procezz.discovery.strategies.RuleBasedEngineStrategy;
import net.explorviz.shared.config.annotations.Config;
import net.explorviz.shared.discovery.services.ClientService;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.mvel.MVELRuleFactory;
import org.jeasy.rules.support.JsonRuleDefinitionReader;
import org.json.JSONArray;
import org.json.JSONObject;

public class UpdateRuleListService extends TimerTask {
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

  MVELRuleFactory ruleFactoryJSON = new MVELRuleFactory(new JsonRuleDefinitionReader());



  public UpdateRuleListService(final RuleBasedEngineStrategy strat2) {
    strat = strat2;
  }



  @Override
  public void run() {
    final Client client = ClientBuilder.newClient();
    final String response = client.target(url).request(MediaType).get(String.class);

    final ClientService clienttest = new ClientService();
    // TODO registration of reader and writer maybe?
    final String s = clienttest.doGETRequest(String.class, url, null);

    try {
      strat.updateRuleList(stringToRules(s));
    } catch (final FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public Rules stringToRules(final String s) throws FileNotFoundException, Exception {
    final JSONObject jObj = new JSONObject(s);
    final JSONArray dataObj =
        jObj.getJSONObject("data").getJSONObject("attributes").getJSONArray("ruleList");

    FileWriter file;
    try {
      file = new FileWriter("ruleList.json");
      file.write(dataObj.toString());
      file.flush();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return ruleFactoryJSON.createRules(new FileReader("ruleList.json"));

  }



}
