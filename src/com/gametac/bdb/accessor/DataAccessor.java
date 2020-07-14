package com.gametac.bdb.accessor;

import java.util.Hashtable;

import com.gametac.bdb.Command;
import com.gametac.bdb.RunTransaction;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.je.OperationFailureException;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.persist.EntityStore;

public abstract class DataAccessor {
	private static Hashtable<String, DataAccessor> dataAccessorMap = new Hashtable<String, DataAccessor>();
	
	public DataAccessor getDataAccessor(String dataAccessorKey) {
		return dataAccessorMap.get(dataAccessorKey);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	protected final String dataAccessorKey;
	
	protected final ReplicatedEnvironment repEnv;
	
	protected final EntityStore store;
	
	public DataAccessor(final String dataAccessorKey, final boolean readOnly, final ReplicatedEnvironment repEnv, final EntityStore store) throws EnvironmentFailureException, InterruptedException {
		this.dataAccessorKey = dataAccessorKey;
		
		this.repEnv = repEnv;
		
		this.store = store;
		
		final DataAccessor myself = this;
		
        new RunTransaction(repEnv, System.out) {

            @Override
            public void doTransactionWork(Transaction txn, Command command) {
            	try {
					initalize(readOnly);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
            	
                dataAccessorMap.put(dataAccessorKey, myself);
            }

            @Override
            public void onRetryFailure
                (OperationFailureException lastException) {

                /* Restart the initialization process. */
                throw lastException;
            }
        }.run(readOnly);
	}
	
	public String getDataAccessorKey() {
		return dataAccessorKey;
	}
	
	protected abstract void initalize(boolean readOnly) throws Exception;
	
	public void shutdown() throws Exception { }
}
