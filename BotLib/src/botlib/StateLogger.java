package botlib;

import java.util.Optional;

/**
 * A singleton for keeping track of what state we are in and logging if necessary.
 * @author austincurtis
 *
 */
public class StateLogger {

	private static StateLogger instance = null;
	public String state;
	private boolean debug;

	/**
	 * @param debug Determines if we log the state when it changes.
	 */
	protected StateLogger(Optional<Boolean> debug) {
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
	public static StateLogger getInstance(Optional<Boolean> debug) {
		if (instance == null) {
			instance = new StateLogger(debug);
		}
		return instance;
	}

	/**
	 * If the state has changed, update the state attribute (often for paint).
	 * If we are using debug, then log the changed state.
	 * @param state - A string representing the current state of the player.
	 */
	public void log(String state) {
		if (this.state != state) {
			this.state = state;
			if (this.debug) {
				log("STATE: " + state);
			}
		}
	}

}
