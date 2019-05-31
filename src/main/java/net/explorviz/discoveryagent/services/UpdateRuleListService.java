package net.explorviz.discoveryagent.services;

import java.util.TimerTask;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import net.explorviz.discoveryagent.procezz.discovery.strategies.RuleBasedEngineStrategy;
import net.explorviz.shared.discovery.services.ClientService;

public class UpdateRuleListService extends TimerTask {
  String url = "http://localhost:8085/v1/extension/dummy/test/rulelist";
  RuleBasedEngineStrategy strat;



  public UpdateRuleListService(final RuleBasedEngineStrategy strat2) {
    strat = strat2;
  }



  @Override
  public void run() {
    final Client client = ClientBuilder.newClient();
    final String response =
        client.target(url).request("application/vnd.api+json").get(String.class);

    final ClientService clienttest = new ClientService();
    // TODO registration of reader and writer maybe?
    final String s = clienttest.doGETRequest(String.class, url, null);


  }



}
