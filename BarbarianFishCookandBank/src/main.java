import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;


@ScriptManifest(author = "Jython",
                info = "Fishes Trout/Salmon at Barbarian Village, cooks them, then banks the fish.",
                name = "Barb. Village Fish n' Cook",
                version = 1.0,
                logo = "")
public class main extends Script {
	private long timeStart;
	private long lastCheckedAntiban;
	private String state = "Initializing..";
	private int inventoryCount = 0;
	private int fishCaught = 0;

	private final Area northFishingArea = new Area(3106, 3436, 3110, 3430);
	private final Area southFishingArea = new Area(3104, 3422, 3101, 3426);
    private final Area[] fishingAreas = { northFishingArea,
    									  southFishingArea};
	private final Area bankArea = new Area(3094, 3488, 3092, 3492);

  private enum State {
	WALK_TO_BANK,
	OPEN_BANK,
	USE_AND_CLOSE_BANK,
    WALK_TO_FISHING_SPOT,
    FIND_FISHING_SPOT,
    FISHING,
    COOKING,
    WAITING
	}

    /**
    * Runs once when the script is started.
    * For this script, the method gets your current Fishing experience and starts our timer.
    * Both used for the Paint updates.
    */
	@Override
	public void onStart() {
		getExperienceTracker().start(Skill.FISHING);
		timeStart = System.currentTimeMillis();
		lastCheckedAntiban = System.currentTimeMillis();
        fishCaught = 0;
	}

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
		long timeElapsed = System.currentTimeMillis() - timeStart;
		long seconds = (timeElapsed / 1000) % 60;
		long minutes = (timeElapsed / (1000 * 60)) % 60;
		long hours = (timeElapsed / (1000 * 60 * 60)) % 24;
		g.setFont(new Font("Trebuchet MS", Font.PLAIN, 14));
		g.setColor(Color.white);

		g.drawString("x", (int)getMouse().getPosition().getX() - 4, (int)getMouse().getPosition().getY() + 5);
		g.drawString(state, 8, 50);
		g.drawString("Time Running: " + (hours >= 10 ? "" + hours : "0" + hours) + ":" + (minutes >= 10 ? "" + minutes : "0" + minutes) + ":" + (seconds >= 10 ? "" + seconds : "0" + seconds), 8, 65);
		g.drawString("XP Gained: " + getExperienceTracker().getGainedXP(Skill.FISHING) + " (" + getExperienceTracker().getGainedLevels(Skill.FISHING) + ")", 8, 80);
		g.drawString("Fish caught: " + fishCaught, 8, 95);
		
