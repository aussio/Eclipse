package botlib;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GUI {

	public JFrame gui;
	private String selectorLabel;
	private String[] selectionOptions;
	public String selection;
	public Boolean started = false;

	public GUI(String selectorLabel, String[] selectionOptions) {
		this.selectorLabel = selectorLabel;
		this.selectionOptions = selectionOptions;
	}

	public void createGUI(){

		// Declare two constants for width and height of the GUI
		final int GUI_WIDTH = 350, GUI_HEIGHT = 75;

		// Get the size of the screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// Calculating x and y coordinates
		final int gX = (int) (screenSize.getWidth() / 2) - (GUI_WIDTH / 2);
		final int gY = (int) (screenSize.getHeight() / 2) - (GUI_HEIGHT / 2);

		// Create a new JFrame with the title "GUI"
		gui = new JFrame("GUI");

		// Set the x coordinate, y coordinate, width and height of the GUI
		gui.setBounds(gX, gY, GUI_WIDTH, GUI_HEIGHT);

		// Disable resizing
		gui.setResizable(false);

		// Create a sub container JPanel
		JPanel panel = new JPanel();

		// Add it to the GUI
		gui.add(panel);

		JLabel label = new JLabel(selectorLabel); // Create a label
		label.setForeground(Color.white); // Set text color to white
		panel.add(label); // Add it to the JPanel

		// Create a select box for tree options
		JComboBox<String> selector = new JComboBox<>(selectionOptions);

		// Add an action listener, to listen for user's selections, assign to a variable called selectedTree on selection.
		selector.addActionListener(e -> this.selection = selector.getSelectedItem().toString());

		// Add the select box to the JPanel
		panel.add(selector);

		JButton startButton = new JButton("Start");
		startButton.addActionListener(e -> {
			this.started  = true;
			gui.setVisible(false);
		});
		panel.add(startButton);

		// Make the GUI visible
		gui.setVisible(true);
	}

	public void destroyGUI(){
		if (this.gui != null) {
			gui.setVisible(false); // Hide it
			gui.dispose(); // Dispose
		}
	}
}
