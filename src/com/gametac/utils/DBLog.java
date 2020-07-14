package com.gametac.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DBLog {
	public static boolean doLogging = true;

	public static void log(String message, boolean logAnyway) {
		if (doLogging && logAnyway) {
			Logger.getLogger("InfomedLogger").log(Level.INFO, message);
		}
	}
	
	public static void log(String message) {
		if (doLogging) {
			Logger.getLogger("InfomedLogger").log(Level.INFO, message);
		}
	}
	
	public static void log(Level level, String message) {
		if (doLogging) {
			Logger.getLogger("InfomedLogger").log(level, message);
		}
	}
}
