package com.gametac.bdb.accessor;

import java.util.Iterator;

import com.gametac.bdb.Command;
import com.gametac.bdb.RunTransaction;
import com.gametac.bdb.entities.EnGameStatistics;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.ForwardCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class GameStatisticsDataAccessor extends DataAccessor {
	private PrimaryIndex<Long, EnGameStatistics> pkGameStatisticsById;
	
	private SecondaryIndex<Integer, Long, EnGameStatistics> skGameStatisticsByGame;
	
	private SecondaryIndex<String, Long, EnGameStatistics> skGameStatisticsByEmail;

	public GameStatisticsDataAccessor(String dataAccessorKey, ReplicatedEnvironment repEnv, EntityStore store) throws EnvironmentFailureException, InterruptedException {
		super(dataAccessorKey, false, repEnv, store);
	}

	@Override
	protected void initalize(boolean readOnly) throws EnvironmentFailureException, InterruptedException {
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				pkGameStatisticsById = store.getPrimaryIndex(Long.class, EnGameStatistics.class);
				skGameStatisticsByGame = store.getSecondaryIndex(pkGameStatisticsById, Integer.class, "gameId");
				skGameStatisticsByEmail = store.getSecondaryIndex(pkGameStatisticsById, String.class, "email");
			}
		};
		runTransaction.run(readOnly);
	}

	public void updateGameStatistics(final String email, final int gameId, final int wins, final int loses, final int draws, final int abandon) {
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnGameStatistics enGameStatistics = null;
				EntityJoin<Long, EnGameStatistics> join = new EntityJoin<Long, EnGameStatistics>(pkGameStatisticsById);
				join.addCondition(skGameStatisticsByGame, gameId);
				join.addCondition(skGameStatisticsByEmail, email);
				ForwardCursor<EnGameStatistics> entitiesCursor = join.entities(txn, CursorConfig.DEFAULT);
				Iterator<EnGameStatistics> it = entitiesCursor.iterator();
				boolean newEntiy = false;
				if (it.hasNext()) {
					enGameStatistics = it.next();
				} else {
					newEntiy = true;
					enGameStatistics = new EnGameStatistics();
					enGameStatistics.setEmail(email);
					enGameStatistics.setGameId(gameId);
				}
				entitiesCursor.close();
				
				enGameStatistics.addWins(wins);
				enGameStatistics.addLose(loses);
				enGameStatistics.addDraw(draws);
				enGameStatistics.addAbandon(abandon);
				
				// update
				if (!newEntiy) {
					pkGameStatisticsById.delete(txn, enGameStatistics.getId());
				}
				pkGameStatisticsById.put(txn, enGameStatistics);
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

	public EnGameStatistics getGamestatistics(final int gameId, final String email) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<EnGameStatistics> ret = new TransactionReturn<EnGameStatistics>();

		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {				
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EntityJoin<Long, EnGameStatistics> join = new EntityJoin<Long, EnGameStatistics>(pkGameStatisticsById);
				join.addCondition(skGameStatisticsByGame, gameId);
				join.addCondition(skGameStatisticsByEmail, email);
				ForwardCursor<EnGameStatistics> cursor = join.entities(txn, CursorConfig.DEFAULT);
				Iterator<EnGameStatistics> entitiesIt = cursor.iterator();
				
				if (entitiesIt.hasNext()) {
					EnGameStatistics enStatistics = entitiesIt.next();
					ret.value = enStatistics;
				}
				
				cursor.close();
			}
		};
		runTransaction.run(false);
		return ret.value;
	}
	
	public EnGameStatistics getTotalGamestatistics(final String email) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<EnGameStatistics> ret = new TransactionReturn<EnGameStatistics>();

		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {				
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EntityIndex<Long, EnGameStatistics> gameStatistics = skGameStatisticsByEmail.subIndex(email);
				ForwardCursor<EnGameStatistics> cursor = gameStatistics.entities(txn, CursorConfig.DEFAULT);
				Iterator<EnGameStatistics> entitiesIt = cursor.iterator();
				
				EnGameStatistics enGameStatisticsToRet = new EnGameStatistics();
				enGameStatisticsToRet.setGameId(-1);
				while (entitiesIt.hasNext()) {
					EnGameStatistics enStatistics = entitiesIt.next();
					enGameStatisticsToRet.setWins(enGameStatisticsToRet.getWins() + enStatistics.getWins());
					enGameStatisticsToRet.setLose(enGameStatisticsToRet.getLoses() + enStatistics.getLoses());
					enGameStatisticsToRet.setDraw(enGameStatisticsToRet.getDraws() + enStatistics.getDraws());
					enGameStatisticsToRet.setAbandon(enGameStatisticsToRet.getAbandon() + enStatistics.getAbandon());
				}
				ret.value = enGameStatisticsToRet;				
				cursor.close();
			}
		};
		runTransaction.run(false);
		return ret.value;
	}
	
	/*
	public static boolean isTrophyAssigned(int trophyType, byte gameEnum, EnUser user) {
		EntityManagerFactory factory = DBHandler.get().getSessionFactory();
		EntityManager entityManager = factory.createEntityManager();
		entityManager.getTransaction().begin();
		
		TypedQuery<EnTrophy> query = entityManager.createQuery("from " + EnTrophy.class.getSimpleName() + " where trophyType = :trophyType and gameEnum = :gameEnum and user = :user", EnTrophy.class);
		query.setParameter("trophyType", trophyType);
		query.setParameter("gameEnum", gameEnum);
		query.setParameter("user", user);
		
		List<EnTrophy> resultList = query.getResultList();
		int listSize = resultList.size();
		if (listSize > 1) {
			throw new RuntimeException("The number of ranking is greater than 1!!!");
		}
		
		return listSize > 0;
	}
	
	public static EnTrophy addTrophy(String email, byte gameEnum, long trophyDate, int trophyType) {
		EnUser user = UserProcess.getUser(email);
		return addTrophy(user, gameEnum, trophyDate, trophyType);
	}
	
	public static EnTrophy addTrophy(EnUser user, byte gameEnum, long trophyDate, int trophyType) {
		if (isTrophyAssigned(trophyType, gameEnum, user)) {
			return null;
		}
		
		EntityManagerFactory factory = DBHandler.get().getSessionFactory();
		EntityManager entityManager = factory.createEntityManager();
		entityManager.getTransaction().begin();
		
		EnTrophy newTrophy = new EnTrophy();
		newTrophy.setTrophyType(trophyType);
		newTrophy.setGameEnum(gameEnum);
		newTrophy.setTrophyDate(trophyDate);
		newTrophy.setUser(user);
		
		entityManager.persist(newTrophy);
		
		entityManager.getTransaction().commit();
		entityManager.close();
		
		return newTrophy;		
	}
	
	public static List<EnTrophy> getTrophies(byte gameEnum, String email) {
		EnUser user = UserProcess.getUser(email);
		return getTrophies(gameEnum, user);		
	}
	
	public static List<EnTrophy> getTrophies(byte gameEnum, EnUser user) {
		EntityManagerFactory factory = DBHandler.get().getSessionFactory();
		EntityManager entityManager = factory.createEntityManager();
		entityManager.getTransaction().begin();
		
		TypedQuery<EnTrophy> query = entityManager.createQuery("from " + EnTrophy.class.getSimpleName() + " where gameEnum = :gameEnum and user = :user", EnTrophy.class);
		query.setParameter("gameEnum", gameEnum);
		query.setParameter("user", user);
		
		List<EnTrophy> resultList = query.getResultList();
		
		entityManager.getTransaction().commit();
		entityManager.close();
		
		return resultList;
	}
	*/
}
