package com.gametac.bdb.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class EnAward {
	@PrimaryKey(sequence="ID")
	private int id;
	
	@SecondaryKey(relate=Relationship.MANY_TO_ONE, relatedEntity=EnUser.class)
	private String email;
	
	@SecondaryKey(relate=Relationship.MANY_TO_ONE, relatedEntity=EnGame.class)
	private int gameId;
	
	@SecondaryKey(relate=Relationship.MANY_TO_ONE, relatedEntity=EnAwardType.class)
	private int awardType;
	
	private long date;
		
	public EnAward() {
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public int getGameId() {
		return gameId;
	}
	
	public void setGameId(int gameId) {
		this.gameId = gameId;
	}
	
	public int getAwardType() {
		return awardType;
	}
	
	public void setAwardType(int awardType) {
		this.awardType = awardType;
	}

	public long getDate() {
		return date;
	}
	
	public void setDate(long date) {
		this.date = date;
	}
}
