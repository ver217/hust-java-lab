package application;

import java.sql.SQLException;
import javax.swing.JOptionPane;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DoctorViewController {

    @FXML
    Label welcomeLabel;
    @FXML
    Label timeLabel;
    private ObservableList<Patient> patientData;
    @FXML
    private TableView<Patient> patientInfoTable;
    @FXML
    private TableColumn<Patient, String> registerIdColumn;
    @FXML
    private TableColumn<Patient, String> patientNameColumn;
    @FXML
    private TableColumn<Patient, String> registerDatetimeColumn;
    @FXML
    private TableColumn<Patient, String> isProColumn;

    private ObservableList<Income> incomeData;
    @FXML
    private TableView<Income> incomeInfoTable;
    @FXML
    private TableColumn<Income, String> departmentNameColumn;
    @FXML
    private TableColumn<Income, String> doctorIdColumn;
    @FXML
    private TableColumn<Income, String> doctorNameColumn;
    @FXML
    private TableColumn<Income, String> _isProColumn;
    @FXML
    private TableColumn<Income, String> registerAmountColumn;
    @FXML
    private TableColumn<Income, String> incomeTotalColumn;

    public static String doctorId;

    @FXML
    public void initialize() {
        welcomeLabelInit();
        timeLabelInit();
        patientInfoInit();
        incomeInfoInit();
    }

    void welcomeLabelInit() {
        try {
            DbBridge db = DbBridge.getInstance();
            String doctorName = db.getDoctorName(doctorId);
            welcomeLabel.setText("欢迎，" + doctorName);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据库错误！");
        }
    }

    void timeLabelInit() {
        try {
            DbBridge db = DbBridge.getInstance();
            String datatime = db.getCurrentDatetime();
            String endDatetime = datatime.substring(0, datatime.length() - 2);
            String startDatetime = String.join(" ", endDatetime.split(" ")[0], "00:00:00");
            timeLabel.setText(String.format("起始时间：%s    截止时间：%s", startDatetime, endDatetime));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据库错误！");
        }
    }

    void patientInfoInit() {
        registerIdColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().registerIdProperty()));
        patientNameColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().patientNameProperty()));
        registerDatetimeColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().registerDatetimeProperty()));
        isProColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isProProperty()));
        try {
            DbBridge db = DbBridge.getInstance();
            patientData = db.getPatients(doctorId);
            patientInfoTable.setItems(patientData);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据库错误！");
        }
    }

    public void incomeInfoInit() {
        departmentNameColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().departmentNameProperty()));
        doctorIdColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().doctorIdProperty()));
        doctorNameColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().doctorNameProperty()));
        _isProColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()._isProProperty()));
        registerAmountColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().registerAmountProperty()));
        incomeTotalColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().incomeTotalProperty()));
        try {
            DbBridge db = DbBridge.getInstance();
            incomeData = db.getIncomes();
            incomeInfoTable.setItems(incomeData);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据库错误！");
        }
    }
}
