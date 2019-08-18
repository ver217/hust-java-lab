package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Patient {
	private final StringProperty registerId;
	private final StringProperty patientName;
	private final StringProperty registerDatetime;
	private final StringProperty isPro;

	public Patient(String registerId, String patientName, String registerDatetime, String isPro) {
		this.registerId = new SimpleStringProperty(registerId);
		this.patientName = new SimpleStringProperty(patientName);
		this.registerDatetime = new SimpleStringProperty(registerDatetime);
		this.isPro = new SimpleStringProperty(isPro);
	}

	public String registerIdProperty() {
		return registerId.get();
	}

	public String patientNameProperty() {
		return patientName.get();
	}

	public String registerDatetimeProperty() {
		return registerDatetime.get();
	}

	public String isProProperty() {
		return isPro.get();
	}
}
