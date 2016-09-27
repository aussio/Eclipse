package tasks;

import java.util.Optional;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.MethodProvider;

import botlib.AbstractTask;

public class FishWalkTo extends AbstractTask {

	// Perfect northern fishing dock area. :)
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
			});

	public FishWalkTo(MethodProvider api, Optional<Boolean> debug) {
		super(api, "Walking to fishing spots", debug);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Execute when our inventory is not full, we're not still banking,
	 * and we're not near the fishing spots.
	 */
	@Override
	public boolean shouldExecute() {
		return (!api.getInventory().isFull()
				&& !fishingArea.contains(api.myPlayer())
				&& !api.getBank().isOpen());
	}

	@Override
	public void execute() throws InterruptedException {
		api.getWalking().walk(new Position(fishingArea.getRandomPosition()));
	}

}