package com.dewa.uccxreports.util;

public class CustomWidgetLog {
	
//	public static String getCurrentClassAndMethodName() {
//		final StackTraceElement e = Thread.currentThread().getStackTrace()[2];
//		final String s = e.getClassName();
//		return s.substring(s.lastIndexOf('.') + 1, s.length()) + "." + e.getMethodName();
//		}
	public static String getCurrentClassAndMethodName() {
	    // Get the stack trace element representing the caller of this method
	    final StackTraceElement e = Thread.currentThread().getStackTrace()[2];
	    
	    // Get the fully qualified class name of the caller's class
	    final String s = e.getClassName();
	    
	    // Extract the simple class name (without package) and append the method name
	    String simpleClassName = s.substring(s.lastIndexOf('.') + 1);
	    String methodName = e.getMethodName();
	    String result = simpleClassName + "." + methodName;
	    
	    return result;
	}

}
