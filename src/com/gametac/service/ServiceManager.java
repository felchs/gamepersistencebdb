package com.gametac.service;

import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

public class ServiceManager {
	
	private static ServiceManager instance;
	
	public static ServiceManager get() {
		if (instance == null) {
			instance = new ServiceManager();
		}
		return instance;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	// one minute
	final long TIME_CHECK_LAUNCH_SERVICES = 1000 * 60;
	
	private TreeMap<Long, Vector<Service>> servicesMap = new TreeMap<Long, Vector<Service>>();
	
	private ServiceManager() {
		new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(TIME_CHECK_LAUNCH_SERVICES);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					checkLaunchService();
				}
			}
		}.start();
	}
	
	private void checkLaunchService() {
		Set<Long> serviceKeys = servicesMap.keySet();
		for (Long key : serviceKeys) {
			Vector<Service> serviceList = servicesMap.get(key);
			for (Service service : serviceList) {
				service.checkLaunchService();
			}
		}
	}
	
	public void addService(Service service) {
		Long startTime = service.getStartTime();
		Vector<Service> servicesList = servicesMap.get(startTime);
		if (servicesList == null) {
			servicesList = new Vector<Service>();
			servicesMap.put(startTime, servicesList);
		}
		servicesList.add(service);
	}
}