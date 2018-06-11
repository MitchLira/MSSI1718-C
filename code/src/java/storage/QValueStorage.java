package storage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import consts.GeneralConsts;

public class QValueStorage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<String, Map<Pair, Double>> values = new HashMap<>();
	
	private Map<String, Double> temperatures = new HashMap<>();
	
	public boolean contains(String agentName) {
		return values.containsKey(agentName);
	}
	
	public void initialize(String agentName, List<Pair> pairs) {
		Map<Pair, Double> qValues = new HashMap<>();
		for (Pair pair: pairs) {
			qValues.put(pair, 0.0);
		}
		values.put(agentName, qValues);
		temperatures.put(agentName, GeneralConsts.INITIAL_TEMP);
	}
	
	public Map<Pair, Double> get(String agentName) {
		return values.get(agentName);
	}
	
	public void put(String agentName, Pair pair, double qValue) {
		values.get(agentName).put(pair, qValue);
	}
	
	public double getTemperature(String agentName) {
		return temperatures.get(agentName);
	}
	
	public double putTemperature(String agentName, double temperature) {
		return temperatures.put(agentName, temperature);
	}
	
	public void updateAgent(String agentName, List<Pair> pairs) {
		Map<Pair, Double> agentMap = values.get(agentName);
		for (Pair pair: pairs) {
			agentMap.put(pair, 0.0);
		}
	}

	public boolean containsStateForAgent(String agentName, Pair pair) {
		return values.get(agentName).containsKey(pair);
	}
}
