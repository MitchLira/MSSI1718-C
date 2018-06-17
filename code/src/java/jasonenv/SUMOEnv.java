package jasonenv;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import consts.GeneralConsts;
import exception.BadConnectionStatusException;
import it.polito.appeal.traci.Edge;
import it.polito.appeal.traci.InductionLoop;
import it.polito.appeal.traci.StepAdvanceListener;
import it.polito.appeal.traci.Vehicle;
import it.polito.appeal.traci.VehicleLifecycleObserver;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.Environment;
import jason.runtime.RuntimeServicesInfraTier;
import observer.InstanceStartedObserver;
import storage.Pair;
import storage.QValueStorage;
import sumoconn.SUMOInstance;

public class SUMOEnv extends Environment {

	private Random rand = new Random();
	
	private Map<String, String> routesToLanes = new HashMap<>();
	
	private Map<String, Double> agentsTravelTime = new TreeMap<>();
	private AtomicInteger numArrivals = new AtomicInteger(0);

	private QValueStorage qValues = null;

	private boolean braessMode = false;

	private Map<String, Set<String> > vehiclesDetected = new HashMap<>();
	
	private double lastStep = 0;
	private double elapsed = 0;
	
	public static Map<String, List<Edge>> routes = new HashMap<>();
	public static Map<String, Vehicle> vehicleObjects = new HashMap<>();
	
	public static SUMOInstance instance = null;

