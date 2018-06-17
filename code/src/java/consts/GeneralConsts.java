package consts;

public final class GeneralConsts {
	public static int NUM_VEHICLES = 100;
	public static int MAX_DELAY = 500;
	public static double INITIAL_TEMP = 1000;
	public static double LEARNING_RATE = 0.5;
	public static double DISCOUNT_FACTOR = 0.5;
	public static double SPECIAL_ROUTES_REWARD_FACTOR = 10;
	public static String Q_VALUES_FILENAME_BRAESS = "q_values_braess.dat";
	public static String Q_VALUES_FILENAME_NO_BRAESS = "q_values.dat";
	public static String AGENTS_TRAVEL_TIME_FILENAME = "agents_travel_time.tsv";
	public static final String BRAESS_FLAG = "--braess";
	public static final String BRAESS_CONFIG_FILE = "res/sumo/braess.sumo.cfg";
	public static final String NO_BRAESS_CONFIG_FILE  = "res/sumo/no-braess.sumo.cfg";
	public static final double MILLI_TO_SEC = 1e3;
	public static final double ONE_THOUSAND = 1e3;
	public static final int SIMULATION_STEP = 1;
	public static final int INDUCTION_LOOP_STEP = 10;
	public static final int MAX_INTERSECTION_DELAY = 5;
}
