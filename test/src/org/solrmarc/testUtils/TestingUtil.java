package org.solrmarc.testUtils;

import java.io.*;

/**
 * A class containing basic methods (all static) for testing code
 * @author Naomi Dushay
 */
public class TestingUtil
{
	/**
	 * create and return a ByteArrayOutputStrem that will replace System.err and 
	 * System.out 
	 */
    public static ByteArrayOutputStream getSysMsgsBAOS()
    {
    	// grab error message  (should check logs too?)
    	ByteArrayOutputStream sysBAOS = new ByteArrayOutputStream();
    	PrintStream sysMsgs = new PrintStream(sysBAOS);
    	System.setErr(sysMsgs);
    	System.setOut(sysMsgs);
    	return sysBAOS;
    }

}
