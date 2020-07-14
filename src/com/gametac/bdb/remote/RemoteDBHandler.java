package com.gametac.bdb.remote;

import java.io.IOException;
import java.util.Iterator;

import com.gametac.bdb.DBHandler;
import com.gametac.bdb.accessor.RankingDataAccessor;

public class RemoteDBHandler {

	private static RemoteDBHandler instance;
	
	public static RemoteDBHandler getInstance() {
		if (instance == null) {
			try {
				instance = new RemoteDBHandler();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}
	
	public static void initilize() {
		getInstance();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private RemoteAccessor remoteAccessor;
	
	public RemoteDBHandler() throws IOException {
		this.remoteAccessor = new RemoteAccessor();
		this.remoteAccessor.addDataAcessor(DBHandler.getAwardsDataAccessor());
		this.remoteAccessor.addDataAcessor(DBHandler.getAchievementsDataAccessor());
		this.remoteAccessor.addDataAcessor(DBHandler.getUserDataAccessor());
		Iterator<RankingDataAccessor> it = DBHandler.getRankingDataAccessorMapping().getRankingDataAccessorMap().values().iterator();
		while (it.hasNext()) {
			RankingDataAccessor dataAccessor = it.next();
			this.remoteAccessor.addDataAcessor(dataAccessor);
		}
		System.out.println("Remote DB Handler Initialized.");
	}
}
