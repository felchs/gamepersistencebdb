package com.gametac.bdb.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class EnPassRedefinition {
	
	@PrimaryKey
	private String email;
	
	private Long date;
	
	private String passKey;
	
	public EnPassRedefinition() {
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public Long getDate() {
		return date;
	}
	
	public void setDate(Long date) {
		this.date = date;
	}

	public String getPassKey() {
		return passKey;
	}
	
	public void setPassKey(String passKey) {
		this.passKey = passKey;
	}
}