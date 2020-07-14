package com.gametac.bdb;

import java.util.ArrayList;

import com.gametac.bdb.DBHandler;
import com.gametac.bdb.accessor.AwardsDataAccessor;
import com.gametac.bdb.entities.EnAward;

public class AwardsDataAccessorTest {
	
	public static void main(String[] args) {
		//args = new String[] { "-env", "D:/tmp", "-nodeName", "someName", "-nodeHost", "127.0.0.1:50002", "-helperHost", "127.0.0.1:50002" };
		
		try {
			DBHandler.getUserDataAccessor().isIpLogged(2);
			
			DBHandler.getUserDataAccessor().createUser("test@test.com", "aPass", 0);
			
			DBHandler.getAwardsDataAccessor().addAwardType(11);
			DBHandler.getAwardsDataAccessor().addAwardType(21);

			AwardsDataAccessor awardsDataAccessor = DBHandler.getAwardsDataAccessor();
			
			int awardType = 11;
			String email = "test@test.com";
			long date = 0;
			int gameId = 0;
			awardsDataAccessor.createAwards(email, gameId, date, awardType);
			
			awardType = 11;
			email = "test@test.com";
			date = 1;
			gameId = 1;
			awardsDataAccessor.createAwards(email, gameId, date, awardType);
	
			awardType = 21;
			email = "test@test.com";
			date = 2;
			gameId = 0;
			awardsDataAccessor.createAwards(email, gameId, date, awardType);
			
			ArrayList<EnAward> awards = awardsDataAccessor.getAwards(2, 21, "test@test.com");
			for (EnAward awardDBDTO : awards) {
				System.out.println("----------------------");
				System.out.println(awardDBDTO);
			}
			
			System.out.println("out");
			
			/*
			GameStatisticsDataAccessor gameStatisticsDataAccessor = DBHandler.getGameStatisticsDataAccessor();
			GameStatisticsDBDTO gameStatisticsStruct = new GameStatisticsDBDTO();
			gameStatisticsStruct.gameId = gameIds[0];
			gameStatisticsStruct.email = "test@test.com";
			gameStatisticsStruct.wins += 1;
			gameStatisticsDataAccessor.updateGameStatistics(gameStatisticsStruct);
			
			GameStatisticsDBDTO gameStatisticsStruct2 = new GameStatisticsDBDTO();
			gameStatisticsStruct2.gameId = gameIds[1];
			gameStatisticsStruct2.email = "test@test.com";
			gameStatisticsStruct2.wins += 1;
			gameStatisticsDataAccessor.updateGameStatistics(gameStatisticsStruct2);		
						
			GameStatisticsDBDTO gamestatistics = gameStatisticsDataAccessor.getGamestatistics(gameIds[1], "test@test.com");
			System.out.println(gamestatistics);
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}