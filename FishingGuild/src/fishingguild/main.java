package fishingguild;
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
                info = "Simple Fishing Guild fisher.",
                name = "Fishing Guild Sharks",
                version = 1.0,
                logo = "")
public class main extends Script {
	private final Boolean DEBUG = false;
	private long timeStart;
	private long lastCheckedAntiban;
	private String state = "Initializing..";
	private int inventoryCount = 0;
	private int fishCaught = 0;
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
    			}
    		);
    private final Area northernFishingSpots = new Area(2598, 3419, 2605, 3426);
    // Perfect guild bank area. :)
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
				}
			);
	// Only useful to instantiate here for debug paint.
	private Position bankDestination;

  private enum State {
	WALK_TO_BANK,
	OPEN_BANK,
	USE_AND_CLOSE_BANK,
    WALK_TO_FISHING_SPOT,
    FIND_FISHING_SPOT,
    FISHING,
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
	
	public void drawTile(Script script, Graphics g, Position position, Color tileColor, Color textColor, String s) {
		Polygon polygon;
		    if (position != null && (polygon = position.getPolygon(script.getBot(), position.getTileHeight(script.getBot()))) != null) {
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
		Script script = client.getBot().getScriptExecutor().getCurrent();
		if (myPlayer().getInteracting() != null) {
			drawTile(script, g, myPlayer().getInteracting(), Color.cyan, Color.white, "");
		}
		
		// DEBUG: Draw bank, bankDestination, and fishing areas.
		if (DEBUG == true) {
			for (Position p : bankArea.getPositions() ) {
				drawTile(script, g, p, Color.green, Color.white, "");
			}
			for (Position p : fishingArea.getPositions() ) {
				drawTile(script, g, p, Color.pink, Color.white, "");
			}
			if (bankDestination != null)
				drawTile(script, g, bankDestination, Color.red, Color.white, "");
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
    	Boolean onDock = fishingArea.contains(myPlayer());
    	Boolean isAnimating = myPlayer().isAnimating();
    	// Inventory full, banking states
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
                && !onDock
                && !getBank().isOpen())
            return State.WALK_TO_FISHING_SPOT;
        if (!inventoryFull
                && onDock
                && !isAnimating)
            return State.FIND_FISHING_SPOT;
        if (!inventoryFull
                && onDock
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
                bankDestination = new Position(bankArea.getRandomPosition());
                getWalking().walk(bankDestination);
                break;
            case OPEN_BANK:
            	bankDestination = null;
            	openBank();
                break;
            case USE_AND_CLOSE_BANK:
            	stateLogger("Depositing items");
    			getBank().depositAllExcept("Harpoon");
    			new ConditionalSleep(5000) {
    				@Override
    				public boolean condition() throws InterruptedException {
    					return (!getInventory().contains("Shark"));
    				}
    			}.sleep();
    			stateLogger("Closing bank");
    			getBank().close();
                break;
            case WALK_TO_FISHING_SPOT:
            	stateLogger("Walking to fishing spots.");
    			getWalking().walk(new Position(fishingArea.getRandomPosition()));
                break;
            case FIND_FISHING_SPOT:
            	stateLogger("Finding spot.");
    			@SuppressWarnings("unchecked")
				NPC fishingSpot = getNpcs().closest(new Filter<NPC>() {
                    @Override
                    public boolean match(NPC n) {
                        return (n.hasAction("Net")
                        		&& n.hasAction("Harpoon")
                        		&& northernFishingSpots.contains(n.getPosition())); // And we're on the northern dock
                    }
                });
    			if (fishingSpot != null) {
                    sleep(random(1000,3000)); // Be a little more human about your reaction time.
    				fishingSpot.interact("Harpoon");
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
				if (getInventory().getEmptySlots() != inventoryCount && fishingArea.contains(myPlayer())) {
					inventoryCount = getInventory().getEmptySlots();
					fishCaught += 1;
				}
                break;
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
// @TODO - add fishing options (and debug option?)
// 		http://osbot.org/forum/topic/87731-explvs-dank-gui-tutorial/
//			- lobsters
//			- swordfish/tuna
//			- sharks
// @TODO - If you take long enough to find a spot, check the other dock.
// @TODO - If you don't have the fishing item (pot, harpoon) then withdraw one from the bank.
//				 If there's not one in the bank, log out and log that.