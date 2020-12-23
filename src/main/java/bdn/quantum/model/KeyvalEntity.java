package bdn.quantum.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import bdn.quantum.QuantumConstants;

@Entity
@Table(name=QuantumConstants.TABLE_KEYVAL)
public class KeyvalEntity {

	@Id
	@Column(name = QuantumConstants.KEYVAL_KEY)
	private String key;

	@Column(name = QuantumConstants.KEYVAL_VALUE)
	private String value;

	public KeyvalEntity() {
	}
	
	public KeyvalEntity(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
