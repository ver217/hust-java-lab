package application;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

enum LoginType {
	PATIENT, DOCTOR
}

public class LoginController implements Initializable {

	@FXML
	private Button loginButton;
	@FXML
	private TextField usernameField;
	@FXML
	private TextField passwordField;
	@FXML
	public ChoiceBox<String> loginType;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	public void onLoginClick() {
		if (usernameField.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "用户名不能为空，请输入用户名");
			return;
		} else if (passwordField.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "密码不能为空，请输入密码");
			return;
		}
		LoginType identity = LoginType.PATIENT;
		if (loginType.getValue().equals("医生")) {
			identity = LoginType.DOCTOR;
		}
		try {
			DbBridge db = DbBridge.getInstance();
			if (db.checkPassword(usernameField.getText().trim(), passwordField.getText().trim(), identity)) {
				String fxmlFilePath;
				String stageTitle;
				if (identity == LoginType.PATIENT) {
					RegisterController.patientId = usernameField.getText().trim();
					fxmlFilePath = "Register.fxml";
					stageTitle = "挂号系统";
				} else {
					DoctorViewController.doctorId = usernameField.getText().trim();
					fxmlFilePath = "DoctorView.fxml";
					stageTitle = "医生系统";
				}
				Stage tempStage = (Stage) loginButton.getScene().getWindow();
				tempStage.close();
				FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilePath));
				AnchorPane root = new AnchorPane();
				Scene myScene = new Scene(root);
				try {
					myScene.setRoot((Parent) loader.load());
					Stage newStage = new Stage();
					newStage.setTitle(stageTitle);
					newStage.setScene(myScene);
					newStage.show();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(null, "账号不存在或密码错误！");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "数据库错误！");
		}

	}
}
