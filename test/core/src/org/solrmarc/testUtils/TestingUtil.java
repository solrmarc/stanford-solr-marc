package org.solrmarc.testUtils;

import java.io.*;
import java.security.Permission;

/**
 * A class containing basic methods (all static) for testing code
 * @author Naomi Dushay
 */
public class TestingUtil
{
	static class ExitException extends SecurityException 
	{
	    private static final long serialVersionUID = -1982617086752946683L;
	    public final int status;
	
	    public ExitException(int status) 
	    {
	        super("There is no escape!");
	        this.status = status;
	    }
	}

	static class NoExitSecurityManager extends SecurityManager 
	{
	    @Override
	    public void checkPermission(Permission perm) 
	    {
	        // allow anything.
	    }
	
	    @Override
	    public void checkPermission(Permission perm, Object context) 
	    {
	        // allow anything.
	    }
	
	    @Override
	    public void checkExit(int status) 
	    {
	        super.checkExit(status);
	        throw new ExitException(status);
	    }
	}

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
