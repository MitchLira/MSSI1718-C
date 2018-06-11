package sumoconn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import consts.GeneralConsts;
import exception.BadConnectionStatusException;
import it.polito.appeal.traci.AddRouteQuery;
import it.polito.appeal.traci.AddVehicleQuery;
import it.polito.appeal.traci.Edge;
import it.polito.appeal.traci.InductionLoop;
import it.polito.appeal.traci.Lane;
import it.polito.appeal.traci.Route;
import it.polito.appeal.traci.StepAdvanceListener;
import it.polito.appeal.traci.SumoTraciConnection;
import it.polito.appeal.traci.Vehicle;
import it.polito.appeal.traci.VehicleLifecycleObserver;
import it.polito.appeal.traci.VehicleType;
import observer.InstanceStartedObserver;

public class SUMOInstance extends Thread {

	private SumoTraciConnection conn;
	private ConnectionStatus connStatus;
	private AtomicBoolean terminate = new AtomicBoolean(false);

	private ArrayList<InstanceStartedObserver> instanceStartedObservers = new ArrayList<>();
	private Map<String, InductionLoop> inductionLoops = null;

	public SUMOInstance(String configFile) {
		this.conn = new SumoTraciConnection(
				configFile,  				// config file
				12345                  	    // random seed
				);
		this.connStatus = ConnectionStatus.MAY_OPEN;
	}

	public void addRoute(String routeName, List<Edge> edges) throws IOException {
		AddRouteQuery routeQueryObj = conn.queryAddRoute();
		routeQueryObj.setVehicleData(routeName, edges);
		routeQueryObj.run();
	}

	public void connect(boolean gui) throws InterruptedException, IOException, BadConnectionStatusException {
		if (this.connStatus != ConnectionStatus.MAY_OPEN)
			throw new BadConnectionStatusException();
		this.conn.runServer(gui);
		this.connStatus = ConnectionStatus.OPEN;
	}

	public void close() throws InterruptedException, IOException, BadConnectionStatusException {
		if (this.connStatus != ConnectionStatus.MAY_OPEN)
			throw new BadConnectionStatusException();
		this.connStatus = ConnectionStatus.CLOSED;
		this.conn.close();
	}

	public void nextStep() throws IllegalStateException, BadConnectionStatusException, IOException {
		if (this.connStatus != ConnectionStatus.OPEN)
			throw new BadConnectionStatusException();
		this.conn.nextSimStep();
	}

	public int getCurrentStep() {
		return this.conn.getCurrentSimTime();
	}


	public Map<String, Edge> getEdges() throws IOException {
		return conn.getEdgeRepository().getAll();
	}

	public Map<String, Route> getRoutes() throws IOException {
		return conn.getRouteRepository().getAll();
	}

	public Map<String, Lane> getLanes() throws IOException {
		return conn.getLaneRepository().getAll();
	}

	public Map<String, VehicleType> getVehicleTypes() throws IOException {
		return conn.getVehicleTypeRepository().getAll();
	}

	public Map<String, Vehicle> getVehicles() throws IOException {
		return conn.getVehicleRepository().getAll();
	}

	public void addVehicleLifecycleObserver(VehicleLifecycleObserver observer) {
		conn.addVehicleLifecycleObserver(observer);
	}

	public void addInstanceStartedObserver(InstanceStartedObserver observer) {
		instanceStartedObservers.add(observer);
	}
	
	public void addStepAdvanceListener(StepAdvanceListener listener) {
		conn.addStepAdvanceListener(listener);
	}

	public void notifyInstanceStarted() {
		for (InstanceStartedObserver observer: instanceStartedObservers) {
			observer.instanceStarted();
		}
	}

	public Map<String, InductionLoop> getInductionLoops() {
		return this.inductionLoops;
	}
	
	public SumoTraciConnection getConn() {
		return this.conn;
	}

	public void createAvatar(String agentID, String vehicleType, String route, String lane) throws IOException {
		if (connStatus != ConnectionStatus.OPEN)
			return;
		synchronized (conn) {
			try {
				AddVehicleQuery v1 = conn.queryAddVehicle();
				v1.setVehicleData(agentID, getVehicleTypes().get(vehicleType), getRoutes().get(route), getLanes().get(lane), conn.getCurrentSimTime(), 0, 0);
				v1.run();
			} catch (IOException e) {
				signalError();
				throw e;
			}
		}
	}

	public int getCurrentSimTime() {
		synchronized (conn) {
			// In milliseconds.
			return conn.getCurrentSimTime();
		}
	}

	public void terminate() {
		this.connStatus = ConnectionStatus.CLOSED;
		this.terminate.set(true);
	}

	public void signalError() {
		this.connStatus = ConnectionStatus.ERROR;
		this.terminate.set(true);
	}

	public void run() {
		try {
			this.connect(true);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (BadConnectionStatusException e1) {
			e1.printStackTrace();
		}

		try {
			this.inductionLoops = this.conn.getInductionLoopRepository().getAll();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		this.notifyInstanceStarted();

		try {
			while (!terminate.get()) {
				synchronized (conn) {
					conn.nextSimStep();
				}
				Thread.sleep(GeneralConsts.SIMULATION_STEP);
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
