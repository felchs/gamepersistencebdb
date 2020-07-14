package com.gametac.main;

import com.gametac.bdb.DBHandler;

public class DBStandAloneInitializer {
	
	public static void main(String[] args) {
		new Thread() {
			@Override
			public void run() {
				DBHandler.getInstance();
			}
		}.start();
		
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
