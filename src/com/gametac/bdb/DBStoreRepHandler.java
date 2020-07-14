package com.gametac.bdb;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Durability;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationFailureException;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.je.rep.ReplicationConfig;
import com.sleepycat.je.rep.TimeConsistencyPolicy;
import com.sleepycat.je.rep.UnknownMasterException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class DBStoreRepHandler {
	private static final String STORE_NAME = "Site";

	/* The name of the replication group used by this application. */
	static final String REP_GROUP_NAME = "SiteRepGroup";

	private EntityStore store = null;
	private File envHome;
	EnvironmentConfig envConfig;

	final ReplicationConfig repConfig;
	ReplicatedEnvironment repEnv;

	/* The maximum number of times to retry handle creation. */
	private static int REP_HANDLE_RETRY_MAX = 5;

	/**
	 * Params: -env -nodeName -nodeHost -helperHost hipotetical ex: -env
	 * somedir/otherdir/filename -nodeName someName -nodeHost 127.0.0.1:50002
	 * -helperHost 127.0.0.1:50002
	 */
	public DBStoreRepHandler(String[] params) throws Exception {
		/*
		 * Set envHome and generate a ReplicationConfig. Note that
		 * ReplicationConfig and EnvironmentConfig values could all be specified
		 * in the je.properties file, as is shown in the properties file
		 * included in the example.
		 */
		repConfig = new ReplicationConfig();

		/* Set consistency policy for replica. */
		TimeConsistencyPolicy consistencyPolicy = new TimeConsistencyPolicy(1, TimeUnit.SECONDS, /*
																								 * 1
																								 * sec
																								 * of
																								 * lag
																								 */
		3, TimeUnit.SECONDS /* Wait up to 3 sec */);
		repConfig.setConsistencyPolicy(consistencyPolicy);

		/* Wait up to two seconds for commit acknowledgments. */
		repConfig.setReplicaAckTimeout(2, TimeUnit.SECONDS);
		parseParams(params);

		/*
		 * A replicated environment must be opened with transactions enabled.
		 * Environments on a master must be read/write, while environments on a
		 * client can be read/write or read/only. Since the master's identity
		 * may change, it's most convenient to open the environment in the
		 * default read/write mode. All write operations will be refused on the
		 * client though.
		 */
		envConfig = new EnvironmentConfig();
		envConfig.setTransactional(true);

		Durability durability = new Durability(Durability.SyncPolicy.WRITE_NO_SYNC, Durability.SyncPolicy.WRITE_NO_SYNC, Durability.ReplicaAckPolicy.SIMPLE_MAJORITY);
		envConfig.setDurability(durability);
		envConfig.setAllowCreate(true);
		envConfig.setConfigParam(EnvironmentConfig.CLEANER_FETCH_OBSOLETE_SIZE, "true");
	}

	/**
	 * Creates the replicated environment handle and returns it. It will retry
	 * indefinitely if a master could not be established because a sufficient
	 * number of nodes were not available, or there were networking issues, etc.
	 *
	 * @return the newly created replicated environment handle
	 *
	 * @throws InterruptedException
	 *             if the operation was interrupted
	 */
	ReplicatedEnvironment getEnvironment() {

		DatabaseException exception = null;

		/*
		 * In this example we retry REP_HANDLE_RETRY_MAX times, but a production
		 * HA application may retry indefinitely.
		 */
		for (int i = 0; i < REP_HANDLE_RETRY_MAX; i++) {
			System.out.println("getEnvironment(), trying time: " + i);
			try {
				return new ReplicatedEnvironment(envHome, repConfig, envConfig);

			} catch (UnknownMasterException unknownMaster) {
				exception = unknownMaster;

				/*
				 * Indicates there is a group level problem: insufficient nodes
				 * for an election, network connectivity issues, etc. Wait and
				 * retry to allow the problem to be resolved.
				 */
				System.err.println("Master could not be established. " + "Exception message:" + unknownMaster.getMessage() + " Will retry after 5 seconds.");
				try {
					Thread.sleep(5 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				continue;
			}
		}
		// Failed despite retries
		if (exception != null) {
			throw exception;
		}
		// Don't expect to get here
		throw new IllegalStateException("Failed despite retries");
	}

	public EntityStore getStore() {
		if (store == null) {
			throw new NullPointerException();
		}
		return store;
	}

	/**
	 * Parse the command line parameters for a replication node and set up any
	 * configuration parameters.
	 */
	void parseParams(String[] argv) throws IllegalArgumentException {

		int argc = 0;
		int nArgs = argv.length;

		if (nArgs == 0) {
			usage("-env, -nodeName, -nodeHost, and -helperHost are required.");
		}
		String nodeName = null;
		String nodeHost = null;
		while (argc < nArgs) {
			String thisArg = argv[argc++];

			if (thisArg.equals("-env")) {
				if (argc < nArgs) {
					envHome = new File(argv[argc++]);
				} else {
					usage("-env requires an argument");
				}
			} else if (thisArg.equals("-nodeName")) {
				// the node name
				if (argc < nArgs) {
					nodeName = argv[argc++];
					repConfig.setNodeName(nodeName);
				} else {
					usage("-nodeName requires an argument");
				}
			} else if (thisArg.equals("-nodeHost")) {
				// The node hostname, port pair
				nodeHost = argv[argc++];
				if (argc <= nArgs) {
					repConfig.setNodeHostPort(nodeHost);
				} else {
					usage("-nodeHost requires an argument");
				}
			} else if (thisArg.equals("-helperHost")) {
				// The helper node hostname, port pair
				if (argc < nArgs) {
					repConfig.setHelperHosts(argv[argc++]);
				} else {
					usage("-helperHost requires an argument");
				}
			} else {
				usage("Unknown argument; " + thisArg);
			}
		}
		if (envHome == null) {
			usage("-env is a required parameter");
		}

		if (nodeName == null) {
			usage("-nodeName is a required parameter");
		}

		if (nodeHost == null) {
			usage("-nodeHost is a required parameter");
		}

		// Helper host can be skipped once a anode has joined a group
		repConfig.setGroupName(REP_GROUP_NAME);
	}

	/**
	 * Provides invocation usage help information in response to an error
	 * condition.
	 *
	 * @param message
	 *            an explanation of the condition that provoked the display of
	 *            usage information.
	 */
	void usage(String message) {
		System.out.println();
		System.out.println(message);
		System.out.println();
		System.out.print("usage: " + getClass().getName());

		System.out.println(" -env <environment dir> -nodeName <nodeName> " + "-nodeHost <host:port> -helperHost <host:port> ");

		System.out.println("\t -env the replicated environment directory\n" + "\t -nodeName the unique name associated with this node\n" + "\t -nodeHost the hostname and port pair associated with " + " this node\n" + "\t -helperHost the hostname and port pair associated with " + " the helper node\n");

		System.out.println("All parameters may also be expressed as " + "properties in a je.properties file.");
		System.exit(0);
	}

	/**
	 * Initializes the Environment, entity store and data accessor used by the
	 * example. It's invoked when the application is first started up, and
	 * subsequently, if the environment needs to be re-established due to an
	 * exception.
	 *
	 * @throws InterruptedException
	 */
	void initialize() throws InterruptedException {

		// Initialize the replication handle.
		repEnv = getEnvironment();

		/*
		 * The following two operations -- opening the EntityStore and
		 * initializing it by calling EntityStore.getPrimaryIndex -- don't
		 * require an explicit transaction because they use auto-commit
		 * internally. A specialized RunTransaction class for auto-commit could
		 * be defined, but for simplicity the RunTransaction class is used here
		 * and the txn parameter is simply ignored.
		 */
		new RunTransaction(repEnv, System.out) {

			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				final StoreConfig storeConfig = new StoreConfig();

				/*
				 * An Entity Store in a replicated environment must be
				 * transactional.
				 */
				storeConfig.setTransactional(true);

				/* Both Master and Replica open the store for write. */
				storeConfig.setReadOnly(false);
				storeConfig.setAllowCreate(true);

				store = new EntityStore(repEnv, STORE_NAME, storeConfig);
			}

			@Override
			public void onRetryFailure(OperationFailureException lastException) {

				/* Restart the initialization process. */
				throw lastException;
			}
		}.run(false /* readOnly */);
	}

	/**
	 * Shuts down the application. If this node was the master, then some other
	 * node will be elected in its place, if a simple majority of nodes survives
	 * this shutdown.
	 */
	void shutdown() {
		store.close();
		store = null;

		repEnv.cleanLog();
		repEnv.flushLog(true);
		repEnv.close();
		repEnv = null;
	}

}