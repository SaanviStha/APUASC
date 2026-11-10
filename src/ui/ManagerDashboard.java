package ui;

import exception.ValidationException;
import model.Appointment;
import model.Comment;
import model.Feedback;
import model.Manager;
import model.Role;
import model.ServiceType;
import model.User;
import storage.DataStore;
import util.ActivityLog;
import util.DateUtil;
import util.ValidationUtil;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ManagerDashboard extends BaseDashboard {

    private final Manager manager;

    private UserManagementPanel staffPanel;
    private JTextField normalPriceField;
    private JTextField majorPriceField;
    private DefaultTableModel feedbackModel;
    private DefaultTableModel commentModel;
    private JTextArea reportArea;
    private JTable feedbackTable;
    private JTable commentTable;

    public ManagerDashboard(Manager manager) {
        super(manager, "Manager Dashboard");
        this.manager = manager;

        tabs.addTab("Staff Accounts", createStaffTab());
        tabs.addTab("Service Prices", createPriceTab());
        tabs.addTab("Feedback & Comments", createFeedbackTab());
        tabs.addTab("Reports", createReportTab());
        finishSetup();
    }

    @Override
    // Reloads every tab from the text files
    protected void refreshData() {
        staffPanel.refresh();
        loadPrices();
        loadFeedbackAndComments();
        loadReport();
    }

    // =====================================================
    // TAB 1: STAFF ACCOUNTS
    // =====================================================

    // Tab 1: create, view, update and delete staff accounts
    private JPanel createStaffTab() {
        staffPanel = new UserManagementPanel("Managers, Counter Staff and Technicians", new UserCrudHandler() {
            @Override
            public List<? extends User> list() {
                return manager.viewStaff();
            }

            @Override
            public void add(User user) throws ValidationException {
                manager.addStaff(user);
            }

            @Override
            public void update(User user) throws ValidationException {
                manager.updateStaff(user);
            }

            @Override
            public void delete(String userId) throws ValidationException {
                manager.deleteStaff(userId);
            }
        }, Role.MANAGER, Role.COUNTER_STAFF, Role.TECHNICIAN);
        return staffPanel;
    }

    // =====================================================
    // TAB 2: SERVICE PRICES
    // =====================================================

    // Tab 2: set the prices of the two service types
    private JPanel createPriceTab() {
        normalPriceField = new JTextField(10);
        majorPriceField = new JTextField(10);

        JPanel form = UIUtil.form("Set Service Prices");
        form.add(new JLabel("Normal Service - 1 hour (RM):"));
        form.add(normalPriceField);
        form.add(new JLabel("Major Service - 3 hours (RM):"));
        form.add(majorPriceField);

        JButton saveButton = new JButton("Save Prices");
        saveButton.addActionListener(e -> savePrices());
        form.add(new JLabel(""));
        form.add(saveButton);

        JPanel page = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        page.add(form);
        return page;
    }

    // Shows the current prices in the text boxes
    private void loadPrices() {
        normalPriceField.setText(String.format("%.2f", DataStore.prices().getPrice(ServiceType.NORMAL).getAmount()));
        majorPriceField.setText(String.format("%.2f", DataStore.prices().getPrice(ServiceType.MAJOR).getAmount()));
    }

    // Checks and saves the new prices
    private void savePrices() {
        try {
            double normal = ValidationUtil.parseAmount("Normal service price", normalPriceField.getText());
            double major = ValidationUtil.parseAmount("Major service price", majorPriceField.getText());
            if (major <= normal) {
                throw new ValidationException("Major service price should be higher than normal service price.");
            }
            manager.setPrice(ServiceType.NORMAL, normal);
            manager.setPrice(ServiceType.MAJOR, major);
            loadPrices();
            UIUtil.info(this, "Prices updated. New appointments will use these prices.");
        } catch (ValidationException ex) {
            UIUtil.validation(this, ex.getMessage());
        }
    }

    // =====================================================
    // TAB 3: FEEDBACK & COMMENTS
    // =====================================================

    // Tab 3: all technician feedback and customer comments
    private JPanel createFeedbackTab() {
        feedbackModel = UIUtil.tableModel("ID", "Appointment", "Technician", "Customer", "Feedback", "Date");
        commentModel = UIUtil.tableModel("ID", "Appointment", "Customer", "About Staff", "Comment", "Date");
        feedbackTable = UIUtil.table(feedbackModel);
        commentTable = UIUtil.table(commentModel);

        JPanel feedbackPanel = new JPanel(new BorderLayout());
        UIUtil.titled(feedbackPanel, "Technician Feedback");
        feedbackPanel.add(UIUtil.scroll(feedbackTable));

        JPanel commentPanel = new JPanel(new BorderLayout());
        UIUtil.titled(commentPanel, "Customer Comments");
        commentPanel.add(UIUtil.scroll(commentTable));

        JPanel page = new JPanel(new GridLayout(2, 1, 10, 10));
        page.add(feedbackPanel);
        page.add(commentPanel);
        return page;
    }

    // Fills the feedback and comment tables
    private void loadFeedbackAndComments() {
        feedbackModel.setRowCount(0);
        for (Feedback feedback : manager.viewAllFeedback()) {
            Appointment appointment = DataStore.appointments().findById(feedback.getAppointmentId());
            String customer = "-";
            if (appointment != null) {
                customer = UIUtil.idAndName(appointment.getCustomerId());
            }
            feedbackModel.addRow(new Object[]{feedback.getFeedbackId(), feedback.getAppointmentId(),
                    UIUtil.idAndName(feedback.getTechnicianId()), customer, feedback.getText(), DateUtil.format(feedback.getCreatedAt())});
        }
        commentModel.setRowCount(0);
        for (Comment comment : manager.viewAllComments()) {
            commentModel.addRow(new Object[]{comment.getCommentId(), comment.getAppointmentId(),
                    UIUtil.idAndName(comment.getCustomerId()), UIUtil.idAndName(comment.getTargetStaffId()),
                    comment.getText(), DateUtil.format(comment.getCreatedAt())});
        }
        UIUtil.fitColumns(feedbackTable);
        UIUtil.fitColumns(commentTable);
    }

    // =====================================================
    // TAB 4: REPORTS
    // =====================================================

    // Tab 4: the analysed report
    private JPanel createReportTab() {
        reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        reportArea.setEditable(false);

        JButton refreshButton = new JButton("Refresh");
        JButton exportButton = new JButton("Export to .txt");
        refreshButton.addActionListener(e -> loadReport());
        exportButton.addActionListener(e -> exportReport());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);

        JPanel page = new JPanel(new BorderLayout(5, 5));
        UIUtil.titled(page, "Analysed Report");
        page.add(UIUtil.scroll(reportArea), BorderLayout.CENTER);
        page.add(buttonPanel, BorderLayout.SOUTH);
        return page;
    }

    // Generates the report and shows it
    private void loadReport() {
        reportArea.setText(manager.generateReport().toText());
        reportArea.setCaretPosition(0);
    }

    // Saves the report into reports/report_<date>.txt
    private void exportReport() {
        File folder = new File("reports");
        folder.mkdirs();
        File file = new File(folder, "report_" + DateUtil.formatDate(LocalDate.now()) + ".txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(manager.generateReport().toText());
            ActivityLog.write(manager.getUserId() + " exported report " + file.getName());
            UIUtil.info(this, "Report saved to " + file.getAbsolutePath());
        } catch (IOException ex) {
            UIUtil.error(this, "Could not save the report: " + ex.getMessage());
        }
    }
}
