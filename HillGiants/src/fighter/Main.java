package fighter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

import org.osbot.rs07.api.def.ItemDefinition;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "jython", info = "Hill Giant fighter", name = "HillGiants", version = 1.0, logo = "")
public class Main extends Script {
	private boolean buryBones = true;
	public byte lookPosition = 0;
	private int[] attackIds = {5430, 5396, 5395, 5399, 5400, 5398, 5397,
			474, 476, 475, 478, 471, 473, 472, 477, 536, 386, 390, 389, 387, 385, };
	private int runStatus = 0;
	private long start;

	private String state;
	// @TODO - check spellings and such:
	private String[] depositAllExcept = {"Dragon dagger(p++)", "Brass key"};

	private String foodType = "Lobster";
	private int foodHealing = 12;

	private enum State {
		WALK_TO_BANK,
		USE_BANK,
		WALK_TO_MOBS,
		FIND_NEXT_MOB,
		FIGHTING,
		WAITING
	}

	public long elapsed() {
		return System.currentTimeMillis() - start;
	}

	/**
	 *
	 * @param script	Most likely: client.getBot().getScriptExecutor().getCurrent()
	 * @param g			The Graphics2D object passed into the onPaint() method
	 * @param entity	The Entity whose tile you wish to draw on.
	 * @param tileColor	The color that will be drawn around the border of the tile
	 * @param textColor	The color of the text for the "s" parameter
	 * @param s			The text you wish to display next to the tile.
	 * 					Particularly useful if it's a property of the Entity passed in.
	 */
	public void drawTile(Script script, Graphics g, Entity entity, Color tileColor, Color textColor, String s) {
		Polygon polygon;
		if (entity != null && entity.exists() && (polygon = entity.getPosition().getPolygon(script.getBot(), entity.getPosition().getTileHeight(script.getBot()))) != null) {
			g.setColor(tileColor);
			for (int i = 0; i < polygon.npoints; i++) {
				g.setColor(new Color(0, 0, 0, 20));
				g.fillPolygon(polygon);
				g.setColor(tileColor);
				g.drawPolygon(polygon);
			}
			g.setColor(textColor);
			g.drawString(s, (int) polygon.getBounds().getX(), (int) polygon.getBounds().getY());
		}
	}

	@Override
	public void onPaint(Graphics2D g) {
		long timeElapsed = System.currentTimeMillis() - start;
		long seconds = (timeElapsed / 1000) % 60;
		long minutes = (timeElapsed / (1000 * 60)) % 60;
		long hours = (timeElapsed / (1000 * 60 * 60)) % 24;
		g.setFont(new Font("Trebuchet MS", Font.PLAIN, 14));
		g.setColor(Color.white);

		g.drawString("x", (int)getMouse().getPosition().getX() - 4, (int)getMouse().getPosition().getY() + 5);
		g.drawString(state, 8, 50);
		g.drawString("Time Running: " + (hours >= 10 ? "" + hours : "0" + hours) + ":" + (minutes >= 10 ? "" + minutes : "0" + minutes) + ":" + (seconds >= 10 ? "" + seconds : "0" + seconds), 8, 65);
		g.drawString("XP Gained: " + getExperienceTracker().getGainedXP(Skill.FISHING) + " (" + getExperienceTracker().getGainedLevels(Skill.FISHING) + ")", 8, 80);

		// Highlight the entity being fought. Just kinda neat. :)
		if (myPlayer().getInteracting() != null) {
			drawTile(client.getBot().getScriptExecutor().getCurrent(), g, myPlayer().getInteracting(), Color.cyan, Color.white, "");
		}
	}

	/**
	 * If the state has changed, log it and update the state attribute for the paint.
	 * @param newState - A string representing the current state of the player.
	 */
	private void stateLogger(String newState) {
		if (state != newState) {
			state = newState;
			log("State updated to: " + state);
		}
	}

	private boolean needToBank() {
		// Health is getting low and we're out of food.
		// We're out of pots and they've run down.
		// We're out of food and our inventory is full (eat food to make room for drops that are worth it)
		return true;
	}

