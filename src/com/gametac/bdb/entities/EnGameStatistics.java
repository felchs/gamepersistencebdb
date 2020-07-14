package com.gametac.bdb.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class EnGameStatistics {
	@PrimaryKey(sequence="ID")
	private Long id;
	
	@SecondaryKey(relate=Relationship.MANY_TO_ONE, relatedEntity=EnUser.class)
	private String email;
	
	@SecondaryKey(relate=Relationship.MANY_TO_ONE, relatedEntity=EnGame.class)
	private int gameId;
	
	private long wins;
	
	private long lose;
	
	private long draw;
	
	private long abandon;
	
	public EnGameStatistics() {
	}

	public Long getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(Long id) {
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

	public long getWins() {
		return wins;
	}

	public void addWins(long winsToAdd) {
		this.wins += winsToAdd;
	}
	
	public void setWins(long wins) {
		this.wins = wins;
	}

	public long getLoses() {
		return lose;
	}

	public void setLose(long lose) {
		this.lose = lose;
	}

	public void addLose(long losesToAdd) {
		this.lose += losesToAdd;
	}
	
	public long getDraws() {
		return draw;
	}

	public void setDraw(long draw) {
		this.draw = draw;
	}
	
	public void addDraw(long drawsToAdd) {
		this.draw += drawsToAdd;
	}

	public long getAbandon() {
		return abandon;
	}
	
	public void addAbandon(long abandonsToAdd) {
		this.abandon += abandonsToAdd;
	}

	public void setAbandon(long abandon) {
		this.abandon = abandon;
	}
}