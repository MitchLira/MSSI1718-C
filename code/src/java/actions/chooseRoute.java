// Internal action code for project MSSI

package actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import it.polito.appeal.traci.Edge;
import it.polito.appeal.traci.Lane;
import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.bb.BeliefBase;
import jasonenv.SUMOEnv;

public class chooseRoute extends DefaultInternalAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Random rand = new Random();

	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

		BeliefBase bb = ts.getAg().getBB();

		boolean routeHasBeenSelected = false;

		Literal stateLiteral = bb.getCandidateBeliefs(new PredicateIndicator("state", 1)).next();
		Literal costLiteral = bb.getCandidateBeliefs(new PredicateIndicator("cost", 1)).next();
		double cost = Double.parseDouble(costLiteral.getTerm(0).toString());

		String state = stateLiteral.getTerm(0).toString();
		
		Literal temperatureLiteral = bb.getCandidateBeliefs(new PredicateIndicator("temperature", 1)).next();
		double temperature = Double.parseDouble(temperatureLiteral.getTerm(0).toString());

		Iterator<Literal> valuesIterator = bb.getCandidateBeliefs(new PredicateIndicator("value", 3));

		List<Literal> values = new ArrayList<>();
		while (valuesIterator.hasNext()) {
			Literal value = valuesIterator.next();
			if (value.getTerm(0).toString().equals(state)) {
				values.add(value);
			}
		}
		

		if (values.size() > 0) {

			double denominator = 0;
			Map<String, Double> probabilityTerms = new HashMap<>();
			Map<String, Double> roulette = new HashMap<>();
			Map<String, Double> ratio = new HashMap<>();


			for (Literal value: values) {
				String actionName = value.getTerm(1).toString();
				double qValue = Double.parseDouble(value.getTerm(2).toString());
				double probabilityTerm = Math.exp(qValue / temperature);
				denominator += probabilityTerm;

				probabilityTerms.put(actionName, probabilityTerm);
			}

			double sum = 0;
			for (String actionName: probabilityTerms.keySet()) {
				double probability = probabilityTerms.get(actionName) / denominator;
				sum += probability;
				roulette.put(actionName, sum);
				List<Edge> route = SUMOEnv.routes.get(actionName);
				Collection<Lane> lanes = SUMOEnv.instance.getLanes().values();
				Double totalLength = 0.0;
				Double averageSpeed = 0.0;
				int counter = 0;
				for(Iterator<Lane> it = lanes.iterator(); it.hasNext();) {
					Lane l = it.next();
					if(route.contains(l.getParentEdge())) {
						totalLength += l.getLength();
						averageSpeed += l.getMaxSpeed();
						counter++;
					}
				}
				averageSpeed /= counter;
				ratio.put(actionName, totalLength/averageSpeed);
			}

			// Selects route.
			double generated = rand.nextDouble();
			for (String actionName: roulette.keySet()) {
				System.out.println("ratio: " + ratio.get(actionName));
				System.out.println("cost: " + cost);
				if (generated <= roulette.get(actionName)) {
					Literal action = new LiteralImpl("action");
					action.addTerms(new Atom(state), new Atom(actionName));

					if (state.equals("initial")) {
						un.bind((VarTerm) args[0], new Atom(actionName));
						stateLiteral.setTerm(0, new Atom(actionName));
						routeHasBeenSelected = true;
					} else {
						String currentRoute = state;
						un.bind((VarTerm) args[0], new Atom(currentRoute));
						if (actionName.equals("k")) {
							un.bind((VarTerm) args[1], new Atom(currentRoute));
							routeHasBeenSelected = true;
						} else {
							Iterator<Literal> pairsIterator = bb.getCandidateBeliefs(new PredicateIndicator("pair", 2));
							while (pairsIterator.hasNext()) {
								Literal pair = pairsIterator.next();
								if (pair.getTerm(0).toString().equals(currentRoute)) {
									String newRoute = pair.getTerm(1).toString();
									un.bind((VarTerm) args[1], new Atom(newRoute));
									stateLiteral.setTerm(0, new Atom(newRoute));

									Literal nameLiteral = bb.getCandidateBeliefs(new PredicateIndicator("name", 1)).next();
									List<Edge> route = SUMOEnv.routes.get(newRoute);
									synchronized(SUMOEnv.instance.getConn()) {
										SUMOEnv.vehicleObjects.get(nameLiteral.getTerm(0).toString()).changeRoute(route);
									}

									routeHasBeenSelected = true;
									break;
								}
							}
						}
					}
					if (routeHasBeenSelected) {
						Literal statesLiteral = bb.getCandidateBeliefs(new PredicateIndicator("actions", 1)).next();
						((ListTerm) statesLiteral.getTerm(0)).add(action);
						break;
					}
				}
			}
		}
		
		return routeHasBeenSelected;
	}
}
