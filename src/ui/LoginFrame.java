package ui;

import exception.InvalidLoginException;
import model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * The first window of the program, where every user logs in.
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckBox;

    public LoginFrame() {
        setTitle("APU Automotive Service Centre - Login");
        setSize(470, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createUI();

        setVisible(true);
    }

    // Builds the title, the username/password form and the buttons
    private void createUI() {

        // =========================
        // MAIN PANEL
        // =========================

        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 15, 40));

        // =========================
        // TITLE
        // =========================

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 0, 10));
        JLabel titleLabel = new JLabel("APU AUTOMOTIVE SERVICE CENTRE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel subtitleLabel = new JLabel("Please log in to continue", SwingConstants.CENTER);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // =========================
        // FORM PANEL
        // =========================

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 15));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        showPasswordCheckBox = new JCheckBox("Show Password");

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel(""));
        formPanel.add(showPasswordCheckBox);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // =========================
        // BUTTON PANEL
        // =========================

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("Login");
        JButton clearButton = new JButton("Clear");
        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // =========================
        // EVENTS
        // =========================

        loginButton.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());   // pressing Enter in the password box also logs in
        clearButton.addActionListener(e -> clearFields());
        showPasswordCheckBox.addActionListener(e -> showOrHidePassword());
        passwordField.setEchoChar('*');
    }

    // Empties both text boxes
    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }

    // Shows the password as normal text when the box is ticked, or as * when it is not
    private void showOrHidePassword() {
        if (showPasswordCheckBox.isSelected()) {
            passwordField.setEchoChar((char) 0);
        } else {
            passwordField.setEchoChar('*');
        }
    }

    // Checks the username and password, then opens the dashboard of that user's role
    private void login() {
        try {
            User user = User.login(usernameField.getText(), new String(passwordField.getPassword()));
            dispose();
            user.openDashboard();   // polymorphism: each role opens its own dashboard
        } catch (InvalidLoginException ex) {
            UIUtil.validation(this, ex.getMessage());
            passwordField.setText("");
        }
    }
}
