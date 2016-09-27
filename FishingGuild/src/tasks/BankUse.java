package tasks;

import java.util.Optional;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.script.MethodProvider;

import botlib.AbstractTask;
import botlib.FConditionalSleep;

public class BankUse extends AbstractTask {

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
	private String[] depositAllExcept;

	public BankUse(MethodProvider api, Optional<Boolean> debug, String[] depositAllExcept) {
		super(api, "Using the bank", debug);
		this.depositAllExcept= depositAllExcept;
	}

	/**
	 * Gets the nearest bank to the player and opens it.
	 * Waits up to 5-seconds for the bank to open and cuts the wait short when the bank opens.
	 *
	 * @throws InterruptedException
	 */
	private void openBank() throws InterruptedException {
		logger.update("Opening Bank");
		api.getBank().open();
		new FConditionalSleep(() -> api.getBank().isOpen(), 5000).sleep();
	}

	/**
	 * Execute when our inventory is full and we're standing in the bank.
	 */
	@Override
	public boolean shouldExecute() {
		return (api.getInventory().isFull()
				&& bankArea.contains(api.myPlayer()));
	}

	/**
	 * If the bank is not already open, open the bank.
	 * Once the bank is open, deposit all items.
	 */
	@Override
	public void execute() throws InterruptedException {
		if ( api.getBank().isOpen() ) {
			logger.update("Depositing items");
			api.getBank().depositAllExcept(this.depositAllExcept);
			new FConditionalSleep(() -> api.getInventory().isEmptyExcept(this.depositAllExcept), 5000).sleep();
			api.getBank().close();
		} else {
			openBank();
		}

	}

}
