package com.gametac.bdb.accessor;

import java.util.Hashtable;
import java.util.Set;

import com.gametac.bdb.RankingInMemThread;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.persist.EntityStore;

public class RankingDataAccessorMapping {
	private Hashtable<Integer, RankingDataAccessor> rankingDataAccessorMap = new Hashtable<Integer, RankingDataAccessor>();
	
	public RankingDataAccessorMapping(int[] gameIds, ReplicatedEnvironment repEnv, EntityStore store) throws EnvironmentFailureException, InterruptedException {
		for (int i = 0; i < gameIds.length; i++) {
			int gameId = gameIds[i];
			RankingInMemThread createRankingInMem = createRankingInMem();
			RankingDataAccessor rankingDataAccessor = new RankingDataAccessor(gameId, "RankingDataAccessor_Game_" + gameId, repEnv, store, createRankingInMem);
			rankingDataAccessorMap.put(gameId, rankingDataAccessor);
		}
	}
	
	private RankingInMemThread createRankingInMem() {
		return new RankingInMemThread();
	}
	
	public Hashtable<Integer, RankingDataAccessor> getRankingDataAccessorMap() {
		return rankingDataAccessorMap;
	}
	
	public RankingDataAccessor getRankingDataAccessor(int gameKey) {
		return rankingDataAccessorMap.get(gameKey);
	}

	public void shutdown() {
		Set<Integer> keySet = rankingDataAccessorMap.keySet();
		for (Integer key : keySet) {
			RankingDataAccessor rankingDataAccessor = rankingDataAccessorMap.get(key);
			rankingDataAccessor.shutdown();
		}		
	}
}
