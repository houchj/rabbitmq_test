package com.sap.sme.occ.product;

import org.apache.log.Logger;

public class BeanShellTest1 {

	public static void main(String[] args) {
		for(int i = 0; i < args.length; i++)
			System.out.println(args[i]);
		
		
		
	}

	public static int sum(int a, int b) {
		return a + b;
	}
	
	public static void log(Logger logger, String msg) {
		logger.info(msg);
	}
	
	public static String getSysEnv(Logger logger) {
		String env = System.getProperty("AAA");
		logger.info("AAA is " + env);
		return env;
	}
}
