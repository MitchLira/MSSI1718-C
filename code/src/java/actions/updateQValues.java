// Internal action code for project MSSI

package actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import consts.GeneralConsts;
import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.bb.BeliefBase;
import storage.Pair;

public class updateQValues extends DefaultInternalAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		BeliefBase bb = ts.getAg().getBB();

		// Gets departure and arrival times.
		Literal departure = bb.getCandidateBeliefs(new PredicateIndicator("departed", 1)).next();
		Literal arrival = bb.getCandidateBeliefs(new PredicateIndicator("arrived", 1)).next();
		double elapsedTime = Double.parseDouble(arrival.getTerm(0).toString()) - Double.parseDouble(departure.getTerm(0).toString());
		double reward = 1 / elapsedTime;

		bb.abolish(new PredicateIndicator("departed", 1));
		bb.abolish(new PredicateIndicator("arrived", 1));
		
		// Gets current values.
		Iterator<Literal> valuesIterator = bb.getCandidateBeliefs(new PredicateIndicator("value", 3));
		
		Map<Pair, Double> values = new HashMap<>();
		
		while (valuesIterator.hasNext()) {
			Literal value = valuesIterator.next();
			values.put(new Pair(value.getTerm(0).toString(), value.getTerm(1).toString()), Double.parseDouble(value.getTerm(2).toString()));
		}
		
		// Gets selected actions.
		Literal actionsLiteral = bb.getCandidateBeliefs(new PredicateIndicator("actions", 1)).next();
		List<Term> actions = ((ListTerm) actionsLiteral.getTerm(0)).getAsList();
		
		for (int i = 0; i < actions.size(); i++) {
			Literal literal = (Literal) actions.get(i);
			String state = literal.getTerm(0).toString();
			String actionName = literal.getTerm(1).toString();
			
			double currentQValue = values.get(new Pair(state, actionName));
			double actionReward = i < actions.size() - 1 ? 0 : reward;
			
			double maxFutureValue = 0;
			
			Set<Pair> pairs = values.keySet();
			for (Pair pair: pairs) {
				if (pair.getState().equals(actionName)) {
					double futureValue = values.get(pair);
					if (futureValue > maxFutureValue) {
						maxFutureValue = futureValue;
					}
				}
			}
			
			double newQValue = currentQValue + GeneralConsts.LEARNING_RATE * (actionReward + GeneralConsts.DISCOUNT_FACTOR * maxFutureValue - currentQValue);
			
			valuesIterator = bb.getCandidateBeliefs(new PredicateIndicator("value", 3));
			while (valuesIterator.hasNext()) {
				Literal value = valuesIterator.next();
				if (value.getTerm(0).toString().equals(state) && value.getTerm(1).toString().equals(actionName)) {
					value.setTerm(2, new NumberTermImpl(newQValue));
				}
			}
		}
		
		ListTerm valuesLiterals = new ListTermImpl();
		valuesIterator = bb.getCandidateBeliefs(new PredicateIndicator("value", 3));
		while (valuesIterator.hasNext()) {
			Literal value = valuesIterator.next();
			valuesLiterals.add(value);
		}
		
		un.bind((VarTerm) args[0], valuesLiterals);
		
		Literal temperatureLiteral = bb.getCandidateBeliefs(new PredicateIndicator("temperature", 1)).next();
		
		double temperature = Double.parseDouble(temperatureLiteral.getTerm(0).toString());
		
		// Updates temperature.
		temperature /= 2;
		
		un.bind((VarTerm) args[1], new NumberTermImpl(temperature));

		return true;
	}
}
