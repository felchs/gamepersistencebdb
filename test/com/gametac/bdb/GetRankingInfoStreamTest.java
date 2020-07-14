package com.gametac.bdb;

import java.io.IOException;
import java.io.OutputStream;

import com.gametac.bdb.accessor.RankingDataAccessor;
import com.gametac.bdb.entities.RankingPeriod;
import com.gametac.utils.ObjectReturn;
import com.sleepycat.je.EnvironmentFailureException;

public class GetRankingInfoStreamTest {

	public static void main(String[] args) {
		RankingDataAccessor rankingDataAccessor = DBHandler.getRankingDataAccessor(0);
		
		/*
		UserDataAccessor userDataAccessor = DBHandler.getUserDataAccessor();
		
		ArrayList<EnUser> userArr = new ArrayList<EnUser>();
		for (int i = 0; i < 10; i++) {
			String email = "test" + i + "@test.com";
			try {
				EnUser user = userDataAccessor.createUser(email, "test", 0);
				userArr.add(user);
			} catch (EnvironmentFailureException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for (int i = 0; i < 100; i++) {
			int idx = (int)(userArr.size() * new Random().nextDouble());
			String email = userArr.get(idx).getEmail();
			int gameId = 0;
			float points = (float) Math.random();
			long randomTime = System.currentTimeMillis() - (long) (1000 * 60 * 60 * 24 * Math.random());
			rankingDataAccessor.updateRanking(email, gameId, points, randomTime);
			if (i % 10 == 0)
			{
				System.out.println("I: " + i);
			}
		}
		
		System.out.println("finished inserting...");
		System.out.println("-----------------------");
		
		PrimaryIndex<Long, EnPoints> pkPoints = rankingDataAccessor.getPkPoints();
		Iterator<EnPoints> it = pkPoints.entities().iterator();
		while (it.hasNext()) {
			EnPoints points = it.next();
			System.out.println(points);
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}*/
		
		System.out.println("-----------------");
		System.out.println("Retrieving as stream...");
		
		RankingPeriod period = RankingPeriod.DIARY;
		final OutputStream os = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				System.out.println("write: " + b);
			}
		};
		
		try {
			int startIdx = 0;
			int numPositions = 100;
			final ObjectReturn<Boolean> wait = new ObjectReturn<Boolean>(false);
			
			rankingDataAccessor.retrieveRankingListAsBytes(period, startIdx, numPositions, new ObjectReturn<byte[]>() {
				@Override
				public void onReturn(byte[] object) {
					try {
						os.write(object);
						os.flush();
						wait.obj = true;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			
			while (!(Boolean)wait.obj) {
				try {
					System.out.println("sleeping");
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		} catch (EnvironmentFailureException e) {
			e.printStackTrace();
		}
		
		try {
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("finished all.");
	}
}
