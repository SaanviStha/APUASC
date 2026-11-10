package ui;

import exception.ValidationException;
import model.Appointment;
import model.AppointmentStatus;
import model.CounterStaff;
import model.Customer;
import model.Payment;
import model.Role;
import model.ServiceType;
import model.Technician;
import model.User;
import storage.DataStore;
import util.DateUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CounterStaffDashboard extends BaseDashboard {

    private final CounterStaff staff;

    private UserManagementPanel customerPanel;

    // Assign task
    private JComboBox<Customer> customerCombo;
    private JComboBox<ServiceType> typeCombo;
    private JTextField dateField;
    private JComboBox<String> timeCombo;
    private JComboBox<Technician> techCombo;
    private JLabel priceLabel;
    private JTextArea taskArea;

    // All appointments
    private DefaultTableModel aptModel;
    private JTable aptTable;

    // Payments
    private DefaultTableModel unpaidModel;
    private JTable unpaidTable;
    private JComboBox<String> methodCombo;
    private DefaultTableModel paymentModel;
    private JTable paymentTable;

    public CounterStaffDashboard(CounterStaff staff) {
        super(staff, "Counter Staff Dashboard");
        this.staff = staff;

        tabs.addTab("Customers", createCustomerTab());
        tabs.addTab("Assign Task", createAssignTaskTab());
        tabs.addTab("Assigned Tasks", createAppointmentsTab());
        tabs.addTab("Payments", createPaymentTab());
        finishSetup();
    }

    @Override
    // Reloads every tab from the text files
    protected void refreshData() {
        customerPanel.refresh();
        loadCustomerCombo();
        loadAppointments();
        loadPayments();
    }

    // =====================================================
    // TAB 1: CUSTOMERS
    // =====================================================

    // Tab 1: create, view, update and delete customers
    private JPanel createCustomerTab() {
        customerPanel = new UserManagementPanel("Registered Customers", new UserCrudHandler() {
            @Override
            public List<? extends User> list() {
                return staff.viewCustomers();
            }

            @Override
            public void add(User user) throws ValidationException {
                staff.addCustomer((Customer) user);
            }

            @Override
            public void update(User user) throws ValidationException {
                staff.updateCustomer((Customer) user);
            }

            @Override
            public void delete(String userId) throws ValidationException {
                staff.deleteCustomer(userId);
            }
        }, Role.CUSTOMER);
        return customerPanel;
    }

    // =====================================================
    // TAB 2: ASSIGN TASK (create an appointment and assign it to a technician)
    // =====================================================

    // Tab 2: create an appointment and assign it to a free technician
    private JPanel createAssignTaskTab() {
        customerCombo = new JComboBox<>();
        typeCombo = new JComboBox<>(ServiceType.values());
        dateField = new JTextField(LocalDate.now().plusDays(1).toString());
        timeCombo = new JComboBox<>();
        for (LocalTime t = DateUtil.OPENING; t.isBefore(DateUtil.CLOSING); t = t.plusMinutes(30)) {
            timeCombo.addItem(t.toString());
        }
        techCombo = new JComboBox<>();
        priceLabel = new JLabel();
        taskArea = new JTextArea(3, 20);
        taskArea.setLineWrap(true);
        taskArea.setWrapStyleWord(true);

        JButton checkButton = new JButton("Check Available Technicians");
        JButton assignButton = new JButton("Assign Task");
        JButton clearButton = new JButton("Clear");

        JPanel form = UIUtil.form("Assign Task to Technician");
        form.add(new JLabel("Customer:"));
        form.add(customerCombo);
        form.add(new JLabel("Service Type:"));
        form.add(typeCombo);
        form.add(new JLabel("Price:"));
        form.add(priceLabel);
        form.add(new JLabel("Date (yyyy-MM-dd):"));
        form.add(dateField);
        form.add(new JLabel("Start Time:"));
        form.add(timeCombo);
        form.add(new JLabel(""));
        form.add(checkButton);
        form.add(new JLabel("Technician:"));
        form.add(techCombo);

        JPanel taskPanel = new JPanel(new BorderLayout());
        UIUtil.titled(taskPanel, "Task Description (what the technician should do)");
        taskPanel.add(UIUtil.scroll(taskArea));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(assignButton);
        buttonPanel.add(clearButton);

        JLabel note = new JLabel("<html>Opening hours: Monday - Saturday, 9:00 - 18:00.<br>"
                + "Normal service = 1 hour, Major service = 3 hours.</html>");

        // If any detail changes, availability must be checked again
        typeCombo.addActionListener(e -> serviceTypeChanged());
        timeCombo.addActionListener(e -> techCombo.removeAllItems());
        dateField.addActionListener(e -> findTechnicians());   // Enter = check availability
        checkButton.addActionListener(e -> findTechnicians());
        assignButton.addActionListener(e -> assignTask());
        clearButton.addActionListener(e -> clearTaskForm());
        updatePrice();

        JPanel content = new JPanel(new BorderLayout(5, 10));
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(note, BorderLayout.SOUTH);
        content.add(form, BorderLayout.NORTH);
        content.add(taskPanel, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        JPanel page = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        page.add(content);
        return page;
    }

    // Fills the customer drop-down list
    private void loadCustomerCombo() {
        Customer selected = (Customer) customerCombo.getSelectedItem();
        customerCombo.removeAllItems();
        for (Customer customer : staff.viewCustomers()) {
            customerCombo.addItem(customer);
            if (selected != null && customer.getUserId().equals(selected.getUserId())) {
                customerCombo.setSelectedItem(customer);
            }
        }
    }

    // Called when the service type changes: show the new price and check availability again
    private void serviceTypeChanged() {
        techCombo.removeAllItems();
        updatePrice();
    }

    // Shows the price of the selected service type
    private void updatePrice() {
        ServiceType type = (ServiceType) typeCombo.getSelectedItem();
        priceLabel.setText(UIUtil.money(DataStore.prices().getPrice(type).getAmount()));
    }

    // Joins the date box and the time list into one date-time value
    private LocalDateTime selectedStart() throws ValidationException {
        return DateUtil.parseInput(dateField.getText().trim() + " " + timeCombo.getSelectedItem());
    }

    // Finds the technicians who are free for the whole service
    private void findTechnicians() {
        techCombo.removeAllItems();
        try {
            ServiceType type = (ServiceType) typeCombo.getSelectedItem();
            LocalDateTime start = selectedStart();
            DateUtil.validateBookingTime(start, type);
            List<Technician> available = staff.findAvailableTechs(type, start);
            if (available.isEmpty()) {
                UIUtil.validation(this, "No technician is free at that time. Please choose another time.");
                return;
            }
            for (Technician technician : available) {
                techCombo.addItem(technician);
            }
            UIUtil.info(this, available.size() + " technician(s) available. Select one, describe the task and click Assign Task.");
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }

    // Resets the Assign Task form
    private void clearTaskForm() {
        dateField.setText(LocalDate.now().plusDays(1).toString());
        timeCombo.setSelectedIndex(0);
        typeCombo.setSelectedIndex(0);
        techCombo.removeAllItems();
        taskArea.setText("");
    }

    // Saves the appointment and assigns it to the chosen technician
    private void assignTask() {
        Customer customer = (Customer) customerCombo.getSelectedItem();
        Technician tech = (Technician) techCombo.getSelectedItem();
        if (customer == null) {
            UIUtil.validation(this, "Please select a customer. Register the customer first if needed.");
            return;
        }
        if (tech == null) {
            UIUtil.validation(this, "Please click 'Check Available Technicians' and select a technician.");
            return;
        }
        try {
            Appointment appointment = staff.createAppointment(customer.getUserId(),
                    (ServiceType) typeCombo.getSelectedItem(), selectedStart(), tech.getUserId(), taskArea.getText());
            UIUtil.info(this, "Task assigned successfully. Appointment ID: " + appointment.getAppointmentId() + "\n"
                    + "Customer: " + customer.getName() + "\n"
                    + "Technician: " + tech.getName() + "\n"
                    + "Time: " + DateUtil.format(appointment.getStartTime()) + " to " + DateUtil.format(appointment.getEndTime()) + "\n"
                    + "Price: " + UIUtil.money(appointment.getPrice()));
            clearTaskForm();
            loadAppointments();
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }

    // =====================================================
    // TAB 3: ALL APPOINTMENTS
    // =====================================================

    // Tab 3: list of all assigned tasks
    private JPanel createAppointmentsTab() {
        aptModel = UIUtil.tableModel("ID", "Customer", "Service", "Start", "End", "Technician", "Task",
                "Status", "Paid");
        aptTable = UIUtil.table(aptModel);

        JButton reassignButton = new JButton("Reassign Technician");
        reassignButton.addActionListener(e -> reassign());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(reassignButton);

        JPanel page = new JPanel(new BorderLayout());
        UIUtil.titled(page, "All Assigned Tasks");
        page.add(UIUtil.scroll(aptTable), BorderLayout.CENTER);
        page.add(buttonPanel, BorderLayout.SOUTH);
        return page;
    }

    // Fills the assigned tasks table
    private void loadAppointments() {
        aptModel.setRowCount(0);
        for (Appointment appointment : DataStore.appointments().readAll()) {
            String paid = "No";
            if (DataStore.payments().findByAppointment(appointment.getAppointmentId()) != null) {
                paid = "Yes";
            }
            aptModel.addRow(new Object[]{appointment.getAppointmentId(), UIUtil.idAndName(appointment.getCustomerId()),
                    appointment.getServiceType().getDisplayName(), DateUtil.format(appointment.getStartTime()),
                    DateUtil.format(appointment.getEndTime()), UIUtil.idAndName(appointment.getTechnicianId()),
                    appointment.getTaskDescription(), appointment.getStatus(), paid});
        }
        UIUtil.fitColumns(aptTable);
    }

    // Moves an unfinished appointment to another free technician
    private void reassign() {
        String aptId = UIUtil.selectedValue(aptTable, 0);
        if (aptId == null) {
            UIUtil.validation(this, "Please select an appointment.");
            return;
        }
        Appointment appointment = DataStore.appointments().findById(aptId);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            UIUtil.validation(this, "A completed appointment cannot be reassigned.");
            return;
        }
        List<Technician> available = staff.findAvailableTechs(appointment.getServiceType(), appointment.getStartTime(), aptId);
        // Do not offer the technician who already has this appointment
        List<Technician> options = new ArrayList<>();
        for (Technician technician : available) {
            if (!technician.getUserId().equals(appointment.getTechnicianId())) {
                options.add(technician);
            }
        }
        if (options.isEmpty()) {
            UIUtil.validation(this, "No other technician is free at that time.");
            return;
        }
        Technician chosen = (Technician) JOptionPane.showInputDialog(this,
                "Choose a new technician for " + aptId + ":", "Reassign Technician",
                JOptionPane.QUESTION_MESSAGE, null, options.toArray(), options.get(0));
        if (chosen == null) {
            return;
        }
        try {
            staff.assignTechnician(aptId, chosen.getUserId());
            loadAppointments();
            UIUtil.info(this, aptId + " is now assigned to " + chosen.getName() + ".");
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }

    // =====================================================
    // TAB 4: PAYMENTS & RECEIPTS
    // =====================================================

    // Tab 4: collect payments and view receipts
    private JPanel createPaymentTab() {
        unpaidModel = UIUtil.tableModel("Appointment", "Customer", "Service", "Date", "Technician", "Amount");
        unpaidTable = UIUtil.table(unpaidModel);
        methodCombo = new JComboBox<>(Payment.METHODS);
        JButton collectButton = new JButton("Collect Payment");
        collectButton.addActionListener(e -> collectPayment());

        JPanel payButtons = new JPanel(new FlowLayout());
        payButtons.add(new JLabel("Payment Method:"));
        payButtons.add(methodCombo);
        payButtons.add(collectButton);

        JPanel unpaidPanel = new JPanel(new BorderLayout());
        UIUtil.titled(unpaidPanel, "Completed Services Waiting for Payment");
        unpaidPanel.add(UIUtil.scroll(unpaidTable), BorderLayout.CENTER);
        unpaidPanel.add(payButtons, BorderLayout.SOUTH);

        paymentModel = UIUtil.tableModel("Payment ID", "Appointment", "Customer", "Amount", "Method",
                "Paid At", "Received By");
        paymentTable = UIUtil.table(paymentModel);
        JButton receiptButton = new JButton("View Receipt");
        receiptButton.addActionListener(e -> showReceipt());
        JPanel historyButtons = new JPanel(new FlowLayout());
        historyButtons.add(receiptButton);

        JPanel historyPanel = new JPanel(new BorderLayout());
        UIUtil.titled(historyPanel, "Payment History");
        historyPanel.add(UIUtil.scroll(paymentTable), BorderLayout.CENTER);
        historyPanel.add(historyButtons, BorderLayout.SOUTH);

        JPanel page = new JPanel(new GridLayout(2, 1, 10, 10));
        page.add(unpaidPanel);
        page.add(historyPanel);
        return page;
    }

    // Fills the unpaid jobs table and the payment history table
    private void loadPayments() {
        unpaidModel.setRowCount(0);
        for (Appointment appointment : DataStore.appointments().findByStatus(AppointmentStatus.COMPLETED)) {
            if (DataStore.payments().findByAppointment(appointment.getAppointmentId()) == null) {
                unpaidModel.addRow(new Object[]{appointment.getAppointmentId(), UIUtil.nameOf(appointment.getCustomerId()),
                        appointment.getServiceType().getDisplayName(), DateUtil.format(appointment.getStartTime()),
                        UIUtil.nameOf(appointment.getTechnicianId()), UIUtil.money(appointment.getPrice())});
            }
        }
        paymentModel.setRowCount(0);
        for (Payment payment : DataStore.payments().readAll()) {
            Appointment appointment = DataStore.appointments().findById(payment.getAppointmentId());
            paymentModel.addRow(new Object[]{payment.getPaymentId(), payment.getAppointmentId(),
                    customerName(appointment), UIUtil.money(payment.getAmount()),
                    payment.getMethod(), DateUtil.format(payment.getPaidAt()), UIUtil.nameOf(payment.getCounterStaffId())});
        }
        UIUtil.fitColumns(unpaidTable);
        UIUtil.fitColumns(paymentTable);
    }

    // Returns the name of the customer of an appointment, or "-" if the appointment was not found
    private String customerName(Appointment a) {
        if (a == null) {
            return "-";
        }
        return UIUtil.nameOf(a.getCustomerId());
    }

    // Collects the payment for the selected completed job and shows the receipt
    private void collectPayment() {
        String aptId = UIUtil.selectedValue(unpaidTable, 0);
        if (aptId == null) {
            UIUtil.validation(this, "Please select an appointment from the table.");
            return;
        }
        Appointment appointment = DataStore.appointments().findById(aptId);
        String method = (String) methodCombo.getSelectedItem();
        if (!UIUtil.confirm(this, "Collect " + UIUtil.money(appointment.getPrice()) + " by " + method
                + " for appointment " + aptId + "?")) {
            return;
        }
        try {
            Payment payment = staff.collectPayment(aptId, method);
            loadPayments();
            loadAppointments();
            new ReceiptDialog(this, staff.generateReceipt(payment.getPaymentId()));
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }

    // Shows the receipt of the selected payment
    private void showReceipt() {
        String paymentId = UIUtil.selectedValue(paymentTable, 0);
        if (paymentId == null) {
            UIUtil.validation(this, "Please select a payment from the table.");
            return;
        }
        try {
            new ReceiptDialog(this, staff.generateReceipt(paymentId));
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }
}
