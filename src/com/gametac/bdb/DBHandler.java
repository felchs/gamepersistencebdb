package com.gametac.bdb;

import java.util.Properties;

import com.gametac.bdb.accessor.AchievementsDataAccessor;
import com.gametac.bdb.accessor.AwardsDataAccessor;
import com.gametac.bdb.accessor.GameDataAccessor;
import com.gametac.bdb.accessor.GameStatisticsDataAccessor;
import com.gametac.bdb.accessor.RankingDataAccessor;
import com.gametac.bdb.accessor.RankingDataAccessorMapping;
import com.gametac.bdb.accessor.TicketDataAccessor;
import com.gametac.bdb.accessor.UserDataAccessor;
import com.gametac.bdb.remote.RemoteDBHandler;
import com.gametac.utils.PropertiesConfig;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.persist.EntityStore;

public class DBHandler {

	private static DBHandler instance;
	
	public static DBHandler getInstance() {
		if (instance == null) {
			try {
				instance = new DBHandler();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}
	
	public static String[] getArgsFromProperties() {
		return getGamesPropertyFromProperties("args");
	}
	
	public static int[] getGamesIdsFromProperties() {
		String[] gameIds = getGamesPropertyFromProperties("gameIds");
		int[] gameIdsAsInt = new int[gameIds.length];
		for (int i = 0; i < gameIds.length; i++) {
			gameIdsAsInt[i] = Integer.parseInt(gameIds[i]);
		}
		return gameIdsAsInt;
	}
	
	public static String[] getGameNamesFromProperties() {
		return getGamesPropertyFromProperties("gameNames");
	}
	
	public static String[] getGamesPropertyFromProperties(String property) {
		Properties properties = PropertiesConfig.loadProperties();
		String gameIdsProperty = properties.getProperty(property);
		String[] gameIdsAsStr = gameIdsProperty.split(",");
		return gameIdsAsStr;
	}
	
	public static int[] gameIds() {
		return getInstance().gameIds;
	}
	
	public static String[] gameNames() {
		return getInstance().gameNames;
	}
	
	public static GameTypes getGameTypes() {
		return getInstance().gameTypes;
	}
	
	public static UserDataAccessor getUserDataAccessor() {
		return getInstance().userDataAccessor;
	}
	
	public static GameDataAccessor getGameDataAccessor() {
		return getInstance().gameDataAccessor;
	}
	
	public static TicketDataAccessor getTicketDataAccessor() {
		return getInstance().ticketDataAcessor;
	}
	
	public static AwardsDataAccessor getAwardsDataAccessor() {
		return getInstance().awardsDataAccessor;
	}
	
	public static AchievementsDataAccessor getAchievementsDataAccessor() {
		return getInstance().achievementsDataAccessor;
	}

	public static GameStatisticsDataAccessor getGameStatisticsDataAccessor() {
		return getInstance().gameStatisticsDataAccessor;
	}
	
	public static RankingDataAccessorMapping getRankingDataAccessorMapping() {
		return getInstance().rankingDataAccessorMapping;
	}
	
	public static RankingDataAccessor getRankingDataAccessor(int gameId) {
		DBHandler inst = getInstance();
		RankingDataAccessorMapping rankingDataAcc = inst.rankingDataAccessorMapping;
		return rankingDataAcc.getRankingDataAccessor(gameId);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private boolean running = true;
	
	private int[] gameIds;
	
	private String[] gameNames;
	
	private GameTypes gameTypes;
	
	private DBStoreRepHandler dbStoreRepHandler;
	
	private UserDataAccessor userDataAccessor;
	
	private GameDataAccessor gameDataAccessor;
	
	private TicketDataAccessor ticketDataAcessor;
	
	private AwardsDataAccessor awardsDataAccessor;

	private AchievementsDataAccessor achievementsDataAccessor;
	
	private GameStatisticsDataAccessor gameStatisticsDataAccessor;
	
	private RankingDataAccessorMapping rankingDataAccessorMapping;
	
	public DBHandler() throws Exception {
		this(null);
	}
	
	public DBHandler(String[] args) throws Exception {
		instance = this;
		
		if (args == null || args.length == 0) {
			args = DBHandler.getArgsFromProperties();
		}
		
		this.gameIds = DBHandler.getGamesIdsFromProperties();
		
		this.gameNames = DBHandler.getGameNamesFromProperties();
		
		this.gameTypes = new GameTypes(gameIds, gameNames);
		
		this.dbStoreRepHandler = new DBStoreRepHandler(args);

		dbStoreRepHandler.initialize();
		
		ReplicatedEnvironment repEnv = dbStoreRepHandler.getEnvironment();
		EntityStore store = dbStoreRepHandler.getStore();
		
		this.userDataAccessor = new UserDataAccessor("UserDataAccessor", repEnv, store);
		
		this.gameDataAccessor = new GameDataAccessor("GameDataAcessor", repEnv, store);
		
		this.ticketDataAcessor = new TicketDataAccessor("TicketDataAcessor", repEnv, store);
		
		this.awardsDataAccessor = new AwardsDataAccessor("AwardsDataAccessor", repEnv, store);
		
		this.rankingDataAccessorMapping = new RankingDataAccessorMapping(gameIds, repEnv, store);
		
		this.achievementsDataAccessor = new AchievementsDataAccessor("AchievementsDataAcessor", repEnv, store);
		
		this.gameStatisticsDataAccessor = new GameStatisticsDataAccessor("GameStatisticsDataAccessor", repEnv, store);
		
		createGames(gameIds);
		
		RemoteDBHandler.initilize();
		
		new Thread() {
			public void run() {
				while (running) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				try {
					shutDown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
		
		System.out.println("DBHandler initialized.");
	}
	
	private void createGames(int[] gameIds) throws EnvironmentFailureException, InterruptedException {
		for (int i = 0; i < gameIds.length; i++) {
			int gameId = gameIds[i];
			getGameDataAccessor().createGame(gameId);
		}
	}
	
	public DBStoreRepHandler getDbStoreRepHandler() {
		return dbStoreRepHandler;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void shutDown() throws Exception {
		userDataAccessor.shutdown();
		gameDataAccessor.shutdown();
		ticketDataAcessor.shutdown();
		awardsDataAccessor.shutdown();
		rankingDataAccessorMapping.shutdown();
		achievementsDataAccessor.shutdown();
		gameStatisticsDataAccessor.shutdown();
		dbStoreRepHandler.shutdown();
	}
}