		// Highlight the fishing spot being used. Just kinda neat. :)
		if (myPlayer().getInteracting() != null) {
			drawTile(client.getBot().getScriptExecutor().getCurrent(), g, myPlayer().getInteracting(), Color.cyan, Color.white, "");
		}
	}

	/**
	 * Move the mouse upwards off the screen, but only if it isn't already off-screen.
	 */
	private void moveMouseOffScreen() {
		if (getMouse().getPosition().y > 0) {
			getMouse().move(random(100, 500), 0);
		}
	}
	
	private int getRandomCloseMouseX() {
		int mouseXPosition = getMouse().getPosition().x;
		return random(mouseXPosition-10, mouseXPosition+10);
	}
	
	@SuppressWarnings("unused")
	private int getRandomCloseMouseY() {
		int mouseYPosition = getMouse().getPosition().y;
		return random(mouseYPosition-10, mouseYPosition+10);
	}
	
	/**
	 * Nudge the mouse down onto the screen then back up, as if keeping the session awake.
	 */
	private void nudgeScreenAwake() {
		getMouse().move(getRandomCloseMouseX(), random(110, 200));
		getMouse().move(getRandomCloseMouseX(), 0);
	}
	
	private void randomizeMouse() throws InterruptedException {
		long now = System.currentTimeMillis();
		long timeSinceLastAntiban = now - lastCheckedAntiban;
		if (timeSinceLastAntiban > random(240000, 290000)) {
			stateLogger("Antiban");
			lastCheckedAntiban = now;
			int i = random(2);
			switch (i) {
			case 0:
				if (getTabs().getOpen() != Tab.SKILLS) {
					getTabs().open(Tab.SKILLS);
					getMouse().move(704, 283);
					sleep(random(1000,1500));
					moveMouseOffScreen();
					break;
				} else {
					nudgeScreenAwake();
					break;
				}
			case 1:
				if (getTabs().getOpen() != Tab.INVENTORY) {
					getTabs().open(Tab.INVENTORY);
					moveMouseOffScreen();
					break;
				} else {
					nudgeScreenAwake();
					break;
				}
			}
		}
		moveMouseOffScreen();
	}

	/**
	 * Gets the nearest bank to the player and opens it.
	 * Waits up to 5-seconds for the bank to open and cuts the wait short when the bank opens.
	 * 
	 * @throws InterruptedException
	 */
	private void openBank() throws InterruptedException {
		getBank().open();
		new ConditionalSleep(5000) {
			@Override
			public boolean condition() throws InterruptedException {
				return getBank().isOpen();
			}
		}.sleep();
		log("Opening Bank");
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
	
	private Boolean inFishingAreas() {
		for (Area a : fishingAreas) {
			if (a.contains(myPlayer()))
				return true;
		}
		return false;
	}
	
	/**
    * Gets the current state of the player.
    * This method is intended to be run on every iteration of the onLoop() method to determine
    * what actions needs to be performed.
    *
    * @return   State   the State that the player is in
    */
    private State getState() {
    	Boolean inventoryFull = getInventory().isFull();
    	Boolean inBank = bankArea.contains(myPlayer());
    	Boolean inFishingArea = inFishingAreas();
    	Boolean isAnimating = myPlayer().isAnimating();
    	Boolean hasRawFish = (getInventory().contains("Raw trout")
    						  || getInventory().contains("Raw salmon"));
    	// Inventory full, banking and cooking states
    	if (inventoryFull
    			&& hasRawFish)
    		return State.COOKING;
        if (inventoryFull
                && !inBank
                && !getBank().isOpen())
            return State.WALK_TO_BANK;
        if (inventoryFull
                && inBank
                && !getBank().isOpen())
            return State.OPEN_BANK;
        if (inventoryFull
                && getBank().isOpen())
            return State.USE_AND_CLOSE_BANK;
        // Inventory NOT full, fishing states
        if (!inventoryFull
                && !inFishingArea
                && !getBank().isOpen())
            return State.WALK_TO_FISHING_SPOT;
        if (!inventoryFull
                && inFishingArea
                && !isAnimating)
            return State.FIND_FISHING_SPOT;
        if (!inventoryFull
                && inFishingArea
                && isAnimating)
            return State.FISHING;
		return State.WAITING;
    }

    /**
    * A loop that continually runs, checking the State of the player and taking action accordingly.
    */
	@Override
	public int onLoop() throws InterruptedException {
        switch (getState()) {
            case WALK_TO_BANK:
            	stateLogger("Walking to bank");
                sleep(random(1000,3000)); // Don't notice immediately when your inventory is full, eh?
                getWalking().webWalk(new Position(bankArea.getRandomPosition()));
                break;
            case OPEN_BANK:
            	openBank();
                break;
            case USE_AND_CLOSE_BANK:
            	stateLogger("Depositing items");
    			getBank().depositAllExcept("Fly fishing rod", "Feather");
    			new ConditionalSleep(5000) {
    				@Override
    				public boolean condition() throws InterruptedException {
    					return (!getInventory().contains("Burnt fish")
    							&& !getInventory().contains("Trout")
    							&& !getInventory().contains("Salmon"));
    				}
    			}.sleep();
    			stateLogger("Closing bank");
    			getBank().close();
                break;
            case WALK_TO_FISHING_SPOT:
            	stateLogger("Walking to fishing spots.");
    			getWalking().webWalk(new Position(northFishingArea.getRandomPosition()));
                break;
            case FIND_FISHING_SPOT:
            	stateLogger("Finding spot.");
    			@SuppressWarnings("unchecked")
				NPC fishingSpot = getNpcs().closest(new Filter<NPC>() {
                    @Override
                    public boolean match(NPC n) {
                        return (n.hasAction("Lure"));
                    }
                });
    			if (fishingSpot != null) {
                    sleep(random(1000,3000)); // Be a little more human about your reaction time.
    				fishingSpot.interact("Lure");
    				inventoryCount = getInventory().getEmptySlots();
    				new ConditionalSleep(5000) {
    					@Override
    					public boolean condition() throws InterruptedException {
    						return myPlayer().isAnimating();
    					}
        			}.sleep();
    			}
                break;
            case FISHING:
            	stateLogger("Catching fish.");
				randomizeMouse(); // antiban
                // @TODO - instead of checking for anything in inventory, we should check for the addition of
                // fish, thereby being more explicit. fishCaught shouldn't go up if something other than fish
                // is in the inventory now for whatever reason (script paused, random, etc).
				if (getInventory().getEmptySlots() != inventoryCount && inFishingAreas()) {
					inventoryCount = getInventory().getEmptySlots();
					fishCaught += 1;
				}
                break;
            case COOKING:
            	stateLogger("Cooking.");
            	// fire = id 26185
            	stop();
            default:
                // I wouldn't expect to ever get here. It's prudent to add a default though.
            	stateLogger("Unexpected condition. Waiting.");
				sleep(1000);
        }
        return random(200,300);
    } // end onLoop()
} // end class main

// @TODO - improve paint
//		http://osbot.org/forum/topic/87697-explvs-dank-paint-tutorial/
//			- Change gained levels to xp/hr
//			- Add all sorts of nice information :)