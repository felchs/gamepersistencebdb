package com.gametac.bdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class RankingStruct {
	private static Comparator<RankingStruct> emailComparator;
	
	private static Comparator<RankingStruct> pointsComparator;

	public static Comparator<RankingStruct> getEmailComparator() {
		if (emailComparator == null) {
			emailComparator = new Comparator<RankingStruct>() {
				@Override
				public int compare(RankingStruct o1, RankingStruct o2) {
					if (o1.email.equals(o2.email)) {
						return 0;
					}
					return 1;
				}
			};
		}
		
		return emailComparator;
	}
	
	public static Comparator<RankingStruct> getPointsComparator() {
		if (pointsComparator == null) {
			pointsComparator = new Comparator<RankingStruct>() {
				@Override
				public int compare(RankingStruct o1, RankingStruct o2) {
					float res = o1.points - o2.points;
					if (Math.abs(res) < 0.00001) {
						return 0;
					}
					return res < 0 ? -1 : 1;
				}
			};
		}
		
		return pointsComparator;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	float points;
	
	String email;
	
	public RankingStruct(String email) {
		this(0, email);
	}
	
	public RankingStruct(float points, String email) {
		this.points = points;
		this.email = email;
	}
	
	public float getPoints() {
		return points;
	}
	
	public String getEmail() {
		return email;
	}
	
	public static void main(String[] args) {
		
		ArrayList<RankingStruct> strlist = new ArrayList<RankingStruct>();
		
		RankingStruct rankingToSearch = null;

		Comparator<RankingStruct> c = new Comparator<RankingStruct>() {
			@Override
			public int compare(RankingStruct o1, RankingStruct o2) {
				float res = o1.points - o2.points;
				if (Math.abs(res) < 0.00001) {
					return 0;
				}
				return res < 0 ? -1 : 1;
			}
		};
		
		for (int i = 0; i < 10000; i++) {
			RankingStruct newRanking = new RankingStruct((new Random()).nextFloat(), "email" + i);
			
			if (rankingToSearch == null) {
				rankingToSearch = newRanking;
			}
			
			strlist.add(newRanking);
			
			Collections.sort(strlist, c);
		}
		
		System.out.println(strlist.size());
		int binarySearch = Collections.binarySearch(strlist, rankingToSearch, c);
		System.out.println(binarySearch);
		System.out.println(strlist.get(binarySearch).points);
		RankingStruct updatedRanking = strlist.remove(binarySearch);
		updatedRanking.points = 2.3f;
		
		int binarySearch2 = Collections.binarySearch(strlist, updatedRanking, c);
		System.out.println(binarySearch2);
		strlist.add(updatedRanking);
		
		Collections.sort(strlist, c);

		int binarySearch3 = Collections.binarySearch(strlist, updatedRanking, c);
		System.out.println(binarySearch3);
		
		System.out.println(strlist.size());
		System.out.println(strlist.get(strlist.size() - 1).points);
		
		System.out.println(strlist.get(binarySearch3).points);
	}
	
}
