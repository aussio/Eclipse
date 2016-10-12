package tasks;

import java.util.Optional;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.MethodProvider;

import botlib.AbstractTask;

public class BankWalkTo extends AbstractTask {

	// Perfect fishing guild bank area. :)
	private final Area bankArea = new Area(
			new int[][]{
					{2586, 3418},
					{2586, 3423},
					{2588, 3423},
					{2588, 3422},
					{2590, 3422},
					{2590, 3421},
					{2589, 3421},
					{2589, 3420},
					{2592, 3420},
					{2592, 3417},
					{2588, 3417},
					{2588, 3418}
			});

	public BankWalkTo(MethodProvider api, Optional<Boolean> debug) {
		super(api, debug);
		this.state = "Walking to the bank";
	}

	/**
	 * Execute when your inventory is full and you are not in the bank.
	 */
	@Override
	public boolean shouldExecute() {
		return (api.getInventory().isFull()
				&& !bankArea.contains(api.myPlayer()));
	}

	/**
	 * Walks to a random position within the defined bank area.
	 */
	@Override
	public void execute() throws InterruptedException {
		Position bankDestination = new Position(bankArea.getRandomPosition());
		api.getWalking().walk(bankDestination);
	}

}