	/** Called before the MAS execution with the args informed in .mas2j */
	@Override
	public void init(String[] args) {
		super.init(args);

		String configFile = GeneralConsts.NO_BRAESS_CONFIG_FILE;
		if (args.length > 0 && args[0].equals(GeneralConsts.BRAESS_FLAG)) {
			configFile = GeneralConsts.BRAESS_CONFIG_FILE;
			this.braessMode = true;
		}

		instance = new SUMOInstance(configFile);

		
		File qValuesFile = new File((this.braessMode ? GeneralConsts.Q_VALUES_FILENAME_BRAESS : GeneralConsts.Q_VALUES_FILENAME_NO_BRAESS));
		if (qValuesFile.exists()) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(qValuesFile));
				qValues = (QValueStorage) ois.readObject();
				ois.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			qValues = new QValueStorage();
		}

		instance.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {

			@Override
			public void vehicleTeleportStarting(Vehicle arg0) {
				// Teleport started.

			}

			@Override
			public void vehicleTeleportEnding(Vehicle arg0) {
				// Teleport started.
			}

			@Override
			public void vehicleDeparted(Vehicle arg0) {
				vehicleObjects.put(arg0.getID(), arg0);
				
				Literal departed = new LiteralImpl("departed");
				double departTime = (double) instance.getCurrentSimTime() / GeneralConsts.MILLI_TO_SEC;
				departed.addTerm(new NumberTermImpl(departTime / GeneralConsts.ONE_THOUSAND));
				addPercept(arg0.getID(), departed);
				
				agentsTravelTime.put(arg0.getID(), departTime);
			}

			@Override
			public void vehicleArrived(Vehicle arg0) {
				Literal arrived = new LiteralImpl("arrived");
				double arrivalTime = (double) instance.getCurrentSimTime() / GeneralConsts.MILLI_TO_SEC;
				arrived.addTerm(new NumberTermImpl(arrivalTime / GeneralConsts.ONE_THOUSAND));
				addPercept(arg0.getID(), arrived);
				
				double elapsed = arrivalTime - agentsTravelTime.get(arg0.getID());

				agentsTravelTime.put(arg0.getID(), elapsed);
			}
		});

		instance.addStepAdvanceListener(new StepAdvanceListener() {

			@Override
			public void nextStep(double arg0) {
				if (lastStep > 0) {
					elapsed += arg0 - lastStep;
					lastStep = arg0;
					
					if (elapsed < GeneralConsts.INDUCTION_LOOP_STEP * 1000)
						return;
					else elapsed = 0;
				} else {
					lastStep = arg0;
				}
				Map<String, InductionLoop> inductionLoops = instance.getInductionLoops();
				for (String loopID: inductionLoops.keySet()) {
					if (!vehiclesDetected.containsKey(loopID)) {
						vehiclesDetected.put(loopID, new HashSet<>());
					}
					Set<Vehicle> vehicles = null;
					try {
						vehicles = inductionLoops.get(loopID).getLastStepVehicles();
					} catch (IOException e) {
						e.printStackTrace();
					}
					for (Vehicle vehicle: vehicles) {
						if (!vehiclesDetected.get(loopID).contains(vehicle.getID())) {
							vehiclesDetected.get(loopID).add(vehicle.getID());
							addPercept(vehicle.getID(), new LiteralImpl("intersection"));
						}
					}
				}
			}
		});

		instance.addInstanceStartedObserver(new InstanceStartedObserver() {

			@Override
			public void instanceStarted() {

				// Defines routes.
				try {
					Map<String, Edge> edges = instance.getEdges();
					String route1 = "r1";
					List<Edge> edgesRoute1 = new ArrayList<>();
					edgesRoute1.add(edges.get("A1"));
					edgesRoute1.add(edges.get("1B"));
					instance.addRoute(route1, edgesRoute1);
					routesToLanes.put(route1, "A1_0");
					routes.put(route1, edgesRoute1);

					String route2 = "r2";
					List<Edge> edgesRoute2 = new ArrayList<>();
					edgesRoute2.add(edges.get("A2"));
					edgesRoute2.add(edges.get("2B"));
					instance.addRoute(route2, edgesRoute2);
					routesToLanes.put(route2, "A2_0");
					routes.put(route2, edgesRoute2);

					if (SUMOEnv.this.braessMode) {
						String route3 = "r3";
						List<Edge> edgesRoute3 = new ArrayList<>();
						edgesRoute3.add(edges.get("A1"));
						edgesRoute3.add(edges.get("12"));
						edgesRoute3.add(edges.get("2B"));
						instance.addRoute(route3, edgesRoute3);
						routesToLanes.put(route3, "A1_0");
						routes.put(route3, edgesRoute3);
						
						String route4 = "r4";
						List<Edge> edgesRoute4 = new ArrayList<>();
						edgesRoute4.add(edges.get("A2"));
						edgesRoute4.add(edges.get("21"));
						edgesRoute4.add(edges.get("1B"));
						instance.addRoute(route4, edgesRoute4);
						routesToLanes.put(route4, "A2_0");
						routes.put(route4, edgesRoute4);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				startAgents();
			}
		});		
		
		instance.start();
	}

	public void startAgents() {
		// Creates and starts the agents.
		RuntimeServicesInfraTier runservice = getEnvironmentInfraTier().getRuntimeServices();

		for (int i = 0; i < GeneralConsts.NUM_VEHICLES; i++) {
			String agentID = "agent_" + i;

			try {
				runservice.createAgent(
						agentID,               	// agent name
						"car_agent.asl",   		// AgentSpeak source
						null,                  	// default agent class
						null,                  	// default architecture class
						null,                	// default belief base parameters
						null, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			List<String> routes = new ArrayList<>();
			routes.add("r1");
			routes.add("r2");

			if (this.braessMode) {
				routes.add("r3");
				routes.add("r4");
			}
			
			if (!qValues.contains(agentID)) {	
				List<Pair> pairs = new ArrayList<>();
				for (String route: routes) {
					pairs.add(new Pair("initial", route));
				}
				
				if (this.braessMode) {
					for (String route: routes) {
						pairs.add(new Pair(route, "k"));		// Keep route.
						pairs.add(new Pair(route, "c"));		// Change route.
					}
				}

				qValues.initialize(agentID, pairs);
			} else if (this.braessMode) {
				if (!qValues.containsStateForAgent(agentID, new Pair("initial", "r3"))) {
					List<String> routesToAdd = new ArrayList<>();
					routesToAdd.add("r3");
					routesToAdd.add("r4");
					
					List<Pair> pairs = new ArrayList<>();
					for (String route: routesToAdd) {
						pairs.add(new Pair("initial", route));
						pairs.add(new Pair(route, "k"));		// Keep route.
						pairs.add(new Pair(route, "c"));		// Change route.
					}
					
					qValues.updateAgent(agentID, pairs);
				}
			}

			List<Literal> initialPercepts = new ArrayList<>();
			
			for (String route: routes) {
				Literal routeLiteral = new LiteralImpl("route");
				routeLiteral.addTerms(new Atom(route));
				initialPercepts.add(routeLiteral);
			}
			
			Map<Pair, Double> agentValues = qValues.get(agentID);
			Set<Pair> stateActions = agentValues.keySet();
			
			for (Pair stateAction: stateActions) {
				Literal valueLiteral = new LiteralImpl("value");
				valueLiteral.addTerms(new Atom(stateAction.getState()), new Atom(stateAction.getAction()),
						new NumberTermImpl(agentValues.get(stateAction)));
				initialPercepts.add(valueLiteral);
			}
			
			double temperature = qValues.getTemperature(agentID);
			Literal temperatureLiteral = new LiteralImpl("temperature");
			temperatureLiteral.addTerm(new NumberTermImpl(temperature));
			initialPercepts.add(temperatureLiteral);

			if (this.braessMode) {				
				Literal pair1 = new LiteralImpl("pair");
				pair1.addTerms(new Atom("r1"), new Atom("r3"));
				
				Literal pair2 = new LiteralImpl("pair");
				pair2.addTerms(new Atom("r3"), new Atom("r1"));
				
				Literal pair3 = new LiteralImpl("pair");
				pair3.addTerms(new Atom("r2"), new Atom("r4"));
				
				Literal pair4 = new LiteralImpl("pair");
				pair4.addTerms(new Atom("r4"), new Atom("r2"));
				
				initialPercepts.add(pair1);
				initialPercepts.add(pair2);
				initialPercepts.add(pair3);
				initialPercepts.add(pair4);
			}
			
			Literal agentName = new LiteralImpl("name");
			agentName.addTerm(new Atom(agentID));
			initialPercepts.add(agentName);
			
			Literal[] percepts = new Literal[initialPercepts.size()];
			addPercept(agentID, initialPercepts.toArray(percepts));

			runservice.startAgent(agentID);
		}
	}

	public boolean killAgent(String agentName) {
		RuntimeServicesInfraTier runservice = getEnvironmentInfraTier().getRuntimeServices();
		return runservice.killAgent(agentName, null);
	}

	@Override
	public boolean executeAction(String agName, Structure action) {
		String functor = action.getFunctor();
		int arity = action.getArity();
		if (functor.equals("createAvatar") && arity == 1) {
			String route = ((Atom) action.getTerm(0)).toString();
			if (!routesToLanes.containsKey(route)) {
				return false;
			}

			try {
				Thread.sleep(rand.nextInt(GeneralConsts.MAX_DELAY));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				instance.createAvatar(agName, "normal car", route, routesToLanes.get(route));
			} catch (IOException e) {
				e.printStackTrace();
			}

			return true;
		} else if (functor.equals("signalArrival") && arity == 2) {
			// Updates Q-value info.
			ListTerm values = (ListTerm) action.getTerm(0);
			Iterator<Term> valuesIter = values.iterator();

			while (valuesIter.hasNext()) {
				Literal next = (Literal) valuesIter.next();
				Pair pair = new Pair(next.getTerm(0).toString(), next.getTerm(1).toString());
				double qValue = Double.parseDouble(next.getTerm(2).toString());
				qValues.put(agName, pair, qValue);
			}
			
			NumberTerm temperatureTerm = (NumberTerm) action.getTerm(1);
			double temperature = Double.parseDouble(temperatureTerm.toString());
			qValues.putTemperature(agName, temperature);

			if (numArrivals.incrementAndGet() == GeneralConsts.NUM_VEHICLES) {
				try {
					terminate();
				} catch (IOException | InterruptedException | BadConnectionStatusException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return true;
		}

		return false;
	}

	public void terminate() throws IOException, InterruptedException, BadConnectionStatusException {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream((this.braessMode ? GeneralConsts.Q_VALUES_FILENAME_BRAESS : GeneralConsts.Q_VALUES_FILENAME_NO_BRAESS)));
			oos.writeObject(qValues);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			FileWriter pw = null;
			double sumTravelTime = 0;
			if(!Files.exists(Paths.get(GeneralConsts.AGENTS_TRAVEL_TIME_FILENAME))) {
				pw = new FileWriter(GeneralConsts.AGENTS_TRAVEL_TIME_FILENAME);
				for (Map.Entry<String, Double> auxMap : agentsTravelTime.entrySet()) {
					StringBuilder sb = new StringBuilder();
					sb.append(auxMap.getKey());
					sb.append("\t");
					pw.append(sb.toString());
				}
				pw.append("Average\n");
				pw.flush();
			}
			pw = new FileWriter(GeneralConsts.AGENTS_TRAVEL_TIME_FILENAME, true);
			for (Map.Entry<String, Double> auxMap : agentsTravelTime.entrySet()) {
				StringBuilder sb = new StringBuilder();
				sb.append(auxMap.getValue());
				sb.append("\t");
				pw.append(sb.toString());
				sumTravelTime += auxMap.getValue();
			}
			double averageTravelTime = sumTravelTime / agentsTravelTime.size();
			pw.append(averageTravelTime + "\n");
			pw.flush();
			pw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		instance.terminate();
		instance.join();

		Thread tr = getThreadByName("main");
		tr.interrupt();
	}
	
	public Thread getThreadByName(String threadName) {
	    for (Thread t : Thread.getAllStackTraces().keySet()) {
	        if (t.getName().equals(threadName)) return t;
	    }
	    return null;
	}

	/** Called before the end of MAS execution */
	@Override
	public void stop() {
		super.stop();
	}
}
