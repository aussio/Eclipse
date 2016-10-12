package botlib;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;

public class Paint implements Subscriber{

	private long timeStart;
	private Script script;
	private long previousItemCount;
	private long itemsCollected;
	private String[] itemsToTrack;
	private Skill skill;
	private int itemsID;
	private long itemPrice;
	private String state;

	public Paint(Script script, StateLogger logger) {
		this.script = script;
		this.timeStart = System.currentTimeMillis();
		this.itemsToTrack = new String[]{"Raw Shark"};
		this.itemsID = 383;
		this.itemPrice = getPrice(itemsID);
		this.skill = Skill.FISHING;
		this.itemsCollected = 0;
		this.previousItemCount = script.getInventory().getAmount(itemsToTrack);
		this.state = "Brett was here";
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

	/**
	 * Draw the mouse on screen.
	 */
	private void drawMouse(Graphics2D g) {
		g.drawString("x", (int)script.getMouse().getPosition().getX() - 4, (int)script.getMouse().getPosition().getY() + 5);
	}

	private long getPrice(int id){
		int price = 0;

		try {
			URL url = new URL("http://api.rsbuddy.com/grandExchange?a=guidePrice&i=" + id);
			URLConnection con = url.openConnection();
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
			con.setUseCaches(true);
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String[] data = br.readLine().replace("{", "").replace("}", "").split(",");
			br.close();
			price = Integer.parseInt(data[0].split(":")[1]);
		} catch(Exception e){
			e.printStackTrace();
		}
		return price;
	}

	/**
	 * Return only the relevant time string, ignoring hours/days if the "ms" passed-in is less than those increments.
	 * @param ms Number of milliseconds that you wish to format into human-readable time.
	 * @return The formatted time string.
	 */
	public final String formatTime(final long ms){
		long s = ms / 1000, m = s / 60, h = m / 60, d = h / 24;
		s %= 60; m %= 60; h %= 24;

		return d > 0 ? String.format("%02d d %02d hr", d, h) :
			h > 0 ? String.format("%02d hr %02d min", h, m) :
				String.format("%02d min %02d s", m, s);
	}

	/**
	 * Takes an integer and returns the shortened string-representation if possible (k for thousands, m for millions).
	 * @param v The value that you wish to format
	 * @return The formatted string.
	 */
	private final String formatValue(final double v){
		return (v > 1_000_000) ? String.format("%.2fm", ( v / 1_000_000)) :
			(v > 1000) ? String.format("%.1fk", ( v / 1000)) :
				v + "";
	}

	/**
	 * Return decimal percentage to next level for a given skill.
	 * @param skill
	 * @return
	 */
	@SuppressWarnings("unused")
	private final double percentToNextLevel(final Skill skill){
		int curLvl = script.getSkills().getStatic(skill),
				curXP = script.getSkills().getExperience(skill),
				xpCurLvl = script.getSkills().getExperienceForLevel(curLvl),
				xpNextLvl = script.getSkills().getExperienceForLevel(curLvl + 1);

		return (((curXP - xpCurLvl) * 100) / (xpNextLvl - xpCurLvl));
	}

	private long getItemsGathered(String... items) {
		long currentItemCount = script.getInventory().getAmount(items);
		long itemsGained = currentItemCount - this.previousItemCount;
		this.previousItemCount = currentItemCount;
		if ( itemsGained > 0 ) {
			this.itemsCollected += itemsGained;
		}
		return this.itemsCollected;
	}

	public void update(String newState){
		this.state = newState;
	}

	public void onPaint(Graphics2D g) {
		// Get the time
		long timeElapsed = System.currentTimeMillis() - this.timeStart;
		double hoursElapsed = (double) timeElapsed / 3_600_000;
		long goldEarned = itemPrice * getItemsGathered(itemsToTrack);

		// Set the font
		g.setFont(new Font("Trebuchet MS", Font.PLAIN, 14));
		g.setColor(Color.white);

		drawMouse(g);

		// Draw the XP gains paint
		g.drawString(this.state, 8, 50);
		g.drawString("Time Running: " +  formatTime(timeElapsed), 8, 65);
		g.drawString("XP Gained: " + formatValue(script.getExperienceTracker().getGainedXP(this.skill)) + " (" + formatValue(script.getExperienceTracker().getGainedXPPerHour(this.skill)) + "/hr)", 8, 80);
		g.drawString("Time to Level: " +  formatTime(script.getExperienceTracker().getTimeToLevel(this.skill)), 8, 95);
		g.drawString("Fish caught: " + getItemsGathered(itemsToTrack), 8, 110);
		g.drawString("Gold earned: " + formatValue(goldEarned) + " (" + formatValue(goldEarned / hoursElapsed) + "/hr)", 8, 125);


		// Highlight the fishing spot being used. Just kinda neat. :)
		if (script.myPlayer().getInteracting() != null) {
			drawTile(script, g, script.myPlayer().getInteracting(), Color.cyan, Color.white, "");
		}

		// DEBUG: Draw bank, bankDestination, and fishing areas.
		//		if (DEBUG.isPresent()) {
		//			for (Position p : bankArea.getPositions() ) {
		//				drawTile(script, g, p, Color.green, Color.white, "");
		//			}
		//			for (Position p : fishingArea.getPositions() ) {
		//				drawTile(script, g, p, Color.pink, Color.white, "");
		//			}
		//			if (bankDestination != null) {
		//				drawTile(script, g, bankDestination, Color.red, Color.white, "");
		//			}
		//		}
	}


}