	private void openBank() throws InterruptedException {
		stateLogger("Opening Bank");
		getBank().open();
		new FConditionalSleep(() -> getBank().isOpen(), 5000).sleep();
	}

	/**
	 * Runs once when the script is started.
	 * For this script, the method gets your current Fishing experience and starts our timer.
	 * Both used for the Paint updates.
	 */
	@Override
	public void onStart() {
		getExperienceTracker().start(Skill.FISHING);
		start = System.currentTimeMillis();
	}

	/**
	 * Gets the current state of the player.
	 * This method is intended to be run on every iteration of the onLoop() method to determine
	 * what actions needs to be performed.
	 *
	 * @return   State   the State that the player is in
	 */
	private State getState() {
		boolean inBank = bankArea.contains(myPlayer());
		boolean inMobArea = mobArea.contains(myPlayer());
		boolean needToBank = needToBank();
		boolean fighting = something...;
		getInventory().isFull();
		// need to bank...
		if (needToBank && !inBank) {
			return State.WALK_TO_BANK;
		}
		if (needToBank && inBank) {
			return State.USE_BANK;
		}
		// fighting...
		if (!needToBank && !inMobArea) {
			return State.WALK_TO_MOBS;
		}
		if (inMobArea && !fighting) {
			return State.FIND_NEXT_MOB;
		}
		if (fighting) {
			return State.FIGHTING;
		}
		//		if ( getSkills().getDynamic(Skill.HITPOINTS) <=
		//				( getSkills().getStatic(Skill.HITPOINTS) - this.foodHealing) ) {
		//		}
		return State.WAITING;
	}

	/**
	 * A loop that continually runs, checking the State of the player and taking action accordingly.
	 */
	@Override
	public int onLoop() throws InterruptedException {
		switch (getState()) {
		case WALK_TO_BANK:
			// Go up ladder
			// leave hut
			stateLogger("Walking to bank");
			getWalking().webWalk(new Position(bankArea.getRandomPosition()));
			break;
		case USE_BANK:
			if ( getBank().isOpen() ) {
				stateLogger("Depositing items");
				getBank().depositAllExcept(this.depositAllExcept);
				new FConditionalSleep(() -> getInventory().isEmptyExcept(this.depositAllExcept), 5000).sleep();
				getBank().close();
			} else {
				openBank();
			}
			break;
		case WALK_TO_MOBS:
			// Walk to hut
			// Open door to hut
			// Go down ladder when in hut
			break;
		case FIND_NEXT_MOB:
			// check for loot
			// check for heal
			// check for need to pot
			// find next mob and attack it
			break;
		case FIGHTING:
			// check for loot
			// check for heal
			// check for need to pot
			break;
		default:
			// I wouldn't expect to ever get here. It's prudent to add a default though.
			stateLogger("Unexpected condition. Waiting.");
			sleep(1000);
		}
		return random(200,300);
	} // end onLoop()

	@Override
	public int onLoop() throws InterruptedException {
		checkHealthAndEat(false);

		if (runStatus != 0) {
			log("Running!");

			int index = runStatus - 1;

			if (getDistance(runAwayPositions[5]) <= 4) {
				runStatus = 0;
				return 100;
			}

			if (getDistance(runAwayPositions[index]) <= 4) {
				runStatus++;
				index++;

				if (index >= runAwayPositions.length) {
					runStatus = 0;
					return 100;
				}
			}

			return random(100, 200);
		}

		if (!myPlayer().isMoving()) {
			checkForLoot();
		}

		if (buryBones) {
			buryBones();
		}

		if (!isAttacking() && !myPlayer().isMoving()) {
			NPC next = getClosestAttackMob();

			if (next != null) {
				getCamera().toEntity(next);

				next.interact("Attack");

				stateLogger("Combatant found, attacking..");

				return random(2000, 3123);
			}

		} else {
			stateLogger("Waiting for combat to finish..");
		}

		return random(1750, 2750);
	}

	public void checkForLoot() {
		// unimplemented
	}

	public boolean hasFood() {
		Item[] items = getInventory().getItems();

		for (Item i : items) {
			if (i != null && ItemDefinition.forId(i.getId()).getActions()[0] != null) {
				if (ItemDefinition.forId(i.getId()).getActions()[0].equals("Eat")) {
					return true;
				}
			}
		}
		return false;
	}

