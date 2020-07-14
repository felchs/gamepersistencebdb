package com.gametac.bdb;

import java.io.Serializable;
import java.util.HashMap;

public class GameTypes implements Serializable {
	private static final long serialVersionUID = 1L;

	private HashMap<String, Integer> gameNameById = new HashMap<String, Integer>();
	
	public GameTypes() {
	}
	
	public GameTypes(int[] gameIds, String[] gameNames) {
		assert gameIds.length == gameNames.length : "Num gameIds: " + gameIds.length + ", numGameNames: " + gameNames.length;
		for (int i = 0; i < gameIds.length; i++) {
			String key = gameNames[i];
			Integer value = gameIds[i];
			gameNameById.put(key, value);
		}
	}
	
	public HashMap<String, Integer> getGameNameById() {
		return gameNameById;
	}
	
	public int getGameId(String gameName) {
		return gameNameById.get(gameName);
	}
}