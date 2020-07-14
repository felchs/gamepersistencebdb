package com.gametac.bdb.accessor;

import com.gametac.bdb.Command;
import com.gametac.bdb.RunTransaction;
import com.gametac.bdb.entities.EnMoney;
import com.gametac.bdb.entities.EnMoneyEvent;
import com.gametac.bdb.entities.EnWallet;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class MoneyDataAccessor extends DataAccessor {
	private Database moneyDb;
	
	private PrimaryIndex<Long, EnMoney> pkMoney;
	
	private SecondaryIndex<String, Long, EnMoney> skMoneyByEmail;

	private PrimaryIndex<Long, EnMoneyEvent> pkMoneyEvent;
	
	private SecondaryIndex<String, Long, EnMoneyEvent> skMoneyEventByEmail;
	
	private SecondaryIndex<Integer, Long, EnMoneyEvent> skMoneyEventByGame;
	
	private PrimaryIndex<String, EnWallet> pkWallet;

	public MoneyDataAccessor(String dataAccessorKey, boolean readOnly, ReplicatedEnvironment repEnv, EntityStore store) throws EnvironmentFailureException, InterruptedException {
		super(dataAccessorKey, readOnly, repEnv, store);
	}

	@Override
	protected void initalize(boolean readOnly) throws Exception {
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
		        DatabaseConfig dbConfig = new DatabaseConfig();
		        dbConfig.setTransactional(true);
		        dbConfig.setAllowCreate(true);
		        dbConfig.setSortedDuplicates(true);
		        moneyDb = repEnv.openDatabase(txn, "MoneyDB_" + dataAccessorKey, dbConfig);
		        
		        pkMoney = store.getPrimaryIndex(Long.class, EnMoney.class);
		        skMoneyByEmail = store.getSecondaryIndex(pkMoney, String.class, "email");
		        pkMoneyEvent = store.getPrimaryIndex(Long.class, EnMoneyEvent.class);
		        skMoneyEventByEmail = store.getSecondaryIndex(pkMoneyEvent, String.class, "email");
		        skMoneyEventByGame = store.getSecondaryIndex(pkMoneyEvent, Integer.class, "game");
		        pkWallet =  store.getPrimaryIndex(String.class, EnWallet.class);
			}
		};
		runTransaction.run(readOnly);
	}

	public void addMoney(final String email, final float amount, final long timeStamp, final boolean virtual) {
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {				
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnMoney enMoney = new EnMoney(email, amount, timeStamp, virtual);
				pkMoney.put(txn, enMoney);
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
	
	public SecondaryIndex<String, Long, EnMoney> getSkMoneyByEmail() {
		return skMoneyByEmail;
	}
	
	public void addMoneyEvent(final String email, final String event) {
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnMoneyEvent enMoneyEvent = new EnMoneyEvent(email, event);
				pkMoneyEvent.put(txn, enMoneyEvent);
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
	
	public SecondaryIndex<String, Long, EnMoneyEvent> getSkMoneyEventByEmail() {
		return skMoneyEventByEmail;
	}
	
	public SecondaryIndex<Integer, Long, EnMoneyEvent> getSkMoneyEventByGame() {
		return skMoneyEventByGame;
	}
	
	public void addWallet(final String email) {
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnWallet enWallet = new EnWallet(email);
				pkWallet.put(txn, enWallet);
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
	
	@Override
	public void shutdown() {
		moneyDb.close();
	}
}