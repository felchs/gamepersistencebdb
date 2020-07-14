package com.gametac.bdb.entities;

import java.util.HashSet;
import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class EnWallet {
	@PrimaryKey
	private String email;
	
	@SecondaryKey(relate=Relationship.ONE_TO_MANY, relatedEntity=EnMoney.class)
	private Set<Long> enMoneySet = new HashSet<Long>();
	
	private float totalAmount;
	
	public EnWallet() {
	}
	
	public EnWallet(String email) {
		this.email = email;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void addMoney(EnMoney money) {
		long moneyId = money.getId();
		enMoneySet.add(moneyId);
		
		float moneyAmount = money.getAmount();
		increaseTotalAmount(moneyAmount);
	}

	private void increaseTotalAmount(float moneyAmount) {
		this.totalAmount += moneyAmount;
	}
	
	public void increaseTotalAmountWithMoneyEvent(EnMoneyEvent moneyEvent) {
		
	}
	
	public float getTotalAmount() {
		return totalAmount;
	}
}
