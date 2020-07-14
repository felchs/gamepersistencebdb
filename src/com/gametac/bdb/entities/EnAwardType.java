package com.gametac.bdb.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class EnAwardType {
	public final transient static int TROPHY = 11;
	public final transient static int MEDALS = 22;
	
	public static int[] getAwards() {
		return new int[] { TROPHY, MEDALS }; 
	}
	
	@PrimaryKey
	int id;
	
	public EnAwardType() {
	}
	
	public EnAwardType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("id: " + id);
		return strBuilder.toString();
	}
}
