package com.gametac.bdb.accessor;

import java.util.ArrayList;
import java.util.Iterator;

import com.gametac.bdb.Command;
import com.gametac.bdb.RankingInMemThread;
import com.gametac.bdb.RankingInMemThread.RankingReturn;
import com.gametac.bdb.RankingStruct;
import com.gametac.bdb.RunTransaction;
import com.gametac.bdb.entities.EnPoints;
import com.gametac.bdb.entities.EnPointsByUser;
import com.gametac.bdb.entities.RankingInfoDBDTO;
import com.gametac.bdb.entities.RankingPeriod;
import com.gametac.utils.DateUtils;
import com.gametac.utils.ReturnInterface;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class RankingDataAccessor extends DataAccessor {
	private int gameId;
	
	private Database rankingDb;
	
	private PrimaryIndex<Long, EnPoints> pkPoints;

	private PrimaryIndex<String, EnPointsByUser> pkPointsByUser;
	
	private RankingInMemThread rankingInMemThread;

	public RankingDataAccessor(int gameId, String dataAccessorKey, ReplicatedEnvironment repEnv, EntityStore store, RankingInMemThread rankingInMemThread) throws EnvironmentFailureException, InterruptedException {
		super(dataAccessorKey, false, repEnv, store);
		this.gameId = gameId;
		this.rankingInMemThread = rankingInMemThread;
		
		createPointsMapInMem();
	}
	
	private void createPointsMapInMem() {
		Iterator<EnPoints> it = pkPoints.entities().iterator();
		while (it.hasNext()) {
			EnPoints enPoints = it.next();
			String email = enPoints.getEmail();
			float points = enPoints.getPoints();
			long pointsTime = enPoints.getTime();
			
			rankingInMemThread.updateRanking(email, points, pointsTime);
		}
	}

	@Override
	protected void initalize(boolean readOnly) throws EnvironmentFailureException, InterruptedException {
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
		        DatabaseConfig dbConfig = new DatabaseConfig();
		        dbConfig.setTransactional(true);
		        dbConfig.setAllowCreate(true);
		        dbConfig.setSortedDuplicates(true);
		        rankingDb = repEnv.openDatabase(txn, "RankingDB_" + dataAccessorKey, dbConfig);
		        
		        pkPoints = store.getPrimaryIndex(Long.class, EnPoints.class);
		        pkPointsByUser = store.getPrimaryIndex(String.class, EnPointsByUser.class);
			}
		};
		runTransaction.run(readOnly);
	}
	
	public void updateRanking(final String email, final int gameId, final float points) {
		updateRanking(email, gameId, points, DateUtils.getCalendar().getTimeInMillis());
	}

	public void updateRanking(final String email, final int gameId, final float points, final long time) {
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {				
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnPoints enPoints = new EnPoints(email, time, points);
				pkPoints.put(txn, enPoints);
				EnPointsByUser enPointsByUser = pkPointsByUser.get(email);
				if (enPointsByUser == null) {
					enPointsByUser = new EnPointsByUser(email);
					pkPointsByUser.put(txn, enPointsByUser);
				}
				enPointsByUser.addEnPoint(enPoints.getId());
				rankingInMemThread.updateRanking(email, points, time);
			}
		};
		
		try {
			transaction.run(false);
		} catch (EnvironmentFailureException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<RankingInfoDBDTO> getRankingListing(final RankingPeriod period, int startIdx, int numPositions) throws EnvironmentFailureException, InterruptedException {
		final ArrayList<RankingInfoDBDTO> rankingInfoListToRet = new ArrayList<RankingInfoDBDTO>();
		rankingInMemThread.getRanking(period, startIdx, numPositions, new RankingReturn() {
			@Override
			public void addRanking(RankingStruct ranking) {
				RankingInfoDBDTO rankingInfoDBDTO = new RankingInfoDBDTO();
				rankingInfoDBDTO.email = ranking.getEmail();
				rankingInfoDBDTO.gameId = gameId;
				rankingInfoDBDTO.points = ranking.getPoints();
				rankingInfoListToRet.add(rankingInfoDBDTO);
			}
		});
		
		return rankingInfoListToRet;
	}

	public void retrieveRankingListAsBytes(RankingPeriod period, int startIdx, int numPositions, final ReturnInterface<byte[]> objRet) {
		rankingInMemThread.retrieveRankingListAsBytes(period, startIdx, numPositions, objRet);
	}
	
	public PrimaryIndex<Long, EnPoints> getPkPoints() {
		return pkPoints;
	}
	
	public PrimaryIndex<String, EnPointsByUser> getSkPointsByUser() {
		return pkPointsByUser;
	}

	@Override
	public void shutdown() {
		rankingDb.close();
	}
}
