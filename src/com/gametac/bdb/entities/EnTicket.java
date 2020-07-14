package com.gametac.bdb.entities;

import java.util.HashSet;
import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class EnTicket {
	@PrimaryKey(sequence="ID")
	private long id;
	
	private long date;
	
	private String subject;
	
	@SecondaryKey(relate=Relationship.MANY_TO_ONE, relatedEntity=EnUser.class)
	private String email;
	
	private int status;

	@SecondaryKey(relate=Relationship.ONE_TO_MANY, relatedEntity=EnTicketMessage.class)
	private Set<Long> messages = new HashSet<Long>(0);
	
	public EnTicket() {
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public void setDate(long date) {
		this.date = date;
	}

	public long getDate() {
		return date;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getStatus() {
		return status;
	}

	public Set<Long> getMessages() {
		return messages;
	}

	public void setMessages(Set<Long> messages) {
		this.messages = messages;
	}
}