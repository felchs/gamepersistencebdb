package com.gametac.bdb.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class EnMoney {
	@PrimaryKey(sequence="ID")
	private long id;
	
	@SecondaryKey(relate=Relationship.MANY_TO_ONE, relatedEntity=EnUser.class)
	private String email;

	private float amount;
	
	private long timeStamp;
	
	private boolean virtual;
	
	public EnMoney() {
	}
	
	public EnMoney(String email, float amount, long timeStamp, boolean virtual) {
		this.email = email;
		this.amount = amount;
		this.timeStamp = timeStamp;
		this.virtual = virtual;
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
	
	public float getAmount() {
		return amount;
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}
	
	public boolean isVirtual() {
		return virtual;
	}
}
