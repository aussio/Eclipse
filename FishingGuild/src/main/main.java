package main;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import tasks.BankUse;
import tasks.BankWalkTo;
import tasks.FindFishingSpot;
import tasks.Fish;
import tasks.FishWalkTo;
import botlib.AbstractTask;
import botlib.GUI;
import botlib.Paint;
import botlib.Publisher;
import botlib.StateLogger;


@ScriptManifest(author = "Jython",
info = "Fishing Guild fisher.",
name = "(guib) Fishing Guild",
version = 1.0,
logo = "")
public class Main extends Script {
	private List<AbstractTask> tasks;
	public final Optional<Boolean> DEBUG = Optional.of(true);
	private Paint paint;
	private StateLogger logger;
	private GUI gui;

	public Main() {
		this.logger = StateLogger.getInstance(this, DEBUG);
		this.gui = new GUI("Select what to fish:",
				new String[]{
				"Shark",
				"Swordfish/Tuna",
				"Lobster"
		});
	}

	/**
	 * Runs once when the script is started.
	 * For this script, the method gets your current Fishing experience and starts our timer.
	 * Both used for the Paint updates.
	 */
	@Override
	public void onStart() {
		this.gui.createGUI();
		this.paint = new Paint(this, logger);
		getExperienceTracker().start(Skill.FISHING);
		// Add all tasks
		this.tasks = Arrays.asList(
				new FishWalkTo(this, this.DEBUG),
				new FindFishingSpot(this, this.DEBUG),
				new Fish(this, this.DEBUG),
				new BankWalkTo(this, this.DEBUG),
				new BankUse(this, this.DEBUG, new String[]{"Harpoon"})
				);

		for (Publisher pub : this.tasks) {
			pub.attach(this.paint, this.logger);
		}
	}

	@Override
	public void onPaint(Graphics2D g) {
		if (this.gui.started){
			this.paint.onPaint(g);
		}
	}

	/**
	 * A loop that continually runs, checking the State of the player and taking action accordingly.
	 */
	@Override
	public int onLoop() throws InterruptedException {
		if (this.gui.started){
			for (AbstractTask task : tasks ) {
				task.executeIfReady();
			}
		}
		return random(200,300);
	}

	@Override
	public void onExit() {
		this.gui.destroyGUI();
	}

} // end class main


// @TODO - add fishing options (and debug option?) GUI
//			- Actually use options selected.
//			- Pass a data object around nicely.
//			- Make sure all elements fit within JFrame
//			- http://osbot.org/forum/topic/91617-tutorial-how-to-pass-data-from-you-gui-to-your-script/
//			- http://osbot.org/forum/topic/88391-my-advice-on-guis/
// @TODO - If you take long enough to find a spot, check the other dock.
// @TODO - If you don't have the fishing item (pot, harpoon) then withdraw one from the bank.
//				 If there's not one in the bank, log out and log that.