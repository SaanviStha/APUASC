package ui;

import exception.ValidationException;
import model.Customer;
import model.User;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * "Edit personal profile" page shared by all roles.
 */
public class ProfilePanel extends JPanel {

    private final User user;
    private final BaseDashboard dashboard;   // the window this panel belongs to

    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmField = new JPasswordField(20);
    private final JTextField carModelField = new JTextField(20);
    private final JTextField carPlateField = new JTextField(20);

    public ProfilePanel(User user, BaseDashboard dashboard) {
        this.user = user;
        this.dashboard = dashboard;

        JPanel form = UIUtil.form("My Profile");
        form.add(new JLabel("User ID:"));
        form.add(new JLabel(user.getUserId()));
        form.add(new JLabel("Username:"));
        form.add(new JLabel(user.getUsername()));
        form.add(new JLabel("Role:"));
        form.add(new JLabel(user.getRole().toString()));
        form.add(new JLabel("Full Name:"));
        form.add(nameField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        form.add(new JLabel("Confirm Password:"));
        form.add(confirmField);
        if (user instanceof Customer) {
            form.add(new JLabel("Car Model:"));
            form.add(carModelField);
            form.add(new JLabel("Car Plate:"));
            form.add(carPlateField);
        }

        JButton saveButton = new JButton("Save");
        JButton resetButton = new JButton("Reset");
        saveButton.addActionListener(e -> save());
        resetButton.addActionListener(e -> loadUser());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);

        JPanel content = new JPanel(new BorderLayout(5, 5));
        content.add(form, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        // FlowLayout keeps the form at its normal size instead of stretching it
        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        add(content);

        loadUser();
    }

    // Puts the user's current details into the text boxes
    private void loadUser() {
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        passwordField.setText(user.getPassword());
        confirmField.setText(user.getPassword());
        if (user instanceof Customer) {
            carModelField.setText(((Customer) user).getCarModel());
            carPlateField.setText(((Customer) user).getCarPlate());
        }
    }

    // Checks the new details and saves them to the users file
    private void save() {
        try {
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());
            if (!password.equals(confirm)) {
                throw new ValidationException("Password and Confirm Password do not match.");
            }
            if (user instanceof Customer) {
                ((Customer) user).editProfile(nameField.getText(), emailField.getText(),
                        phoneField.getText(), password, carModelField.getText(), carPlateField.getText());
            } else {
                user.editProfile(nameField.getText(), emailField.getText(), phoneField.getText(), password);
            }
            loadUser();
            dashboard.updateWelcome();   // show the new name in the window header
            UIUtil.info(this, "Profile updated successfully.");
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }
}
