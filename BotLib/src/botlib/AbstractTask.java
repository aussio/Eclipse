package botlib;

import java.util.Optional;

import org.osbot.rs07.script.MethodProvider;

/**
 * Provides the interface for Task classes.
 * A "Task" is intended to have a method by which the caller can check if the Task needs to be called
 *   and an associated method to perform the work of the task when the Task does need to be called.
 */
public abstract class AbstractTask implements Publisher{

	protected MethodProvider api;
	protected StateLogger logger;
	protected String state;
	protected Subscriber[] subscribers;

	/**
	 * Take in the script context so that the osbot methods run on the script using the Tasks.
	 * When instantiating a concrete Task class within a Script, you will pass the script object itself into the constructor.
	 * Example: `new DropTask(this)`
	 * @param api
	 * @param debug An optional parameter determining if we're running in debug mode.
	 */
	public AbstractTask(MethodProvider api, Optional<Boolean> debug) {
		this.api = api;
		this.logger = StateLogger.getInstance(api, debug);
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
			notify_subscribers();
			execute();
		}
	}

	public void notify_subscribers(){
		for( Subscriber sub : this.subscribers){
			sub.update(this.state);
		}
	}

	public void attach(Subscriber... subscribers){
		this.subscribers = subscribers;
	}

}