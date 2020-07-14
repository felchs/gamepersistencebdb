package com.gametac.bdb;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gametac.bdb.entities.RankingPeriod;
import com.gametac.socket.ByteUtils;
import com.gametac.utils.ReturnInterface;

public class RankingInMemThread extends Thread {
	public static class RankingReturn {
		private LinkedList<RankingStruct> rankingList = new LinkedList<RankingStruct>();
		
		public LinkedList<RankingStruct> getRankingList() {
			return rankingList;
		}
		
		public void addRanking(RankingStruct ranking) {
			rankingList.add(ranking);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private RankingInMem rankingInMem;
	
	private ExecutorService executor;
	
	public RankingInMemThread() {
		this.rankingInMem = new RankingInMem();
		
		this.executor = Executors.newFixedThreadPool(2);
	}
	
	public void updateRanking(final String email, final float points, final long pointsTime) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				rankingInMem.updateRanking(email, points, pointsTime);
			}
		});
	}
	
	public void getRanking(final RankingPeriod period, final int startIdx, final int numPositions, final RankingReturn result) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				LinkedList<RankingStruct> pointsByUser = rankingInMem.getRankingListingWithPeriod(period);
				try {
					for (int i = startIdx; i < numPositions; i++) {
						RankingStruct rankingToRet = pointsByUser.get(startIdx);
						result.rankingList.add(rankingToRet);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void getRankingAsStream(final RankingPeriod period, final int startIdx, final int numPositions, final RankingReturn result) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				LinkedList<RankingStruct> pointsByUser = rankingInMem.getRankingListingWithPeriod(period);
				try {
					for (int i = startIdx; i < numPositions; i++) {
						RankingStruct rankingToRet = pointsByUser.get(startIdx);
						result.rankingList.add(rankingToRet);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void retrieveRankingListAsBytes(final RankingPeriod period, final int startIdx, final int numPositions, final ReturnInterface<byte[]> returnInterface) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				LinkedList<RankingStruct> pointsByUser = rankingInMem.getRankingListingWithPeriod(period);
				try {
					LinkedList<byte[]> bytesToRetList = new LinkedList<byte[]>();
					int totalBytes = 0;
					for (int i = startIdx; i < numPositions; i++) {
						RankingStruct rankingStruct = pointsByUser.get(startIdx);
						String email = rankingStruct.email;
						float points = rankingStruct.points;
						byte[] bytesEmail = email.getBytes(Charset.forName("UTF-8"));
						byte[] bytesPoints = ByteUtils.getBytes(points);
						totalBytes += bytesEmail.length;
						totalBytes += bytesPoints.length;
						bytesToRetList.add(bytesEmail);
						bytesToRetList.add(bytesPoints);
					}
					byte[] bytesToRetArr = new byte[totalBytes];
					Iterator<byte[]> it = bytesToRetList.iterator();
					int idxCopy = 0;
					while (it.hasNext()) {
						byte[] bytesToCopy = it.next();
						for (int i = 0; i < bytesToCopy.length; i++) {
							bytesToRetArr[idxCopy++] = bytesToCopy[i];
						}
					}
					returnInterface.onReturn(bytesToRetArr);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
