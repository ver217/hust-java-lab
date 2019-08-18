package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Income {
	private final StringProperty departmentName;
	private final StringProperty doctorId;
	private final StringProperty doctorName;
	private final StringProperty _isPro;
	private final StringProperty registerAmount;
	private final StringProperty incomeTotal;

	public Income(String departmentName, String doctorId, String doctorName, String _isPro, String registerAmount,
			String incomeTotal) {
		this.departmentName = new SimpleStringProperty(departmentName);
		this.doctorId = new SimpleStringProperty(doctorId);
		this.doctorName = new SimpleStringProperty(doctorName);
		this._isPro = new SimpleStringProperty(_isPro);
		this.registerAmount = new SimpleStringProperty(registerAmount);
		this.incomeTotal = new SimpleStringProperty(incomeTotal);
	}

	public String departmentNameProperty() {
		return departmentName.get();
	}

	public String doctorIdProperty() {
		return doctorId.get();
	}

	public String doctorNameProperty() {
		return doctorName.get();
	}

	public String _isProProperty() {
		return _isPro.get();
	}

	public String registerAmountProperty() {
		return registerAmount.get();
	}

	public String incomeTotalProperty() {
		return incomeTotal.get();
	}

}
