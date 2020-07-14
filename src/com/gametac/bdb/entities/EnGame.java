package com.gametac.bdb.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class EnGame {
	@PrimaryKey
	private int id;
	
	public EnGame() {
	}
	
	public EnGame(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int gameId) {
		this.id = gameId;
	}
}
