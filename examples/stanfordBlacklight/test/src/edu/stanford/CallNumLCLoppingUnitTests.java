package edu.stanford;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * unit tests for edu.stanford.CallNumberUtils LC lopping methods
 * @author Naomi Dushay
 */
public class CallNumLCLoppingUnitTests extends AbstractStanfordBlacklightTest {

	/**
	 * test that LC lopping doesn't go after the class number when it looks 
	 *  like a year - one cutter, no suffix
	 */
@Test
	public void testLCNoLopClassLikeYearNoSuffix() 
	{
		String callnum = "PN1998 .S589";
		assertEquals("PN1998 .S589", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN1998 .S589", CallNumUtils.removeLCVolSuffix(callnum));
		
		callnum = "PN2007 .S3";
		assertEquals("PN2007 .S3", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN2007 .S3", CallNumUtils.removeLCVolSuffix(callnum));
	}

	/**
	 * test that LC lopping doesn't go after the class number when it looks 
	 *  like a year - suffix after first cutter
	 */
@Test
	public void testLCNoLopClassLikeYearOneCutterSuffix() 
	{
		String callnum;
		// volume designation first
		callnum = "PN2007 .S589 NO.17 1998";
		assertEquals("PN2007 .S589", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN2007 .S589", CallNumUtils.removeLCVolSuffix(callnum));
		
		callnum = "PN2007 .K3 V.7:NO.4";
		assertEquals("PN2007 .K3", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN2007 .K3", CallNumUtils.removeLCVolSuffix(callnum));
		
		callnum = "PN2007 .K3 V.8:NO.1-2 1972";
		assertEquals("PN2007 .K3", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN2007 .K3", CallNumUtils.removeLCVolSuffix(callnum));
		
		callnum = "PN2007 .K3 V.5-6:NO.11-25 1967-1970";
		assertEquals("PN2007 .K3", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN2007 .K3", CallNumUtils.removeLCVolSuffix(callnum));

		callnum = "PN2007 .S3 NO.14-15,34";
		assertEquals("PN2007 .S3", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN2007 .S3", CallNumUtils.removeLCVolSuffix(callnum));
		
		// year first
		callnum = "PN2007 .S3 1987";
		assertEquals("PN2007 .S3", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN2007 .S3 1987", CallNumUtils.removeLCVolSuffix(callnum));
		
		callnum = "PN2007 .K93 2002/2003:NO.3/1";
		assertEquals("PN2007 .K93", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN2007 .K93 2002/2003", CallNumUtils.removeLCVolSuffix(callnum));
	
		callnum = "PN2007 .Z37 1993:JAN.-DEC";
		assertEquals("PN2007 .Z37", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN2007 .Z37 1993", CallNumUtils.removeLCVolSuffix(callnum));
		
		callnum = "PN2007 .Z37 1994:SEP-1995:JUN";
		assertEquals("PN2007 .Z37", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN2007 .Z37 1994", CallNumUtils.removeLCVolSuffix(callnum));
		
		callnum = "PN2007 .K93 2002:NO.1-2";
		assertEquals("PN2007 .K93", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN2007 .K93 2002", CallNumUtils.removeLCVolSuffix(callnum));	
	}


	/**
	 * test that LC lopping doesn't go after the class number when it looks 
	 *  like a year -- suffix after second cutter
	 */
	@Test
	public void testLCNoLopClassLikeYearTwoCuttersSuffix() 
	{
		String callnum;

		// volume designation first
		callnum = "PN1993.5 .A35 A373 VOL.4";
		assertEquals("PN1993.5 .A35 A373", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN1993.5 .A35 A373", CallNumUtils.removeLCVolSuffix(callnum));
	
		callnum = "PN1993.5 .A1 S5595 V.2 2008";
		assertEquals("PN1993.5 .A1 S5595", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN1993.5 .A1 S5595", CallNumUtils.removeLCVolSuffix(callnum));
	
		callnum = "PN1993.5 .A75 C564 V.1:NO.1-4 2005";
		assertEquals("PN1993.5 .A75 C564", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN1993.5 .A75 C564", CallNumUtils.removeLCVolSuffix(callnum));
	
		callnum = "PN1993.5 .L3 S78 V.1-2 2004-2005";
		assertEquals("PN1993.5 .L3 S78", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN1993.5 .L3 S78", CallNumUtils.removeLCVolSuffix(callnum));
	
		// year first
		callnum = "PN1993.5 .F7 A3 2006:NO.297-300";
		assertEquals("PN1993.5 .F7 A3", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN1993.5 .F7 A3 2006", CallNumUtils.removeLCVolSuffix(callnum));
	
		callnum = "JQ1519 .A5 A369 1990:NO.1-9+SUPPL.";
		assertEquals("JQ1519 .A5 A369", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("JQ1519 .A5 A369 1990", CallNumUtils.removeLCVolSuffix(callnum));
	
		callnum = "PN1993.5 .F7 A3 2005-2006 SUPPL.NO.27-30";
		assertEquals("PN1993.5 .F7 A3", CallNumUtils.removeLCSerialVolSuffix(callnum));
// TODO: suboptimal -  it finds V.31 first, so it doesn't strip suppl.
		assertEquals("PN1993.5 .F7 A3 2005-2006 SUPPL", CallNumUtils.removeLCVolSuffix(callnum));
	
		callnum = "PN1993.5 .S6 S374 F 2001:JUL.-NOV.";
		assertEquals("PN1993.5 .S6 S374 F", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("PN1993.5 .S6 S374 F 2001", CallNumUtils.removeLCVolSuffix(callnum));
	}

}
