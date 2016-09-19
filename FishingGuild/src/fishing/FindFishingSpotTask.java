package fishing;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalSleep;

import botlib.AbstractTask;


public class FindFishingSpotTask extends AbstractTask {

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
	
	public FindFishingSpotTask(MethodProvider api) {
		super(api);
	}

	public void execute() throws InterruptedException {
		@SuppressWarnings("unchecked")
		NPC fishingSpot = api.getNpcs().closest(new Filter<NPC>() {
            @Override
            public boolean match(final NPC n) {
                return (n.hasAction("Net")
                		&& n.hasAction("Harpoon")
                		&& northernFishingSpots.contains(n.getPosition())); // And we're on the northern dock
            }
        });
		if (fishingSpot != null) {
			MethodProvider.sleep(MethodProvider.random(1000,3000)); // Be a little more human about your reaction time.
			fishingSpot.interact("Harpoon");
			int inventoryCount = api.getInventory().getEmptySlots();
			new ConditionalSleep(5000) {
				@Override
				public boolean condition() throws InterruptedException {
					return api.myPlayer().isAnimating();
				}
			}.sleep();
		}

	}

	@Override
	public boolean shouldExecute() {
		// TODO Auto-generated method stub
		return false;
	}

}
