package com.gametac.bdb.accessor;

import java.util.ArrayList;
import java.util.Iterator;

import com.gametac.bdb.Command;
import com.gametac.bdb.RunTransaction;
import com.gametac.bdb.entities.EnAward;
import com.gametac.bdb.entities.EnAwardType;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.ForwardCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class AwardsDataAccessor extends DataAccessor {
	private PrimaryIndex<Integer, EnAward> pkAwardsById;

	private PrimaryIndex<Integer, EnAwardType> pkAwardTypeById;
	
	private SecondaryIndex<Integer, Integer, EnAward> skAwardsByGame;
	
	private SecondaryIndex<String, Integer, EnAward> skAwardsByEmail;
	
	private SecondaryIndex<Integer, Integer, EnAward> skAwardsByAwardsType;

	public AwardsDataAccessor(String dataAccessorKey, ReplicatedEnvironment repEnv, EntityStore store) throws EnvironmentFailureException, InterruptedException {
		super(dataAccessorKey, false, repEnv, store);
	}

	@Override
	protected void initalize(boolean readOnly) throws Exception {
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				pkAwardsById = store.getPrimaryIndex(Integer.class, EnAward.class);
				pkAwardTypeById = store.getPrimaryIndex(Integer.class, EnAwardType.class);
				skAwardsByGame = store.getSecondaryIndex(pkAwardsById, Integer.class, "gameId");
				skAwardsByEmail = store.getSecondaryIndex(pkAwardsById, String.class, "email");
				skAwardsByAwardsType = store.getSecondaryIndex(pkAwardsById, Integer.class, "awardType");
			}
		};
		runTransaction.run(readOnly);
		
		initAwardsTypes();
	}
	
	private void initAwardsTypes() throws EnvironmentFailureException, InterruptedException {
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				int[] awards = EnAwardType.getAwards();
				for (int award : awards) {
					EnAwardType enAwardType = new EnAwardType(award);
					if (!pkAwardTypeById.contains(award)) {
						pkAwardTypeById.put(txn, enAwardType);
					}
				}
			}
		};
		transaction.run(false);
	}

	public SecondaryIndex<String, Integer, EnAward> getSkAwardsByEmail() {
		return skAwardsByEmail;
	}
	
	public SecondaryIndex<Integer, Integer, EnAward> getSkAwardsByGame() {
		return skAwardsByGame;
	}
	
	public void createAwards(final String email, final int gameId, final long date, final int awardType) throws EnvironmentFailureException, InterruptedException {
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnAward enAward = new EnAward();
				enAward.setEmail(email);
				enAward.setGameId(gameId);
				enAward.setDate(date);
				enAward.setAwardType(awardType);
				
				pkAwardsById.put(txn, enAward);
			}
		};
		transaction.run(false);
	}
	
	public void updateAwards(final String email, final int gameId, final int awardId, final long date, final int awardType) throws EnvironmentFailureException, InterruptedException {
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnAward enAward = new EnAward();
				enAward.setId(awardId);
				enAward.setEmail(email);
				enAward.setGameId(gameId);
				enAward.setDate(date);
				enAward.setAwardType(awardType);
				
				// update
				if (pkAwardsById.get(awardId) != null) {
					pkAwardsById.delete(txn, awardId);
				}
				pkAwardsById.put(txn, enAward);
			}
		};
		transaction.run(false);
	}

	public ArrayList<EnAward> getAwards(final int gameId, final String email) throws EnvironmentFailureException, InterruptedException {
		final ArrayList<EnAward> awardsDTOToRet = new ArrayList<EnAward>();

		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EntityJoin<Integer, EnAward> join = new EntityJoin<Integer, EnAward>(pkAwardsById);
				join.addCondition(skAwardsByGame, gameId);
				join.addCondition(skAwardsByEmail, email);
				ForwardCursor<EnAward> cursor = join.entities(txn, CursorConfig.DEFAULT);
				Iterator<EnAward> entitiesIt = cursor.iterator();
				while (entitiesIt.hasNext()) {
					EnAward enAward = entitiesIt.next();
					awardsDTOToRet.add(enAward);
//					AwardDBDTO awardToRet = new AwardDBDTO();
//					awardToRet.awardId = enAward.getId();
//					awardToRet.date = enAward.getDate();
//					awardToRet.gameId = enAward.getGameId();
//					awardToRet.email = enAward.getEmail();
//					awardToRet.awardType = enAward.getAwardType();
//					awardsDTOToRet.add(awardToRet);
				}
				
				cursor.close();
			}
		};
		transaction.run(true);
		
		return awardsDTOToRet;
	}
	
	public ArrayList<EnAward> getAwards(final int gameId, final int awardType, final String email) throws EnvironmentFailureException, InterruptedException {
		final ArrayList<EnAward> awardsDTOToRet = new ArrayList<EnAward>();

		RunTransaction transaction = new RunTransaction(repEnv, System.out) {				
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EntityJoin<Integer, EnAward> join = new EntityJoin<Integer, EnAward>(pkAwardsById);
				join.addCondition(skAwardsByGame, gameId);
				join.addCondition(skAwardsByEmail, email);
				join.addCondition(skAwardsByAwardsType, awardType);
				ForwardCursor<EnAward> cursor = join.entities(txn, CursorConfig.DEFAULT);
				Iterator<EnAward> entitiesIt = cursor.iterator();
				while (entitiesIt.hasNext()) {
					EnAward enAward = entitiesIt.next();
					awardsDTOToRet.add(enAward);
//					AwardDBDTO awardToRet = new AwardDBDTO();
//					awardToRet.awardId = enAward.getId();
//					awardToRet.date = enAward.getDate();
//					awardToRet.gameId = enAward.getGameId();
//					awardToRet.email = enAward.getEmail();
//					awardToRet.awardType = enAward.getAwardType();
//					awardsDTOToRet.add(awardToRet);
				}
				
				cursor.close();
			}
		};
		transaction.run(true);
		
		return awardsDTOToRet;
	}

	public void addAwardType(final int awardTypeId) throws EnvironmentFailureException, InterruptedException {
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnAwardType enAwardType = new EnAwardType(awardTypeId);
				pkAwardTypeById.put(txn, enAwardType);
			}
		};
		transaction.run(false);
	}
}