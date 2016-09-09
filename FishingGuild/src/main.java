import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;


@ScriptManifest(author = "Jython",
                info = "Simple Fishing Guild fishing script.",
                name = "Simple Fishing Guild Fisher",
                version = 1.0,
                logo = "")
public class main extends Script {
	private long timeStart;
	private String state = "Initializing..";
	private int inventoryCount = 0;
	private int fishCaught = 0;
	private int lastMouseAction = 0;

    private final Area fishingDocksArea = new Area(2599, 3425, 2600, 3420);
	private final Area guildBankArea = new Area(2585, 3422, 2588, 3418);

  private enum State {
	WALK_TO_BANK,
    OPEN_AND_USE_BANK,
    CLOSE_BANK,
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
	}

	private void randomizeMouse() {
		lastMouseAction++;
		if (lastMouseAction > 4) {
			int i = random(5);
			switch (i) {
			case 0:
			case 1:
				getMouse().moveOutsideScreen();
				break;
			case 2:
				getMouse().moveRandomly();
				break;
			case 3:
				getMouse().moveSlightly();
				lastMouseAction = 3;
				break;
			case 4:
				getMouse().moveVerySlightly();
				break;
			case 5:
				getTabs().open(randomTab());
				if (getTabs().getOpen() == Tab.SKILLS) {
					getMouse().move(704, 283);
				}
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
    * Gets the current state of the player.
    * This method is intended to be run on every iteration of the onLoop() method to determine
    * what actions needs to be performed.
    *
    * @return   State   the State that the player is in
    */
    private State getState() {
        // Inventory full, banking states
        if (getInventory().isFull()
                && !guildBankArea.contains(myPlayer())
                && !getBank().isOpen())
            return State.WALK_TO_BANK;
        if (getInventory().isFull()
                && guildBankArea.contains(myPlayer())
                && !getBank().isOpen())
            return State.OPEN_AND_USE_BANK;
        if (getInventory().isFull()
                && guildBankArea.contains(myPlayer())
                && getBank().isOpen())
            return State.CLOSE_BANK;
        // Inventory NOT full, fishing states
        if (!getInventory().isFull()
                && !fishingDocksArea.contains(myPlayer())
                && !getBank().isOpen())
            return State.WALK_TO_FISHING_SPOT;
        if (!getInventory().isFull()
                && fishingDocksArea.contains(myPlayer())
                && !myPlayer().isAnimating())
            return State.FIND_FISHING_SPOT;
        if (!getInventory().isFull()
                && fishingDocksArea.contains(myPlayer())
                && myPlayer().isAnimating())
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
                state = "Walking to bank";
                getWalking().webWalk(new Position(guildBankArea.getRandomPosition()));
                break;
            case OPEN_AND_USE_BANK:
    			RS2Object bank = getObjects().closest(new Filter<NPC>() {
                    @Override
                    public boolean match(NPC n) {
                        return (n.hasAction("Bank") && n.hasAction("Collect"));
                    }
                });
    			state = "Opening bank";
    			if (bank != null) {
    				if (bank.interact("Bank")) {
    					state = "Depositing items";
    					sleep(1000);
    				}
    			}
                break;
            case CLOSE_BANK:
    			getBank().depositAllExcept("Harpoon");
                // @TODO - replace with conditionalSleep
    			while (getInventory().contains("Tuna") || getInventory().contains("Swordfish")) {
    				sleep(100);
    			}
    			state = "Closing bank";
    			getBank().close();
                break;
            case WALK_TO_FISHING_SPOT:
    			state = "Walking to fishing spots.";
    			getWalking().webWalk(new Position(fishingDocksArea.getRandomPosition()));
                break;
            case FIND_FISHING_SPOT:
    			@SuppressWarnings("unchecked")
				NPC fishingSpot = getNpcs().closest(new Filter<NPC>() {
                    @Override
                    public boolean match(NPC n) {
                        return (n.hasAction("Cage") && n.hasAction("Harpoon"));
                    }
                });
    			state = "Finding spot.";
    			if (fishingSpot != null) {
    				fishingSpot.interact("Harpoon");
                    // @TODO - could this be a conditionalSleep as well?
    				sleep(2000); // time to run to the spot and interact with it
    				inventoryCount = getInventory().getEmptySlots();
    			}
                break;
            case FISHING:
				state = "Catching fish.";
				randomizeMouse(); // antiban
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
                state = "Unexpected condition. Waiting.";
				sleep(1000);
        }
        return random(200,300);
    } // end onLoop()
} // end class main

// @TODO - Paint variables need to reset between runs
// @TODO - Change gained levels to xp/hr
// @TODO - improve antiban
//          - move mouse off screen like I'm AFK
//          - check fishing xp
//          - check inventory if not already on inv tab
// @TODO - improve fishingArea to cover whole dock to not confuse script
