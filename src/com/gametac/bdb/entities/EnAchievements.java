package com.gametac.bdb.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class EnAchievements {
	@PrimaryKey
	private int achievementId;
	
	@SecondaryKey(relate=Relationship.ONE_TO_ONE, relatedEntity=EnGame.class)
	private int gameId;

	@SecondaryKey(relate=Relationship.ONE_TO_ONE, relatedEntity=EnUser.class)
	private String email;
	
	private long achievemntDate;
	
	private int quantityAchieved;
	
	private int quantityRequired;
	
	public EnAchievements() {
	}

	public int getAchievementId() {
		return achievementId;
	}

	public void setAchievementId(int achievementId) {
		this.achievementId = achievementId;
	}

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getAchievemntDate() {
		return achievemntDate;
	}

	public void setAchievemntDate(long achievemntDate) {
		this.achievemntDate = achievemntDate;
	}

	public int getQuantityAchieved() {
		return quantityAchieved;
	}

	public void setQuantityAchieved(int quantityAchieved) {
		this.quantityAchieved = quantityAchieved;
	}

	public int getQuantityRequired() {
		return quantityRequired;
	}

	public void setQuantityRequired(int quantityRequired) {
		this.quantityRequired = quantityRequired;
	}
}
