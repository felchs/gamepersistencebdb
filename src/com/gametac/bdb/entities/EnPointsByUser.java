package com.gametac.bdb.entities;

import java.util.HashSet;
import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class EnPointsByUser {
	@PrimaryKey
	String email;

	@SecondaryKey(relate=Relationship.ONE_TO_MANY, relatedEntity=EnPoints.class)
	Set<Long> enPointsSet = new HashSet<Long>();

	public EnPointsByUser() {
	}
	
	public EnPointsByUser(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public Set<Long> getEnPointsSet() {
		return enPointsSet;
	}

	public void addEnPoint(long pointId) {
		enPointsSet.add(pointId);
	}
}