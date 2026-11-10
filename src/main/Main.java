package main;

import ui.LoginFrame;

import javax.swing.UIManager;

/**
 * Starting point of the APU Automotive Service Centre application.
 */
public class Main {

    public static void main(String[] args) {
        // Use the normal look of the computer (e.g. Windows buttons and tables)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Make text boxes use the same font as the labels
            UIManager.put("TextArea.font", UIManager.getFont("Label.font"));
        } catch (Exception e) {
            System.out.println("Using the default Java look instead.");
        }
        new LoginFrame();
    }
}
