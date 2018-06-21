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
				System.out.println(actionName);
				List<Edge> route = new ArrayList<Edge>();
				if(state.equals("initial") && (actionName.equals("r3") || actionName.equals("r4"))) {
					route = SUMOEnv.routes.get(actionName);
				}
				else if(state.equals("r3") || state.equals("r4")) {
					route = SUMOEnv.routes.get(state);
				}
				if((state.equals("initial") && (actionName.equals("r3") || actionName.equals("r4"))) || state.equals("r3") || state.equals("r4")) {
					Collection<Lane> lanes = SUMOEnv.instance.getLanes().values();
					double totalLength = 0.0;
					double averageSpeed = 0.0;
					int counter = 0;
					ArrayList<Edge> parentEdges = new ArrayList<Edge>();
					for(Iterator<Lane> it = lanes.iterator(); it.hasNext();) {
						Lane l = it.next();
						if(!parentEdges.contains(l.getParentEdge())) {
							if(route.contains(l.getParentEdge())) {
								totalLength += l.getLength();
								averageSpeed += l.getMaxSpeed();
								counter++;
							}
							parentEdges.add(l.getParentEdge());
						}
					}
					System.out.println("length " + totalLength);
					averageSpeed /= counter;

					System.out.println("speed " + averageSpeed);
					ratio.put(state, totalLength/averageSpeed);
				}
				
			}

			double sum = 0;
			for (String actionName: probabilityTerms.keySet()) {
				double value = 0.0;
				if(state.equals("r3") || state.equals("r4")) {
					if(ratio.containsKey("r1")) {
						value = ratio.get(state)/ratio.get("r1");
					}
					else if(ratio.containsKey("r2")) {
						value = ratio.get(state)/ratio.get("r2");
					}
				}
				System.out.println("VALUE: " + value);
				double probability = probabilityTerms.get(actionName) / denominator;
				sum += probability;
				if(value <= cost && value != 0.0 && value <= 1.0) {
					sum += 0.1*probability;
				}
				roulette.put(actionName, sum);
				
			}

			// Selects route.
			double generated = rand.nextDouble();
			for (String actionName: roulette.keySet()) {
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
