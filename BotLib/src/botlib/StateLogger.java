package botlib;

import java.util.Optional;

import org.osbot.rs07.script.MethodProvider;

/**
 * A singleton for keeping track of what state we are in and logging if necessary.
 * @author austincurtis
 *
 */
public class StateLogger implements Subscriber {

	private static StateLogger instance = null;
	public String state = "Initializing";
	private boolean debug;
	private MethodProvider api;

	/**
	 * @param debug Determines if we log the state when it changes.
	 */
	protected StateLogger(MethodProvider api, Optional<Boolean> debug) {
		this.api = api;
		this.state = "Initializing";
		if (debug.isPresent()) {
			this.debug = true;
		}
	}

	/**
	 * The only public method, which will instantiate the class if an instance does not already exist.
	 * @param debug Determines if we log the state when it changes.
	 * @return Returns the singleton instance of the class.
	 */
	public static StateLogger getInstance(MethodProvider api, Optional<Boolean> debug) {
		if (instance == null) {
			instance = new StateLogger(api, debug);
		}
		return instance;
	}

	/**
	 * If the state has changed, update the state attribute (often for paint).
	 * If we are using debug, then log the changed state.
	 * @param state - A string representing the current state of the player.
	 */
	public void update(String newState) {
		if (! this.state.equals(newState)) {
			this.state = newState;
			if (this.debug) {
				api.log("STATE: " + this.state);
			}
		}
	}

}
