package botlib;

import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.MethodProvider;


public class Antiban {

	protected MethodProvider api;
	private long lastChecked;

	/**
	 * Start the antiban timer whenever this object is created.
	 * The object should be created within the constructor of the Task calling it, which is created onStart().
	 * @param api
	 */
	public Antiban(MethodProvider api) {
		this.api = api;
		this.lastChecked = System.currentTimeMillis();
	}

	/**
	 * Move the mouse upwards off the screen, but only if it isn't already off-screen.
	 */
	private void moveMouseOffScreen() {
		if (api.getMouse().getPosition().y > 0) {
			api.getMouse().move(MethodProvider.random(100, 500), 0);
		}
	}

	/**
	 * Get a random mouse X Position within 10 pixels of where the mouse currently is.
	 */
	private int getRandomCloseMouseX() {
		int mouseXPosition = api.getMouse().getPosition().x;
		return MethodProvider.random(mouseXPosition-10, mouseXPosition+10);
	}

	/**
	 * Get a random mouse Y Position within 10 pixels of where the mouse currently is.
	 */
	@SuppressWarnings("unused")
	private int getRandomCloseMouseY() {
		int mouseYPosition = api.getMouse().getPosition().y;
		return MethodProvider.random(mouseYPosition-10, mouseYPosition+10);
	}

	/**
	 * Nudge the mouse down onto the screen then back up, as if keeping the session awake.
	 */
	private void nudgeScreenAwake() {
		api.getMouse().move(getRandomCloseMouseX(), MethodProvider.random(110, 200));
		api.getMouse().move(getRandomCloseMouseX(), 0);
	}

	/**
	 * Every 4-5 minutes, either check the experience of the skill you are working on,
	 * open your inventory, or just nudge the screen awake.
	 * After any action, move the mouse back off the top of the screen since you are AFK after all.
	 * @param skill The skill's tab that you wish to hover over to check experience.
	 * @throws InterruptedException
	 */
	public void afkAntiban(String skill) throws InterruptedException {
		long now = System.currentTimeMillis();
		long timeSinceLastAntiban = now - this.lastChecked;
		if (timeSinceLastAntiban > MethodProvider.random(240000, 290000)) {
			//logger.update("Antiban");
			this.lastChecked = now;
			int i = MethodProvider.random(2);
			switch (i) {
			case 0:
				if (api.getTabs().getOpen() != Tab.SKILLS) {
					api.getSkills().hoverSkill(Skill.valueOf(skill));
					MethodProvider.sleep(MethodProvider.random(1000,1500));
					break;
				} else {
					nudgeScreenAwake();
					break;
				}
			case 1:
				if (api.getTabs().getOpen() != Tab.INVENTORY) {
					api.getTabs().open(Tab.INVENTORY);
					break;
				} else {
					nudgeScreenAwake();
					break;
				}
			}
		}
		moveMouseOffScreen();
	}


}