	public void buryBones() {
		if (!buryBones) {
			return;
		}
		Item[] items = getInventory().getItems();
		int slot = 0;

		for (Item i : items) {
			if (i != null && ItemDefinition.forId(i.getId()).getActions()[0] != null) {
				if (ItemDefinition.forId(i.getId()).getActions()[0].equals("Bury")) {
					getInventory().interact(slot, "Bury");
				}
			}

			slot++;
		}
	}

	//	public void moveCamera() throws Exception {
	//		switch (lookPosition) {
	//		case 0:
	//			myPlayer().getClient().moveCameraToPosition(
	//					new Position(myPlayer().getPosition().getX() - 10,
	//							myPlayer().getPosition().getY(),
	//							myPlayer().getPosition().getZ()));
	//			break;
	//		case 1:
	//			myPlayer().getClient().moveCameraToPosition(
	//					new Position(myPlayer().getPosition().getX(),
	//							myPlayer().getPosition().getY() - 10,
	//							myPlayer().getPosition().getZ()));
	//			break;
	//		case 2:
	//			myPlayer().getClient().moveCameraToPosition(
	//					new Position(myPlayer().getPosition().getX() + 10,
	//							myPlayer().getPosition().getY(),
	//							myPlayer().getPosition().getZ()));
	//			break;
	//		case 3:
	//			myPlayer().getClient().moveCameraToPosition(
	//					new Position(myPlayer().getPosition().getX(),
	//							myPlayer().getPosition().getY() + 10,
	//							myPlayer().getPosition().getZ()));
	//			break;
	//		}
	//
	//			lookPosition = (byte) (lookPosition >= 3 ? 0 : lookPosition + 1);
	//
	//			Thread.sleep(random(900, 1453));
	//		}

	public void checkHealthAndEat(boolean override) {
		if (isLowHealth() || override) {
			Item[] items = getInventory().getItems();

			int slot = 0;

			for (Item i : items) {
				if (i != null && ItemDefinition.forId(i.getId()) != null
						&& ItemDefinition.forId(i.getId()).getActions()[0] != null) {
					if (ItemDefinition.forId(i.getId()).getActions()[0].equals("Eat")) {

						try {
							getInventory().interact(slot, "Eat");
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						if (!isLowHealth()) {
							return;
						}
					}
				}

				slot++;
			}

			if (getSkills().getDynamic(Skill.HITPOINTS) <= 25) {
			}
		}
	}

	public void onPaint(Graphics g) {
		//unimplemented
	}

	public boolean hasAttackId(int id) {
		for (int i : attackIds) {
			if (i == id) {
				return true;
			}
		}

		return false;
	}

	public NPC getClosestAttackMob() {
		List<NPC> npcs = getNpcs().getAll();

		int lowDist = 99999;
		NPC closest = null;

		for (NPC i : npcs) {
			if (i != null && i.	getHealthPercent() > 0) {

				if (i.getInteracting() != null
						&& i.getInteracting().getIndex() == myPlayer().getIndex()) {
					log("Attacking mob that is currently attacking player..");
					return i;
				}

				if (!i.isUnderAttack() && hasAttackId(i.getId())) {
					if (closest == null || lowDist > getDistance(i)) {
						closest = i;
						lowDist = getDistance(i);
					}
				}
			}
		}

		return closest;
	}

	public int getDistance(NPC mob) {
		return Math.abs(mob.getX() - myPlayer().getX())
				+ Math.abs(mob.getY() - myPlayer().getY());
	}

	public int getDistance(Position p) {
		return Math.abs(p.getX() - myPlayer().getX())
				+ Math.abs(p.getY() - myPlayer().getY());
	}

	public boolean isAttacking() {
		return myPlayer().getInteracting() != null
				&& myPlayer().isUnderAttack()
				|| myPlayer().getInteracting() != null;
	}

	@Override
	public String getAuthor() {
		return "Mikey1";
	}

	public boolean isLowHealth() {
		return true;
		//unimplemented
	}
}