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
	private long timeStart;
	private String state = "Initializing..";
	private int inventoryCount = 0;
	private int fishCaught = 0;
	private int lastMouseAction = 0;

    private final Area fishingDocksArea = new Area(2599, 3420, 2604, 3425);
	private final Area guildBankArea = new Area(2591, 3419, 2586, 3418);

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

	private void randomizeMouse() {
		lastMouseAction++;
		if (lastMouseAction > 4) {
			int i = random(5);
			switch (i) {
			case 5:
				getTabs().open(randomTab());
				if (getTabs().getOpen() == Tab.SKILLS) {
					getMouse().move(704, 283);
				}
            default:
                getMouse().moveOutsideScreen();
                break;
			}

			lastMouseAction = 0;
		}
	}

	private Tab randomTab() {
		int i = random(6);
		switch(i) {
		case 0:
		case 1:
			return Tab.INVENTORY;
		case 2:
			return Tab.EQUIPMENT;
		case 3:
			return Tab.ATTACK;
		case 4:
			return Tab.SKILLS;
		case 5:
			return Tab.FRIENDS;
		case 6:
			return Tab.QUEST;
		}
		return Tab.SKILLS;
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
    	Boolean inBank = guildBankArea.contains(myPlayer());
    	Boolean onDock = fishingDocksArea.contains(myPlayer());
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
                getWalking().webWalk(new Position(guildBankArea.getRandomPosition()));
                break;
            case OPEN_BANK:
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
    			getWalking().webWalk(new Position(fishingDocksArea.getRandomPosition()));
                break;
            case FIND_FISHING_SPOT:
            	stateLogger("Finding spot.");
    			@SuppressWarnings("unchecked")
				NPC fishingSpot = getNpcs().closest(new Filter<NPC>() {
                    @Override
                    public boolean match(NPC n) {
                        return (n.hasAction("Net")
                        		&& n.hasAction("Harpoon")
                        		&& n.getPosition().getY() > 3418); // And we're on the northern dock
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
				// randomizeMouse(); // antiban
                // @TODO - instead of checking for anything in inventory, we should check for the addition of
                // fish, thereby being more explicit. fishCaught shouldn't go up if something other than fish
                // is in the inventory now for whatever reason (script paused, random, etc).
				if (getInventory().getEmptySlots() != inventoryCount && fishingDocksArea.contains(myPlayer())) {
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
// @TODO - improve antiban
//          - move mouse off screen like I'm AFK
//          - check fishing xp
//          - check inventory if not already on inv tab
// @TODO - add fishing options
// 		http://osbot.org/forum/topic/87731-explvs-dank-gui-tutorial/
//			- lobsters
//			- swordfish/tuna
//			- sharks
// @TODO - improve areas
//			- Using some of the drawing snippets, fill in the area that is defined to check that it covers
//			  what it needs to cover. With that assurance, make the bank and dock more precise.