package net.explorviz.discoveryagent.procezz.discovery;

import java.util.ArrayList;
import java.util.List;

import net.explorviz.discoveryagent.procezz.discovery.strategies.KiekerSampleAppStrategy;
import net.explorviz.discoveryagent.procezz.discovery.strategies.TomcatStrategy;

public final class DiscoveryStrategyFactory {

	private static List<DiscoveryStrategy> strategies = new ArrayList<DiscoveryStrategy>();

	private DiscoveryStrategyFactory() {
		// no need to instantiate
	}

	public static List<DiscoveryStrategy> giveAllStrategies() {

		synchronized (strategies) {

			if (strategies.isEmpty()) {
				strategies.add(new KiekerSampleAppStrategy());
				strategies.add(new TomcatStrategy());
			}

		}

		return strategies;

	}

}
