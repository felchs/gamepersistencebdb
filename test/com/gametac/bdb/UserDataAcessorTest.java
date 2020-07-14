package com.gametac.bdb;

import com.gametac.bdb.DBHandler;
import com.sleepycat.je.EnvironmentFailureException;

public class UserDataAcessorTest {
	public static void main(String[] args) throws EnvironmentFailureException, InterruptedException {
		System.out.println(DBHandler.getUserDataAccessor().isIpLogged(2));
		
		System.out.println(DBHandler.getUserDataAccessor().createAnonymousUser("bane", "password", 0, "userAgent"));
	}
}
