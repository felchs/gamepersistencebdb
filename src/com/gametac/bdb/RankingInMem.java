package com.gametac.bdb;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;

import com.gametac.bdb.entities.RankingPeriod;
import com.gametac.utils.DateUtils;

public class RankingInMem {
	private LinkedList<RankingStruct> diaryPointsByUser = new LinkedList<RankingStruct>();
	
	private LinkedList<RankingStruct> semanalPointsByUser = new LinkedList<RankingStruct>();
	
	private LinkedList<RankingStruct> mensalPointsByUser = new LinkedList<RankingStruct>();
	
	private LinkedList<RankingStruct> semestralPointsByUser = new LinkedList<RankingStruct>();
	
	private LinkedList<RankingStruct> annualPointsByUser = new LinkedList<RankingStruct>();
	
	public LinkedList<RankingStruct> getDiaryPointsByUser() {
		return diaryPointsByUser;
	}
	
	public synchronized LinkedList<RankingStruct> getSemanalPointsByUser() {
		return semanalPointsByUser;
	}
	
	public synchronized LinkedList<RankingStruct> getMensalPointsByUser() {
		return mensalPointsByUser;
	}
	
	public synchronized LinkedList<RankingStruct> getSemestralPointsByUser() {
		return semestralPointsByUser;
	}
	
	public synchronized LinkedList<RankingStruct> getAnnualPointsByUser() {
		return annualPointsByUser;
	}

	public LinkedList<RankingStruct> getRankingListingWithPeriod(RankingPeriod period) {
		switch (period) {
		case ANNUAL:			
			return getAnnualPointsByUser();
		case SEMESTRAL:
			return getSemanalPointsByUser();
		case MENSAL:
			return getMensalPointsByUser();
		case SEMANAL:
			return getSemanalPointsByUser();
		case DIARY:
			return getDiaryPointsByUser();
		}
		throw new RuntimeException("Wrong period: " + period);
	}
	
	private void updateRanking(String email, Float points, RankingPeriod period) {
		RankingStruct rankingStruct = new RankingStruct(points, email);
		LinkedList<RankingStruct> rankingListingWithPeriod = getRankingListingWithPeriod(period);
		updateListByPeriod(email, rankingStruct, rankingListingWithPeriod);
	}

	private void updateListByPeriod(String email, RankingStruct rankingStruct, LinkedList<RankingStruct> listToBeUpdated) {
		Collections.sort(listToBeUpdated, RankingStruct.getPointsComparator());
		int binarySearch = Collections.binarySearch(listToBeUpdated, new RankingStruct(email), RankingStruct.getEmailComparator());
		float prevPoints = 0;
		if (binarySearch > 0) {
			RankingStruct rankingStructRemoved = listToBeUpdated.remove(binarySearch);
			prevPoints = rankingStructRemoved.points;
		}
		rankingStruct.points += prevPoints;
		listToBeUpdated.add(rankingStruct);
		Collections.sort(listToBeUpdated, RankingStruct.getPointsComparator());
	}

	public synchronized void updateRanking(String email, float points, long pointsTime) {
		Calendar cal = DateUtils.getCalendar();
		
		// last 24 hours
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long timeLast24Hours = cal.getTimeInMillis();
		
		// last 7 days
		cal.add(Calendar.HOUR_OF_DAY, -24 * 7);
		long timeLast7Days = cal.getTimeInMillis();
		
		// last month
		cal.add(Calendar.HOUR_OF_DAY, -24 * 7 * 3);
		long timeLastMonth = cal.getTimeInMillis();
		
		// last semester
		cal.add(Calendar.HOUR_OF_DAY, -24 * 30 * 6);
		long timeLastSemester = cal.getTimeInMillis();
		
		// last year
		cal.add(Calendar.HOUR_OF_DAY, -24 * 30 * 6);
		long timeLastYear = cal.getTimeInMillis();
		
		if (pointsTime > timeLast24Hours) {
			updateRanking(email, points, RankingPeriod.DIARY);
		}
		
		if (pointsTime > timeLast7Days) {
			updateRanking(email, points, RankingPeriod.SEMANAL);
		}
		
		if (pointsTime > timeLastMonth) {
			updateRanking(email, points, RankingPeriod.MENSAL);
		}
		
		if (pointsTime > timeLastSemester) {
			updateRanking(email, points, RankingPeriod.SEMESTRAL);
		}
		
		if (pointsTime > timeLastYear) {
			updateRanking(email, points, RankingPeriod.DIARY);
		}
	}
}