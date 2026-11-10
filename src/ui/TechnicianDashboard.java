package ui;

import exception.ValidationException;
import model.Appointment;
import model.AppointmentStatus;
import model.Comment;
import model.Customer;
import model.Feedback;
import model.Technician;
import model.User;
import storage.DataStore;
import util.DateUtil;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class TechnicianDashboard extends BaseDashboard {

    private final Technician technician;

    private DefaultTableModel aptModel;
    private JTable aptTable;
    private JTextArea detailsArea;
    private JTextArea feedbackArea;

    public TechnicianDashboard(Technician technician) {
        super(technician, "Technician Dashboard");
        this.technician = technician;

        tabs.addTab("My Appointments", createAppointmentsTab());
        finishSetup();
    }

    @Override
    // Reloads the appointments from the text files
    protected void refreshData() {
        loadAppointments();
    }

    // Builds the table, the details box and the feedback box
    private JPanel createAppointmentsTab() {

        // =========================
        // TABLE OF ASSIGNED APPOINTMENTS
        // =========================

        aptModel = UIUtil.tableModel("ID", "Customer", "Service", "Start", "End", "Status", "Feedback");
        aptTable = UIUtil.table(aptModel);
        // When a row is clicked, show its details
        aptTable.getSelectionModel().addListSelectionListener(e -> showDetails());

        JPanel tablePanel = new JPanel(new BorderLayout());
        UIUtil.titled(tablePanel, "Appointments Assigned to Me");
        tablePanel.add(UIUtil.scroll(aptTable));

        // =========================
        // DETAILS AND COMMENTS
        // =========================

        detailsArea = new JTextArea();

        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        JButton completeButton = new JButton("Mark as Completed");
        completeButton.addActionListener(e -> markCompleted());
        JPanel completeButtons = new JPanel(new FlowLayout());
        completeButtons.add(completeButton);

        JPanel detailsPanel = new JPanel(new BorderLayout());
        UIUtil.titled(detailsPanel, "Details and Customer Comments");
        detailsPanel.add(UIUtil.scroll(detailsArea), BorderLayout.CENTER);
        detailsPanel.add(completeButtons, BorderLayout.SOUTH);

        // =========================
        // FEEDBACK
        // =========================

        feedbackArea = new JTextArea();

        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        JButton feedbackButton = new JButton("Save Feedback");
        feedbackButton.addActionListener(e -> saveFeedback());
        JPanel feedbackButtons = new JPanel(new FlowLayout());
        feedbackButtons.add(feedbackButton);

        JPanel feedbackPanel = new JPanel(new BorderLayout());
        UIUtil.titled(feedbackPanel, "My Feedback (after completing the service)");
        feedbackPanel.add(UIUtil.scroll(feedbackArea), BorderLayout.CENTER);
        feedbackPanel.add(feedbackButtons, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 10, 10));
        bottom.add(detailsPanel);
        bottom.add(feedbackPanel);

        JPanel page = new JPanel(new GridLayout(2, 1, 10, 10));
        page.add(tablePanel);
        page.add(bottom);
        return page;
    }

    // Fills the table with the appointments assigned to this technician
    private void loadAppointments() {
        aptModel.setRowCount(0);
        for (Appointment appointment : technician.viewAssignedAppointments()) {
            String feedbackText = "-";
            if (DataStore.feedback().findByAppointment(appointment.getAppointmentId()) != null) {
                feedbackText = "Given";
            }
            aptModel.addRow(new Object[]{appointment.getAppointmentId(), UIUtil.nameOf(appointment.getCustomerId()),
                    appointment.getServiceType().getDisplayName(), DateUtil.format(appointment.getStartTime()),
                    DateUtil.format(appointment.getEndTime()), appointment.getStatus(), feedbackText});
        }
        UIUtil.fitColumns(aptTable);
        showDetails();
    }

    // Shows the details and customer comments of the selected appointment
    private void showDetails() {
        String aptId = UIUtil.selectedValue(aptTable, 0);
        if (aptId == null) {
            detailsArea.setText("Select an appointment from the table.");
            feedbackArea.setText("");
            return;
        }
        try {
            Appointment appointment = technician.viewAppointmentDetails(aptId);
            User foundUser = DataStore.users().findById(appointment.getCustomerId());

            StringBuilder text = new StringBuilder();
            text.append("Appointment: ").append(appointment.getAppointmentId()).append("\n");
            text.append("Service: ").append(appointment.getServiceType()).append("\n");
            text.append("Time: ").append(DateUtil.format(appointment.getStartTime())).append(" to ")
                    .append(DateUtil.format(appointment.getEndTime())).append("\n");
            text.append("Status: ").append(appointment.getStatus()).append("\n");
            text.append("Task: ").append(appointment.getTaskDescription()).append("\n");
            text.append("Assigned by: ").append(UIUtil.idAndName(appointment.getCounterStaffId())).append("\n");
            if (foundUser instanceof Customer) {
                Customer customer = (Customer) foundUser;
                text.append("Customer: ").append(customer.getName()).append(" (").append(customer.getPhone()).append(")\n");
                text.append("Car: ").append(customer.getCarModel()).append(", ").append(customer.getCarPlate()).append("\n");
            }
            text.append("\nCustomer Comments:\n");
            boolean any = false;
            for (Comment comment : technician.viewComments(aptId)) {
                text.append("- ").append(comment.getText()).append(" (about ")
                        .append(UIUtil.nameOf(comment.getTargetStaffId())).append(")\n");
                any = true;
            }
            if (!any) {
                text.append("No comments yet.\n");
            }
            detailsArea.setText(text.toString());
            detailsArea.setCaretPosition(0);

            Feedback savedFeedback = DataStore.feedback().findByAppointment(aptId);
            if (savedFeedback == null) {
                feedbackArea.setText("");
            } else {
                feedbackArea.setText(savedFeedback.getText());
            }
            feedbackArea.setEditable(appointment.getStatus() == AppointmentStatus.COMPLETED);
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }

    // Marks the selected appointment as Completed
    private void markCompleted() {
        String aptId = UIUtil.selectedValue(aptTable, 0);
        if (aptId == null) {
            UIUtil.validation(this, "Please select an appointment.");
            return;
        }
        if (!UIUtil.confirm(this, "Mark appointment " + aptId + " as Completed?")) {
            return;
        }
        try {
            technician.markCompleted(aptId);
            loadAppointments();
            UIUtil.info(this, "Appointment " + aptId + " is now Completed. You can add feedback.");
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }

    // Saves the feedback for the selected appointment
    private void saveFeedback() {
        String aptId = UIUtil.selectedValue(aptTable, 0);
        if (aptId == null) {
            UIUtil.validation(this, "Please select an appointment.");
            return;
        }
        try {
            technician.provideFeedback(aptId, feedbackArea.getText());
            loadAppointments();
            UIUtil.info(this, "Feedback saved for appointment " + aptId + ".");
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }
}
