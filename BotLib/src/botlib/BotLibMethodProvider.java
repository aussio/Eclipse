package botlib;

import org.osbot.rs07.api.model.NPC;

public class BotLibMethodProvider {

	/**
	 * A helper method to check if all actions in a list are present on a given NPC.
	 * This is particularly useful within a Filter.
	 *
	 * @param npc The NPC object whose actions you wish to check.
	 * @param actions A list of actions which you want to check for.
	 * @return Boolean representing if all passed-in actions are present on npc.
	 */
	public static Boolean hasActions(NPC npc, String[] actions) {
		Boolean match = true;
		for (String a : actions) {
			if (! npc.hasAction(a)) {
				match = false;
			}
		}
		return match;
	}

}
