package com.gametac.service;

import java.util.Calendar;
import java.util.Vector;

public abstract class Service {
	
	private static Vector<Integer> getOneTime(int timeInIMuntesOfDay) {
		Vector<Integer> v = new Vector<Integer>();
		v.add(timeInIMuntesOfDay);
		return v;
	}
	
	public static Integer getTimeInMinutes(int hour, int minute) {
		return hour * 60 + minute;
	}

	private Vector<Integer> timeOfDayToStartList;
	
	private boolean[] dayOfWeek;
	
	///////////////////////////////////////////////////////////////////////////
	
	private long startTime;
	
	private long endTime;
	
	private boolean active;
	
	public Service(int timeInIMuntesOfDay, boolean[] dayOfWeek) {
		this(getOneTime(timeInIMuntesOfDay), dayOfWeek);
	}
	
	public Service(Vector<Integer> timeOfDayToStartList, boolean[] dayOfWeek) {
		this.timeOfDayToStartList = timeOfDayToStartList;
		this.dayOfWeek = dayOfWeek;
	}
	
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	public long getEndTime() {
		return endTime;
	}
	
	protected void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}

	public Vector<Integer> getTimeOfDayToStartList() {
		return timeOfDayToStartList;
	}
	
	public boolean[] getDayOfWeek() {
		return dayOfWeek;
	}
	
	public void checkLaunchService() {
		if (isActive()) {
			return;
		}
		
		long currTime = System.currentTimeMillis();
		if (currTime < getStartTime() || currTime > getEndTime()) {
			return;
		}
		

		for (final Integer timeToStartInMinutes : timeOfDayToStartList) {
			Calendar cal = Calendar.getInstance();
			int calDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
			
			if (!dayOfWeek[calDayOfWeek]) {
				continue;
			}
			
			new Thread() {
				public void run() {
					setActive(true);
					
					Calendar cal = Calendar.getInstance();
					long timeInMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
					long timeToSleepInMillis = (timeToStartInMinutes - timeInMinutes) * 1000 * 60;
					if (timeToSleepInMillis > 0) {
						try {
							Thread.sleep(timeToSleepInMillis);
							
							// after sleeping the time
							initService();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					setActive(false);
				}
			}.start();
		}
	}
	
	protected abstract void initService();
}