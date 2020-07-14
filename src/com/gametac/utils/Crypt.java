package com.gametac.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;

import org.jasypt.util.password.StrongPasswordEncryptor;

public class Crypt {
	private static Crypt instance;
	
	public static Crypt get() {
		if (instance == null) {
			instance = new Crypt();
		}
		return instance;
	}
	
	public String encrypt(String stringToEncript) {
		return get().getPassEncryptor().encryptPassword(stringToEncript);
	}
	
	public boolean check(String plainPassword, String encryptedPassword) {
		return get().getPassEncryptor().checkPassword(plainPassword, encryptedPassword);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
	
	private Crypt() {
	}
	
	public StrongPasswordEncryptor getPassEncryptor() {
		return passwordEncryptor;
	}
	
	public String getRandomB64String(int numChars) {
		SecureRandom random = new SecureRandom();  
		String stringToEncode = new BigInteger(numChars, random).toString(32);
		byte[] bytes = null;
		try {
			bytes = stringToEncode.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		byte[] encodeBase64 = Base64.encodeBase64(bytes);
		return new String(encodeBase64);
	}
	
    public static void main(String [] args) throws Exception {
        String password = "This is a Password";
        
        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword(password);
        assertTrue(Base64.isArrayByteBase64(encryptedPassword.getBytes("US-ASCII")));

        assertTrue(passwordEncryptor.checkPassword(password, encryptedPassword));
        
        String password2 = "This is a  Password";
        assertFalse(passwordEncryptor.checkPassword(password2, encryptedPassword));

        StrongPasswordEncryptor digester2 = new StrongPasswordEncryptor();
        assertTrue(digester2.checkPassword(password, encryptedPassword));
        
        assertFalse(passwordEncryptor.encryptPassword(password).equals(passwordEncryptor.encryptPassword(password)));
        
        StrongPasswordEncryptor digester3 = new StrongPasswordEncryptor();
        encryptedPassword = digester3.encryptPassword(password);
        assertTrue(digester3.checkPassword(password, encryptedPassword));
    }

	private static void assertFalse(boolean res) {
		System.out.println("Res must be false, result: " + res);
	}

	private static void assertTrue(boolean res) {
		System.out.println("Res must be true, result: " + res);
	}

}