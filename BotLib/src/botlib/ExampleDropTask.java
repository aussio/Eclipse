package botlib;

import org.osbot.rs07.script.MethodProvider;


/**
 * An example implementation of the AbstractTask class.
 * This Task will drop all objects when the inventory is full.
 */
public class ExampleDropTask extends AbstractTask {

	public ExampleDropTask(MethodProvider api) {
		super(api);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Return whether the inventory is full or not.
	 */
	@Override
	public boolean shouldExecute() {
		return api.getInventory().isFull();
	}

	/**
	 * Drop all items within the inventory.
	 */
	@Override
	public void execute() {
		api.getInventory().dropAll();
	}
}
