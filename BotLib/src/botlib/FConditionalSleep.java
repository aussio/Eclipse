package botlib;

import java.util.function.BooleanSupplier;

import org.osbot.rs07.utility.ConditionalSleep;

/**
 * A subclass allowing for a functional implementation for ConditionalSleep.
 *
 * @author Explv - http://osbot.org/forum/topic/95067-snippet-csleep-syntactic-sugar-for-conditionalsleep/
 */
public final class FConditionalSleep extends ConditionalSleep {

	private final BooleanSupplier condition;

	/**
	 * @param condition The Boolean condition by which the Conditional Sleep should end early.
	 * @param timeout The maximum time that the Conditional Sleep should wait for "condition" to return True.
	 */
	public FConditionalSleep(final BooleanSupplier condition, int timeout) {
		super(timeout);
		this.condition = condition;
	}

	@Override
	public boolean condition() throws InterruptedException {
		return condition.getAsBoolean();
	}
}