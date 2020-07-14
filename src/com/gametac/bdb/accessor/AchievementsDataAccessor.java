package com.gametac.bdb.accessor;

import java.util.ArrayList;
import java.util.Iterator;

import com.gametac.bdb.Command;
import com.gametac.bdb.RunTransaction;
import com.gametac.bdb.entities.EnAchievements;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class AchievementsDataAccessor extends DataAccessor {
	private PrimaryIndex<Integer, EnAchievements> pkAchievementsById;
	
	private SecondaryIndex<Integer, Integer, EnAchievements> skAchievementsByGame;
	
	private SecondaryIndex<String, Integer, EnAchievements> skAchievementsByEmail;

	public AchievementsDataAccessor(String dataAccessorKey, ReplicatedEnvironment repEnv, EntityStore store) throws EnvironmentFailureException, InterruptedException {
		super(dataAccessorKey, false, repEnv, store);
	}

	@Override
	protected void initalize(boolean readOnly) throws EnvironmentFailureException, InterruptedException {
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				pkAchievementsById = store.getPrimaryIndex(Integer.class, EnAchievements.class);
				skAchievementsByGame = store.getSecondaryIndex(pkAchievementsById, Integer.class, "gameId");
				skAchievementsByEmail = store.getSecondaryIndex(pkAchievementsById, String.class, "email");        
			}
		};
		runTransaction.run(readOnly);
	}
	
	public PrimaryIndex<Integer, EnAchievements> getPkAchievementsById() {
		return pkAchievementsById;
	}
	
	public SecondaryIndex<String, Integer, EnAchievements> getSkAchievementsByEmail() {
		return skAchievementsByEmail;
	}
	
	public SecondaryIndex<Integer, Integer, EnAchievements> getSkAchievementsByGame() {
		return skAchievementsByGame;
	}
	
//	public void updateAchievements(AchievementDBDTO achievementsDTO) {
//		ArrayList<AchievementDBDTO> achievementsDTOList = new ArrayList<AchievementDBDTO>();
//		achievementsDTOList.add(achievementsDTO);
//		updateAchievements(achievementsDTOList);
//	}

	public void updateAchievement(final String email, final int gameId, final int achievementId, final int quantityAchieved, final int quantityRequired) {
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnAchievements enAchievement = skAchievementsByGame.subIndex(gameId).get(achievementId);
				boolean newEntity = false;
				if (enAchievement == null) {
					newEntity = true;
					enAchievement = new EnAchievements();
					enAchievement.setEmail(email);
					enAchievement.setGameId(gameId);
				}
				
				enAchievement.setAchievementId(achievementId);
				enAchievement.setQuantityAchieved(quantityAchieved);
				enAchievement.setQuantityRequired(quantityRequired);
				
				// update
				if (!newEntity) {
					pkAchievementsById.delete(txn, enAchievement.getAchievementId());
				}
				pkAchievementsById.put(enAchievement);
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
	
	public ArrayList<EnAchievements> getAchievements(final int gameId, final String email) throws EnvironmentFailureException, InterruptedException {
		final ArrayList<EnAchievements> achievementsToRet = new ArrayList<EnAchievements>();

		RunTransaction transaction = new RunTransaction(repEnv, System.out) {				
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				CursorConfig cursorConfig = CursorConfig.DEFAULT;
				EntityCursor<EnAchievements> entities = skAchievementsByEmail.subIndex(email).entities(txn, gameId, true, gameId, true, cursorConfig);
				Iterator<EnAchievements> entitiesIt = entities.iterator();
				while (entitiesIt.hasNext()) {
					EnAchievements enAchievement = entitiesIt.next();
					achievementsToRet.add(enAchievement);
//					AchievementDBDTO achievementToRet = new AchievementDBDTO();
//					achievementToRet.achievementId = enAchievement.getAchievementId();
//					achievementToRet.achievemntDate = enAchievement.getAchievemntDate();
//					achievementToRet.email = enAchievement.getEmail();
//					achievementToRet.gameId = enAchievement.getGameId();
//					achievementToRet.quantityAchieved = enAchievement.getQuantityRequired();
//					achievementToRet.quantityRequired = enAchievement.getQuantityAchieved();
//					achievementsToRet.add(achievementToRet);
				}
			}
		};
		transaction.run(true);
		return achievementsToRet;
	}
}
