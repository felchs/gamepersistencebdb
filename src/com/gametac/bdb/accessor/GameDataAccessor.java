package com.gametac.bdb.accessor;

import com.gametac.bdb.Command;
import com.gametac.bdb.RunTransaction;
import com.gametac.bdb.entities.EnGame;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class GameDataAccessor extends DataAccessor {	
	private PrimaryIndex<Integer, EnGame> pkGameById;
	
	public GameDataAccessor(String dataAccessorKey, ReplicatedEnvironment repEnv, EntityStore store) throws EnvironmentFailureException, InterruptedException {		
		this(dataAccessorKey, true, repEnv, store);
	}
	
	public GameDataAccessor(String dataAccessorKey, boolean readOnly, ReplicatedEnvironment repEnv, EntityStore store) throws EnvironmentFailureException, InterruptedException {
		super(dataAccessorKey, readOnly, repEnv, store);
	}
		
	@Override
	protected void initalize(boolean readOnly) throws Exception {
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				pkGameById = store.getPrimaryIndex(Integer.class, EnGame.class);
			}
		};
		runTransaction.run(readOnly);
	}
	
	public PrimaryIndex<Integer, EnGame> getPkGameById() {
		return pkGameById;
	}
	
	public EnGame getGame(int gameId) {
		return pkGameById.get(gameId);
	}
	
	public void createGame(final int gameId) throws EnvironmentFailureException, InterruptedException {
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnGame enGame = pkGameById.get(gameId);
				if (enGame == null) {
					EnGame enNewGame = new EnGame(gameId);
					pkGameById.put(txn, enNewGame);
				}
			}
		};
		runTransaction.run(false);
	}
}
