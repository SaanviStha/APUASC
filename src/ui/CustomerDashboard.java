package ui;

import exception.ValidationException;
import model.Appointment;
import model.Comment;
import model.Customer;
import model.Feedback;
import model.Payment;
import model.User;
import storage.DataStore;
import util.DateUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class CustomerDashboard extends BaseDashboard {

    private final Customer customer;

    private DefaultTableModel historyModel;
    private JTable historyTable;
    private JTextArea feedbackArea;
    private JComboBox<User> staffCombo;
    private JTextArea commentArea;

    private DefaultTableModel paymentModel;
    private JTable paymentTable;
    private JLabel totalLabel;

    public CustomerDashboard(Customer customer) {
        super(customer, "Customer Dashboard");
        this.customer = customer;

        tabs.addTab("Service History", createHistoryTab());
        tabs.addTab("Payment History", createPaymentTab());
        finishSetup();
    }

    @Override
    // Reloads both tabs from the text files
    protected void refreshData() {
        loadHistory();
        loadPayments();
    }

    // =====================================================
    // TAB 1: SERVICE HISTORY, FEEDBACK AND COMMENTS
    // =====================================================

    // Tab 1: service history, feedback and comments
    private JPanel createHistoryTab() {
        historyModel = UIUtil.tableModel("ID", "Service", "Start", "End", "Technician", "Status", "Price", "Paid");
        historyTable = UIUtil.table(historyModel);
        // When a row is clicked, show its details
        historyTable.getSelectionModel().addListSelectionListener(e -> showSelected());

        JPanel tablePanel = new JPanel(new BorderLayout());
        UIUtil.titled(tablePanel, "My Appointments");
        tablePanel.add(UIUtil.scroll(historyTable));

        // Feedback from the technician + my comments
        feedbackArea = new JTextArea();
        feedbackArea.setEditable(false);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        JPanel feedbackPanel = new JPanel(new BorderLayout());
        UIUtil.titled(feedbackPanel, "Feedback and My Comments");
        feedbackPanel.add(UIUtil.scroll(feedbackArea));

        // New comment
        staffCombo = new JComboBox<>();
        commentArea = new JTextArea();
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JButton commentButton = new JButton("Submit Comment");
        commentButton.addActionListener(e -> submitComment());

        JPanel staffPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        staffPanel.add(new JLabel("Comment about:"));
        staffPanel.add(staffCombo);
        JPanel commentButtons = new JPanel(new FlowLayout());
        commentButtons.add(commentButton);

        JPanel commentPanel = new JPanel(new BorderLayout());
        UIUtil.titled(commentPanel, "Write a Comment");
        commentPanel.add(staffPanel, BorderLayout.NORTH);
        commentPanel.add(UIUtil.scroll(commentArea), BorderLayout.CENTER);
        commentPanel.add(commentButtons, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 10, 10));
        bottom.add(feedbackPanel);
        bottom.add(commentPanel);

        JPanel page = new JPanel(new GridLayout(2, 1, 10, 10));
        page.add(tablePanel);
        page.add(bottom);
        return page;
    }

    // Fills the table with this customer's appointments
    private void loadHistory() {
        historyModel.setRowCount(0);
        for (Appointment appointment : customer.viewServiceHistory()) {
            String paid = "No";
            if (DataStore.payments().findByAppointment(appointment.getAppointmentId()) != null) {
                paid = "Yes";
            }
            historyModel.addRow(new Object[]{appointment.getAppointmentId(), appointment.getServiceType().getDisplayName(),
                    DateUtil.format(appointment.getStartTime()), DateUtil.format(appointment.getEndTime()),
                    UIUtil.nameOf(appointment.getTechnicianId()), appointment.getStatus(), UIUtil.money(appointment.getPrice()),
                    paid});
        }
        UIUtil.fitColumns(historyTable);
        showSelected();
    }

    // Shows the feedback and comments of the selected appointment
    private void showSelected() {
        staffCombo.removeAllItems();
        String aptId = UIUtil.selectedValue(historyTable, 0);
        if (aptId == null) {
            feedbackArea.setText("Select an appointment from the table.");
            return;
        }
        try {
            StringBuilder text = new StringBuilder();
            Feedback feedback = customer.viewFeedback(aptId);
            text.append("Technician Feedback:\n");
            if (feedback == null) {
                text.append("No feedback yet.");
            } else {
                text.append(feedback.getText() + " (" + UIUtil.nameOf(feedback.getTechnicianId()) + ")");
            }
            text.append("\n\nMy Comments:\n");
            boolean any = false;
            for (Comment comment : DataStore.comments().findByAppointment(aptId)) {
                text.append("- ").append(comment.getText()).append(" (about ")
                        .append(UIUtil.nameOf(comment.getTargetStaffId())).append(")\n");
                any = true;
            }
            if (!any) {
                text.append("No comments yet.");
            }
            feedbackArea.setText(text.toString());

            // Only the counter staff and technician of this appointment can be commented on
            Appointment appointment = DataStore.appointments().findById(aptId);
            addStaffOption(appointment.getCounterStaffId());
            addStaffOption(appointment.getTechnicianId());
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }

    // Adds a staff member to the 'Comment about' list
    private void addStaffOption(String staffId) {
        User foundUser = DataStore.users().findById(staffId);
        if (foundUser != null) {
            staffCombo.addItem(foundUser);
        }
    }

    // Saves a new comment about the chosen staff member
    private void submitComment() {
        String aptId = UIUtil.selectedValue(historyTable, 0);
        User target = (User) staffCombo.getSelectedItem();
        if (aptId == null || target == null) {
            UIUtil.validation(this, "Please select an appointment first.");
            return;
        }
        try {
            customer.addComment(aptId, target.getUserId(), commentArea.getText());
            commentArea.setText("");
            showSelected();
            UIUtil.info(this, "Comment submitted. Thank you!");
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }

    // =====================================================
    // TAB 2: PAYMENT HISTORY
    // =====================================================

    // Tab 2: payment history
    private JPanel createPaymentTab() {
        paymentModel = UIUtil.tableModel("Payment ID", "Appointment", "Service", "Amount", "Method", "Paid At");
        paymentTable = UIUtil.table(paymentModel);
        totalLabel = new JLabel();
        JButton receiptButton = new JButton("View Receipt");
        receiptButton.addActionListener(e -> showReceipt());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(receiptButton);
        buttonPanel.add(totalLabel);

        JPanel page = new JPanel(new BorderLayout());
        UIUtil.titled(page, "My Payments");
        page.add(UIUtil.scroll(paymentTable), BorderLayout.CENTER);
        page.add(buttonPanel, BorderLayout.SOUTH);
        return page;
    }

    // Fills the payment table and shows the total amount spent
    private void loadPayments() {
        paymentModel.setRowCount(0);
        double total = 0;
        for (Payment payment : customer.viewPaymentHistory()) {
            Appointment appointment = DataStore.appointments().findById(payment.getAppointmentId());
            paymentModel.addRow(new Object[]{payment.getPaymentId(), payment.getAppointmentId(),
                    appointment.getServiceType().getDisplayName(), UIUtil.money(payment.getAmount()), payment.getMethod(),
                    DateUtil.format(payment.getPaidAt())});
            total += payment.getAmount();
        }
        totalLabel.setText("   Total Spent: " + UIUtil.money(total));
        UIUtil.fitColumns(paymentTable);
    }

    // Shows the receipt of the selected payment
    private void showReceipt() {
        String paymentId = UIUtil.selectedValue(paymentTable, 0);
        if (paymentId == null) {
            UIUtil.validation(this, "Please select a payment from the table.");
            return;
        }
        try {
            new ReceiptDialog(this, DataStore.payments().findById(paymentId).generateReceipt());
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }
}
