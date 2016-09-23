package tasks;
import java.util.Optional;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;

import botlib.AbstractTask;
import botlib.BotLibMethodProvider;
import botlib.FConditionalSleep;


public class FindFishingSpotTask extends AbstractTask {

	private final Area fishingArea = new Area(
			new int[][]{
					{2600, 3426},
					{2605, 3426},
					{2605, 3424},
					{2601, 3424},
					{2601, 3422},
					{2605, 3422},
					{2605, 3420},
					{2602, 3420},
					{2602, 3421},
					{2601, 3421},
					{2601, 3420},
					{2599, 3420},
					{2599, 3424},
					{2600, 3424}
			}
			);
	private final Area northernFishingSpots = new Area(2598, 3419, 2605, 3426);

	public FindFishingSpotTask(MethodProvider api, Optional<Boolean> debug) {
		super(api, "Finding fishing spot", debug);
	}

	/**
	 * Alternate constructor allowing for the default state String to be overridden.
	 */
	public FindFishingSpotTask(MethodProvider api, String state, Optional<Boolean> debug) {
		super(api, state, debug);
	}

	/**
	 * Find the closest appropriate fishing spot.
	 *
	 * @param actions The list of actions that our fishing spot should have.
	 * @return The closest appropriate fishing spot.
	 */
	private NPC findFishingSpot(final String[] actions) {
		@SuppressWarnings("unchecked")
		NPC fishingSpot = api.getNpcs().closest(new Filter<NPC>() {
			public boolean match(final NPC npc) {
				return (BotLibMethodProvider.hasActions(npc, actions)
						&& northernFishingSpots.contains(npc.getPosition())); // And we're on the northern dock
			}
		});
		return fishingSpot;
	}

	/**
	 * Find the closest fishing spot and interact with it.
	 */
	public void execute() throws InterruptedException {
		NPC fishingSpot = findFishingSpot(new String[]{"Net", "Harpoon"});
		if (fishingSpot != null) {
			MethodProvider.sleep(MethodProvider.random(1000,3000)); // Be a little more human about your reaction time.
			fishingSpot.interact("Harpoon");
			new FConditionalSleep(() -> api.myPlayer().isAnimating(), 5000).sleep();
		}

	}

	/**
	 * Find a fishing spot when the following conditions are true:
	 *   1. Your inventory is NOT full (we could get more fish!)
	 *   2. You are already within the Fishing Area (otherwise you need to walk to the fishing location first)
	 *   3. You are NOT already animating
	 *   	(which would indicate that you are either running to an fishing spot or already fishing).
	 */
	@Override
	public boolean shouldExecute() {
		return (! api.getInventory().isFull()
				&& fishingArea.contains(api.myPlayer())
				&& ! api.myPlayer().isAnimating());
	}

}
