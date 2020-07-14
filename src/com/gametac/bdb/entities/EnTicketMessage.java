package com.gametac.bdb.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class EnTicketMessage {
	@PrimaryKey(sequence="ID")
	private long id;
	
	private int type;
	
	private long date;
	
	private String message;
	
	public EnTicketMessage() {
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	
	public void setDate(long date) {
		this.date = date;
	}

	public long getDate() {
		return date;
	}

	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}