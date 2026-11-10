package ui;

import model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Common window for every role: welcome header, logout and the "My Profile" tab.
 * Subclasses add their own tabs, then call finishSetup().
 */
public abstract class BaseDashboard extends JFrame {

    protected final User user;
    protected final JTabbedPane tabs = new JTabbedPane();
    private final JLabel welcomeLabel = new JLabel();

    protected BaseDashboard(User user, String title) {
        this.user = user;
        setTitle(title);
        setSize(1150, 680);
        setLocationRelativeTo(null);

        // Closing the window logs out instead of ending the program
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        // =========================
        // HEADER
        // =========================

        JPanel headerPanel = new JPanel(new BorderLayout());
        // a thin grey line under the header, then some space around the text
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        updateWelcome();
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    // Reload tables from the text files (called whenever a tab is opened)
    protected abstract void refreshData();

    // Adds the "My Profile" tab, loads the data and shows the window
    protected void finishSetup() {
        tabs.addTab("My Profile", new ProfilePanel(user, this));
        tabs.addChangeListener(e -> refreshData());   // reload the data whenever another tab is opened
        refreshData();
        setVisible(true);
    }

    // Shows the user's name and role at the top of the window
    public void updateWelcome() {
        welcomeLabel.setText("Welcome, " + user.getName() + " (" + user.getRole() + ")");
    }

    // Asks for confirmation, then goes back to the login window
    private void logout() {
        if (UIUtil.confirm(this, "Are you sure you want to logout?")) {
            user.logout();
            dispose();
            new LoginFrame();   // the system keeps running for the next user
        }
    }
}
