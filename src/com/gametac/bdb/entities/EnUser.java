package com.gametac.bdb.entities;

import java.io.Serializable;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class EnUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @PrimaryKey
	private String email;
	
	private String password;
	
	private String name;
	
	private int sex;
	
	private int majority;
	
	private String cep;
	
	private String phone;
	
	private int userType;
	
	private String avatarURL;
	
	private long dateCreation;
	
	public EnUser() {
	}

    public EnUser(String email) {
    	this.email = email;
    	dateCreation = System.currentTimeMillis();
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getSex() {
		return sex;
	}
	
	public void setSex(int sex) {
		this.sex = sex;
	}
	
	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	public int getMajority() {
		return majority;
	}
	
	public void setMajority(int majority) {
		this.majority = majority;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPhone() {
		return phone;
	}
	
	public void setAvatarURL(String avatarURL) {
		this.avatarURL = avatarURL;
	}
	
	public String getAvatarURL() {
		return avatarURL;
	}
	
	public void setUserType(int userType) {
		this.userType = userType;
	}
	
	public int getUserType() {
		return userType;
	}
	
	public long getDateCreation() {
		return dateCreation;
	}
	
	public void setDateCreation(long dateCreation) {
		this.dateCreation = dateCreation;
	}
	
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("email: " + email + "\n");
		strBuilder.append("name: " + name + "\n");
		//strBuilder.append("pass: " + password + "\n");
		strBuilder.append("sex: " + sex);
		return strBuilder.toString();
	}
}