package com.gametac.bdb.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class EnLoggedUser {
	@PrimaryKey
	private long ip;
	
	private String userAgent;
	
	@SecondaryKey(relate=Relationship.ONE_TO_ONE, relatedEntity=EnUser.class)
	private String email;
	
	private long loginTime;
	
	public EnLoggedUser() {
	}
		
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	public String getUserAgent() {
		return userAgent;
	}

	public void setIp(long ip) {
		this.ip = ip;
	}
	
	public long getIp() {
		return ip;
	}
	
	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}
	
	public long getLoginTime() {
		return loginTime;
	}
}