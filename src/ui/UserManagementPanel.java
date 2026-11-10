package ui;

import exception.ValidationException;
import model.CounterStaff;
import model.Customer;
import model.Manager;
import model.Role;
import model.Technician;
import model.User;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * Create / Read / Update / Delete screen for user accounts.
 * Used by managers (staff accounts) and counter staff (customers).
 */
public class UserManagementPanel extends JPanel {

    private final UserCrudHandler handler;
    private final boolean customerMode;

    private final DefaultTableModel model;
    private final JTable table;

    private final JTextField idField = new JTextField(15);
    private final JComboBox<Role> roleCombo;
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JTextField nameField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);
    private final JTextField phoneField = new JTextField(15);
    private final JTextField carModelField = new JTextField(15);
    private final JTextField carPlateField = new JTextField(15);

    public UserManagementPanel(String title, UserCrudHandler handler, Role... roles) {
        this.handler = handler;
        // The panel is in "customer mode" when it is only used for customers (by counter staff)
        this.customerMode = roles.length == 1 && roles[0] == Role.CUSTOMER;
        this.roleCombo = new JComboBox<>(roles);

        setLayout(new BorderLayout(10, 10));

        // =========================
        // TABLE (READ)
        // =========================

        if (customerMode) {
            model = UIUtil.tableModel("ID", "Username", "Name", "Email", "Phone", "Car Model", "Car Plate");
        } else {
            model = UIUtil.tableModel("ID", "Username", "Name", "Email", "Phone", "Role");
        }
        table = UIUtil.table(model);
        // When a row is clicked, copy that record into the form
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromSelection());

        JPanel tablePanel = new JPanel(new BorderLayout());
        UIUtil.titled(tablePanel, title);
        tablePanel.add(UIUtil.scroll(table), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // =========================
        // FORM (CREATE / UPDATE / DELETE)
        // =========================

        String formTitle = "Staff Details";
        if (customerMode) {
            formTitle = "Customer Details";
        }
        JPanel form = UIUtil.form(formTitle);
        idField.setEditable(false);

        form.add(new JLabel("ID:"));
        form.add(idField);
        if (!customerMode) {
            form.add(new JLabel("Role:"));
            form.add(roleCombo);
        }
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        form.add(new JLabel("Full Name:"));
        form.add(nameField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);
        if (customerMode) {
            form.add(new JLabel("Car Model:"));
            form.add(carModelField);
            form.add(new JLabel("Car Plate:"));
            form.add(carPlateField);
        }

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        addButton.addActionListener(e -> addUser());
        updateButton.addActionListener(e -> updateUser());
        deleteButton.addActionListener(e -> deleteUser());
        clearButton.addActionListener(e -> clearForm());

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(form, BorderLayout.NORTH);
        rightPanel.add(buttonPanel, BorderLayout.CENTER);

        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add(rightPanel, BorderLayout.NORTH);
        add(eastPanel, BorderLayout.EAST);
    }

    // =========================
    // ACTIONS
    // =========================

    // Reloads the table from the users file
    public void refresh() {
        model.setRowCount(0);   // remove the old rows
        for (User user : handler.list()) {
            if (user instanceof Customer) {
                Customer customer = (Customer) user;
                model.addRow(new Object[]{customer.getUserId(), customer.getUsername(), customer.getName(), customer.getEmail(),
                        customer.getPhone(), customer.getCarModel(), customer.getCarPlate()});
            } else {
                model.addRow(new Object[]{user.getUserId(), user.getUsername(), user.getName(), user.getEmail(),
                        user.getPhone(), user.getRole()});
            }
        }
        UIUtil.fitColumns(table);
    }

    // Creates a new account from the form
    private void addUser() {
        if (!idField.getText().isEmpty()) {
            UIUtil.validation(this, "This record already exists. Click 'Clear' first to add a new one.");
            return;
        }
        try {
            User user = buildUser(null);
            handler.add(user);
            refresh();
            clearForm();
            UIUtil.info(this, user.getRole() + " added successfully. ID: " + user.getUserId());
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }

    // Saves the changes made to the selected account
    private void updateUser() {
        String id = idField.getText();
        if (id.isEmpty()) {
            UIUtil.validation(this, "Please select a record from the table first.");
            return;
        }
        try {
            handler.update(buildUser(id));
            refresh();
            clearForm();
            UIUtil.info(this, "Record " + id + " updated successfully.");
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }

    // Deletes the selected account after asking for confirmation
    private void deleteUser() {
        String id = idField.getText();
        if (id.isEmpty()) {
            UIUtil.validation(this, "Please select a record from the table first.");
            return;
        }
        if (!UIUtil.confirm(this, "Are you sure you want to delete " + nameField.getText() + " (" + id + ")?")) {
            return;
        }
        try {
            handler.delete(id);
            refresh();
            clearForm();
            UIUtil.info(this, "Record " + id + " deleted successfully.");
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }

    // Creates an object of the right subclass (Manager, CounterStaff, Technician or Customer) from the form
    private User buildUser(String id) {
        Role role = Role.CUSTOMER;
        if (!customerMode) {
            role = (Role) roleCombo.getSelectedItem();
        }
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        switch (role) {
            case MANAGER:
                return new Manager(id, username, password, name, email, phone);
            case COUNTER_STAFF:
                return new CounterStaff(id, username, password, name, email, phone);
            case TECHNICIAN:
                return new Technician(id, username, password, name, email, phone);
            default:
                return new Customer(id, username, password, name, email, phone,
                        carModelField.getText().trim(), carPlateField.getText().trim());
        }
    }

    // Copies the selected row of the table into the form
    private void fillFormFromSelection() {
        String id = UIUtil.selectedValue(table, 0);
        if (id == null) {
            return;
        }
        for (User user : handler.list()) {
            if (user.getUserId().equals(id)) {
                idField.setText(user.getUserId());
                roleCombo.setSelectedItem(user.getRole());
                roleCombo.setEnabled(false);
                usernameField.setText(user.getUsername());
                passwordField.setText(user.getPassword());
                nameField.setText(user.getName());
                emailField.setText(user.getEmail());
                phoneField.setText(user.getPhone());
                if (user instanceof Customer) {
                    carModelField.setText(((Customer) user).getCarModel());
                    carPlateField.setText(((Customer) user).getCarPlate());
                }
                return;
            }
        }
    }

    // Empties the form so a new record can be added
    private void clearForm() {
        table.clearSelection();
        roleCombo.setEnabled(true);
        for (JTextField f : new JTextField[]{idField, usernameField, passwordField, nameField,
                emailField, phoneField, carModelField, carPlateField}) {
            f.setText("");
        }
    }
}
