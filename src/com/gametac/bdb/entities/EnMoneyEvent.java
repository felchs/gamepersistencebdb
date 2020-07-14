package com.gametac.bdb.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class EnMoneyEvent {
	@PrimaryKey(sequence="ID")
	private long id;
	
	@SecondaryKey(relate=Relationship.MANY_TO_ONE, relatedEntity=EnUser.class)
	private String email;
	
	@SecondaryKey(relate=Relationship.MANY_TO_ONE, relatedEntity=EnGame.class)
	private int game;
	
	private String event;
	
	public EnMoneyEvent() {
	}
	
	public EnMoneyEvent(String email, String event) {
		this.email = email;
		this.event = event;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getEmail() {
		return email;
	}
	
	public int getGame() {
		return game;
	}
	
	public String getEvent() {
		return event;
	}
}
