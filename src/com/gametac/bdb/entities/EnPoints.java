package com.gametac.bdb.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class EnPoints {
	@PrimaryKey(sequence="ID")
	long id;
	
	String email;
	
	long time;
	
	float points;
	
	public EnPoints() {
	}
	
	public EnPoints(String email, long time, float points)
	{
		this.email = email;
		this.time = time;
		this.points = points;
	}
	
	public long getId() {
		return id;
	}
	
	public String getEmail() {
		return email;
	}
	
	public long getTime() {
		return time;
	}
	
	public float getPoints() {
		return points;
	}
	
	public void setPoints(float points) {
		this.points = points;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("id: " + id + "\n");
		str.append("email: " + email + "\n");
		str.append("time: " + time + "\n");
		str.append("points: " + points);
		return str.toString();
	}
}
