package tasks;

import java.util.Optional;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.script.MethodProvider;

import botlib.AbstractTask;
import botlib.Antiban;

public class Fish extends AbstractTask {

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
	private Antiban antiban;

	public Fish(MethodProvider api, Optional<Boolean> debug) {
		super(api, debug);
		this.antiban = new Antiban(api);
		this.state = "Catching fish";
	}

	@Override
	public boolean shouldExecute() {
		return (!api.getInventory().isFull()
				&& fishingArea.contains(api.myPlayer())
				&& api.myPlayer().isAnimating());
	}

	@Override
	public void execute() throws InterruptedException {
		this.antiban.afkAntiban("FISHING");

	}

}

//		case FISHING:
//			logger.update("Catching fish.");
//			randomizeMouse(); // antiban
//			// @TODO - instead of checking for anything in inventory, we should check for the addition of
//			// fish, thereby being more explicit. fishCaught shouldn't go up if something other than fish
//			// is in the inventory now for whatever reason (script paused, random, etc).
//			if (getInventory().getEmptySlots() != inventoryCount && fishingArea.contains(myPlayer())) {
//				inventoryCount = getInventory().getEmptySlots();
//				fishCaught += 1;
//			}
//			break;