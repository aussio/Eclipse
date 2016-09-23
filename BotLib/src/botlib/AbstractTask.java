package botlib;

import org.osbot.rs07.script.MethodProvider;

/**
 * Provides the interface for Task classes.
 * A "Task" is intended to have a method by which the caller can check if the Task needs to be called
 *   and an associated method to perform the work of the task when the Task does need to be called. 
 */
public abstract class AbstractTask {

	protected MethodProvider api;

	/**
	 * Take in the script context so that the osbot methods run on the script using the Tasks.
	 * When instantiating a concrete Task class within a Script, you will pass the script object itself into the constructor.
	 * Example: `new DropTask(this)`
	 * @param api
	 */
	public AbstractTask(MethodProvider api) {
		this.api = api;
	}

	/**
	 * A status-check representing whether the work within this.execute() needs to be called.
	 * @return
	 */
	public abstract boolean shouldExecute();

	/**
	 * The "work" that needs to be performed when the script is in a state such that this.shouldExecute() is true.
	 */
	public abstract void execute() throws InterruptedException;

	/**
	 * The command-pattern interface that the caller executes to perform the Task's work when needed.
	 */
	public void executeIfReady() throws InterruptedException {
		if (shouldExecute()) { 
			execute();
		}
	}

} 