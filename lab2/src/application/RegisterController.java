package application;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class RegisterController {

	@FXML
	private TextField departmentNameField;
	@FXML
	public ChoiceBox<String> registerTypeField;
	@FXML
	private TextField doctorNameField;
	@FXML
	private TextField registerNameField;
	@FXML
	private TextField paymentField;
	@FXML
	private TextField duePaymentField;
	@FXML
	private TextField changeField;
	@FXML
	private TextField registerIdField;
	@FXML
	private Button confirmButton;
	@FXML
	private Button clearButton;
	@FXML
	private Button exitButton;

	public static String patientId;

	private String departmentId;
	private String doctorId;
	private boolean isPro = false;
	private String registerTypeId;
	private BigDecimal duePayment;

	private SuggestionProvider<String> departmentNameProvider;
	private SuggestionProvider<String> doctorNameProvider;
	private SuggestionProvider<String> registerNameProvider;

	@FXML
	public void initialize() {
		departmentNameFieldInit();
		registerTypeFieldInit();
		addPaymentFieldChangeListener();
	}

	private void departmentNameFieldInit() {
		DbBridge db = DbBridge.getInstance();
		try {
			LinkedList<String> departments = db.getAllDepartments();
			if (departmentNameProvider == null) {
				departmentNameProvider = SuggestionProvider.create(departments);
				departmentNameProvider.setShowAllIfEmpty(true);
				AutoCompletionBinding<String> binding = TextFields.bindAutoCompletion(departmentNameField,
						departmentNameProvider);
				binding.setOnAutoCompleted((AutoCompletionBinding.AutoCompletionEvent<String> event) -> {
					String[] splited = event.getCompletion().split(" ");
					departmentId = splited[0];
					departmentNameField.setText(splited[1]);
					doctorNameField.setDisable(false);
					doctorNameInit();
				});
			} else {
				departmentNameProvider.clearSuggestions();
				departmentNameProvider.addPossibleSuggestions(departments);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "数据库错误！");
		}
	}

	private void doctorNameInit() {
		DbBridge db = DbBridge.getInstance();
		try {
			LinkedList<String> doctors = db.getDoctorsByDepartment(departmentId);
			if (doctorNameProvider == null) {
				doctorNameProvider = SuggestionProvider.create(doctors);
				doctorNameProvider.setShowAllIfEmpty(true);
				AutoCompletionBinding<String> binding = TextFields.bindAutoCompletion(doctorNameField,
						doctorNameProvider);
				binding.setOnAutoCompleted((AutoCompletionBinding.AutoCompletionEvent<String> event) -> {
					String[] splited = event.getCompletion().split(" ");
					doctorId = splited[0];
					doctorNameField.setText(splited[1]);
					if (splited[3].equals("false")) {
						registerTypeField.setValue("普通号");
						registerTypeField.setDisable(true);
						isPro = false;
					} else {
						registerTypeField.setDisable(false);
					}
					registerNameField.setDisable(false);
					registerNameFieldInit();
				});
			} else {
				doctorNameProvider.clearSuggestions();
				doctorNameProvider.addPossibleSuggestions(doctors);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "数据库错误！");
		}
	}

	private void registerTypeFieldInit() {
		registerTypeField.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					if (newValue.equals("普通号")) {
						isPro = false;
					} else {
						isPro = true;
					}
					registerNameFieldInit();
				});
	}

	private void registerNameFieldInit() {
		DbBridge db = DbBridge.getInstance();
		try {
			LinkedList<String> registerTypes = db.getRegisterTypesByDepartment(departmentId, isPro);
			if (registerNameProvider == null) {
				registerNameProvider = SuggestionProvider.create(registerTypes);
				registerNameProvider.setShowAllIfEmpty(true);
				AutoCompletionBinding<String> binding = TextFields.bindAutoCompletion(registerNameField,
						registerNameProvider);
				binding.setOnAutoCompleted((AutoCompletionBinding.AutoCompletionEvent<String> event) -> {
					String[] splited = event.getCompletion().split(" ");
					try {
						boolean canReg = db.canRegister(splited[0]);
						if (!canReg) {
							JOptionPane.showMessageDialog(null, "该号已挂满！");
						} else {
							registerTypeId = splited[0];
							registerNameField.setText(splited[1]);
							duePayment = new BigDecimal(splited[3]);
							duePaymentField.setText(splited[3]);
							duePaymentField.setDisable(false);
							if (!db.canPay(patientId, registerTypeId)) {
								paymentField.setDisable(false);
								changeField.setDisable(false);
							}
						}
					} catch (SQLException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "数据库错误！");
					}
				});
			} else {
				registerNameProvider.clearSuggestions();
				registerNameProvider.addPossibleSuggestions(registerTypes);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "数据库错误！");
		}
	}

	public void addPaymentFieldChangeListener() {
		paymentField.textProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					if (!newValue.equals("")) {
						try {
							BigDecimal payment = new BigDecimal(newValue);
							DbBridge db = DbBridge.getInstance();
							BigDecimal amount = db.getPatientAmount(patientId);
							BigDecimal change = payment.add(amount).subtract(duePayment);
							if (change.compareTo(new BigDecimal(0)) >= 0) {
								changeField.setText(change.toString());
							} else {
								changeField.clear();
							}
						} catch (NumberFormatException e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(null, "输入格式错误！");
							paymentField.clear();
							changeField.clear();
						} catch (SQLException e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(null, "数据库错误！");
						}
					} else {
						changeField.clear();
					}
				});
	}

	@FXML
	public void onClearButtonClick() {
		departmentNameField.clear();
		doctorNameField.clear();
		doctorNameField.setDisable(true);
		registerTypeField.setValue("普通号");
		registerTypeField.setDisable(true);
		registerNameField.clear();
		registerNameField.setDisable(true);
		paymentField.clear();
		paymentField.setDisable(true);
		duePaymentField.clear();
		duePaymentField.setDisable(true);
		changeField.clear();
		changeField.setDisable(true);
		registerIdField.clear();
		registerIdField.setDisable(true);
		departmentId = null;
		doctorId = null;
		isPro = false;
		registerTypeId = null;
		duePayment = null;
		departmentNameFieldInit();
	}

	@FXML
	public void onConfirmButtonClick() {
		if (departmentNameField.getText().equals("") || doctorNameField.getText().equals("")
				|| registerNameField.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "输入格式错误！");
			return;
		}
		BigDecimal payment;
		if (paymentField.getText().equals("")) {
			payment = new BigDecimal(0);
		} else {
			payment = new BigDecimal(paymentField.getText());
		}
		try {
			DbBridge db = DbBridge.getInstance();
			String registerId = db.register(patientId, doctorId, registerTypeId, payment);
			registerIdField.setText(registerId);
			registerIdField.setDisable(false);
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "数据库错误,挂号失败！");
		} catch (RuntimeException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "挂号人数已满,挂号失败！");
		}
	}

	@FXML
	public void onExitButtonClick() {
		Platform.exit();
	}
}
