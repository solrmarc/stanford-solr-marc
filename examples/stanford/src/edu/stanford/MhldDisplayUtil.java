package edu.stanford;

import java.util.*;

import org.apache.log4j.Logger;
import org.marc4j.marc.*;
import org.solrmarc.tools.MarcUtils;

/**
 * Use this class to get mhld_display field values
 *  
 * @author Naomi Dushay
 */
public class MhldDisplayUtil
{
	/** separator used in mhld_display field */
	public static final String SEP = " -|- ";
	
    public static Logger logger = Logger.getLogger(MhldDisplayUtil.class.getName());
    
    private Record record = null;
    private String id = null;
    
    /** true when the previous field read was an 852 */
    private boolean justGot852 = false;
    /** if an 852 has subfield '=', then 86x fields with ind2=0 are ignored */
    private boolean df852hasEqualsSubfield = false;
    /** the part of a result string derived from the 852 */
    private String resultPrefixFrom852 = "";

    private boolean haveOpenHoldings = false;
    private boolean have866for852 = false;
    /** used for error detection */
    private boolean haveIgnored866for852 = false; 
    /** used for error reporting */
    private boolean wroteMult866errMsg = false;

    /** helps track when to capture a result string */
    private boolean have867for852 = false;
    /** used for error detection */
    private boolean haveIgnored867for852 = false; 
    /** used for error reporting */
    private boolean wroteMult867errMsg = false;

    /** helps track when to capture a result string */
    private boolean have868for852 = false;
    /** used for error detection */
    private boolean haveIgnored868for852 = false;
    /** used for error reporting */
    private boolean wroteMult868errMsg = false;

	/**
	 * for each 852, we need to have all the patterns in the 853 fields
	 *  available so we can turn the correct 863 field into a sensible "Latest Received" string.
	 *   key:  linkage number from 853 sub 8 
	 *   value:  853 DataField object
	 */
	private Map<Integer, DataField> patternFieldMap = new HashMap<Integer, DataField>();
    
	/** if there is a "Latest Received" portion to the mhld_display value,
	 * it comes from the most recent 863 for the mhld record.  */
	private DataField mostRecent863 = null;
	/** the link number from sub 8  of the most recent 863 
	 *    (the most recent 863 will have the highest link number and 
	 *    the highest seq number of any 863 for the given mhld) */
	private int mostRecent863linkNum = 0;
	/** the sequence number from sub 8  of the most recent 863 
	 *    (the most recent 863 will have the highest link number and 
	 *    the highest seq number of any 863 for the given mhld) */
	private int mostRecent863seqNum = 0;
	
	String resultStr = "";

	/** (ordered) set of mhld_display field values to be returned by getMhldDislayValues() */
	Set<String> result = new LinkedHashSet<String>();

	/**
	 * @param id - used for easier error reporting
	 */	
	public MhldDisplayUtil(Record record, String id)
	{ 
		this.record = record;
		this.id = id;
	}

	
	/**
	 * return the set of mhld_display values based on mhld fields 
	 *   (852, 853, 863, 866, 867, 868 ...)
	 * @return Set of strings in format:
	 *   library + SEP + 
	 *   location + SEP + 
	 *   comment + SEP + 
	 *   library has + SEP +
	 *   latest received  
	 */
	Set<String> getMhldDisplayValues() 
	{
		result = new LinkedHashSet<String>();
		
		// note:  have to read fields sequentially to associate mhld 8xx fields with preceding 852.
		List<DataField> allDataFieldsList = record.getDataFields();
		for (DataField df : allDataFieldsList)
		{
			if (df.getTag().equals("852"))
				process852(df);
			
			// 853 gives pattern for 863  for "Latest Received"
			else if (df.getTag().equals("853"))
				addFieldToPatternFieldsMap(df);
			else if (df.getTag().equals("863"))
				setMostRecent863(df);
				
			// 86x fields for "Library Has"
			else if (df.getTag().equals("866"))
				process86x(df, "866");
			else if (df.getTag().equals("867"))
				process86x(df, "867");
			else if (df.getTag().equals("868"))
				process86x(df, "868");
			
		} // end looping through fields

		addValueToResult();
	
		return result;
	}
	
	private void addValueToResult()
	{
		if (justGot852)
			result.add(resultPrefixFrom852 + SEP);
		else if (resultStr.length() > 0)
			result.add(resultStr + getLatestReceivedStr());
	}
	
	/**
	 * given an 852 field, process it, changing class variables as appropriate
	 *  if the 852 is not skipped, sets resultPrefixFrom852, a portion of a 
	 *  result string.
	 */
	private void process852(DataField df852)
	{
		// if there were no intervening fields between the previous 852
		//   and this one, then output the previous 852 information
		addValueToResult();
		
		resetVarsForNew852();
		
		String comment = "";
		String sub3 = MarcUtils.getSubfieldData(df852, '3');
		if (sub3 != null && sub3.length() > 0)
			comment = sub3;
		String subz = MarcUtils.getSubfieldData(df852, 'z');
		if (subz != null && subz.length() > 0)
		{
			// skip mhld if 852z has "All holdings transferred"
			if (subz.toLowerCase().contains("all holdings transferred"))
				return;
			else
			{
				if (comment.length() > 0)
					comment = comment + " " + subz;
				else
					comment = subz;
			}
		}

		String libraryCode = MarcUtils.getSubfieldData(df852, 'b');
		String locationCode = MarcUtils.getSubfieldData(df852, 'c');
		
		resultPrefixFrom852 = libraryCode + SEP + locationCode + SEP + comment + SEP;				
		
		String subEquals = MarcUtils.getSubfieldData(df852, '=');
		if (subEquals != null && subEquals.length() > 0)
			df852hasEqualsSubfield = true;

		justGot852 = true;
	}
	
	
	/**
	 * reset class variables for a new 852 field 
	 */
	private void resetVarsForNew852()
	{
		resultStr = "";

		// from 852
		justGot852 = false;
		df852hasEqualsSubfield = false;
		resultPrefixFrom852 = "";

		// from 853
		patternFieldMap.clear();
		// from 863
		mostRecent863 = null;
		mostRecent863linkNum = 0;
		mostRecent863seqNum = 0;
		
		// from 866
		have866for852 = false;
		haveOpenHoldings = false;

		// for reporting 86x errors
		haveIgnored866for852 = false;
		haveIgnored867for852 = false;
		haveIgnored868for852 = false;
		wroteMult866errMsg = false;
		wroteMult867errMsg = false;
		wroteMult868errMsg = false;
	}
		
	/**
	 * for each 852, we need to have all the patterns in the 853 fields
	 *  available so we can turn the 863 field into a sensible "Latest Received" string.
	 * @param df853 - an 853 field as a DataField object
	 */
	private void addFieldToPatternFieldsMap(DataField df853)
	{
		String linkSeqNum = MarcUtils.getSubfieldTrimmed(df853, '8');
		try
		{
			patternFieldMap.put(Integer.valueOf(linkSeqNum), df853);
		}
		catch (NumberFormatException e)
		{
			logger.error(id + " has mhld 853 with a non-integer value in sub 8: " + linkSeqNum);
			return;
		}
		justGot852 = false;
	}
	
	/**
	 * we need mostRecent863 to be the 863 field with the highest link and
	 *  sequence number.
	 * this method can assign:
	 *   mostRecent863linkNum
	 *   mostRecent863seqNum
	 *   mostRecent863
	 * @param df863
	 */
	private void setMostRecent863(DataField df863)
	{
		String sub8 = MarcUtils.getSubfieldTrimmed(df863, '8');
		int periodPos = sub8.indexOf('.');
		if (periodPos == -1)
		{
			logger.error(id + " has mhld 863 without a period in sub 8: " + sub8);
			return;
		}
		String dfLinkNumStr = sub8.substring(0, periodPos);
		String dfSeqNumStr = sub8.substring(periodPos + 1);
		int dfLinkNum;
		int dfSeqNum;
		try
		{
			dfLinkNum = Integer.valueOf(dfLinkNumStr);
			dfSeqNum = Integer.valueOf(dfSeqNumStr);
		}
		catch (NumberFormatException e)
		{
			logger.error(id + " has mhld 863 with a non-integer value for link or sequence number: " + sub8);
			return;
		}

		// the df is more recent if the link number is greater, or if the link
		//  number is the same and the sequence number is greater
		if ((mostRecent863linkNum < dfLinkNum) ||
		    (mostRecent863linkNum == dfLinkNum && mostRecent863seqNum < dfSeqNum))
		{
			mostRecent863linkNum = dfLinkNum;
			mostRecent863seqNum = dfSeqNum;
			mostRecent863 = df863;
		}
		justGot852 = false;
	}

	
	/**
	 * given an 86x field, process it, assigning class variables as appropriate
	 * @param df86x - the DataField
	 * @param tag - a string for the tag;  either  866, 867 or 868
	 */
	private void process86x(DataField df86x, String tag)
	{
		// if we have a previous 86x, then keep the resultStr from the previous 86x
		if (resultStr.length() > 0 && (have866for852 || have867for852 || have868for852))
			result.add(resultStr);
		resultStr = "";

		// should we skip this 86x?
		char ind2 = df86x.getIndicator2();
		if (ind2 == '0' && df852hasEqualsSubfield)
		{
			// we skip this one ... but we may need to write error message
			if (tag.equals("866"))
			{
				if (!haveIgnored866for852)
					haveIgnored866for852 = true;
				else if (!wroteMult866errMsg)
				{
					logger.error("Record " + id + " has multiple 866 with ind2=0 and an 852 sub=");
					wroteMult866errMsg = true;
				}
			}
			else if (tag.equals("867"))
			{
				if (!haveIgnored867for852)
					haveIgnored867for852 = true;
				else if (!wroteMult867errMsg)
				{
					logger.error("Record " + id + " has multiple 867 with ind2=0 and an 852 sub=");
					wroteMult867errMsg = true;
				}
				
			}
			else if (tag.equals("868"))
			{
				if (!haveIgnored868for852)
					haveIgnored868for852 = true;
				else if (!wroteMult868errMsg)
				{
					logger.error("Record " + id + " has multiple 868 with ind2=0 and an 852 sub=");
					wroteMult868errMsg = true;
				}
			}
			return;
		}
		else
		{
			// set up result string for this one
			String suba = MarcUtils.getSubfieldData(df86x, 'a');
			if (suba == null)
				suba = "";
			
			String prefix = "";
			if (tag.equals("866") && !have866for852)
			{
				have866for852 = true;
				if (suba.endsWith("-"))
					haveOpenHoldings = true;
			}
			else if (tag.equals("867"))
			{
				prefix = "Supplement: ";
				if (!have867for852)
					have867for852 = true;
			}
			else if (tag.equals("868"))
			{
				prefix = "Index: ";
				if (!have868for852)
					have868for852 = true;
			}
			
			resultStr = resultPrefixFrom852 + prefix + suba + SEP ;
		}
		justGot852 = false;	
	}

	/**
	 * @return a string for "Latest Received" based on mostRecent863 and the
	 *   matching pattern field retrieved from the patternFieldMap
	 */
	private String getLatestReceivedStr()
	{
		String result = "";
		if (haveOpenHoldings)
		{
			if (mostRecent863 != null && mostRecent863linkNum != 0)
			{
				DataField pattern853df = patternFieldMap.get(Integer.valueOf(mostRecent863linkNum));
				result = expandWithCaptions(mostRecent863, pattern853df);
			}
		}

System.out.println("DEBUG: " + id + " has latest received: " + result);		
		return result;
	}
	
	
	/**
	 * MHLD records put the pattern of the enumeration in an 853, and the values
	 *  for each issue received into the 863.  To get a user friendly string,
	 *  the captions from the 853 must be applied to the values in the 863.
	 * NOTE:  the match between the 853 and 863 linkage numbers should be done
	 *  before calling this method.
	 *  
	 *  @author Bob Haschart, with some revisions by Naomi Dushay
	 *  
	 * @param df863 - the 863 DataField object to be transformed
	 * @param pattern853df - the 853 DataField containing the pattern for the
	 *  863 field.
	 * @return a user friendly string representation of the information in the 
	 *   863 field.
	 */
    private String expandWithCaptions(DataField df863, DataField pattern853df)
    {
        StringBuffer result = new StringBuffer();

        if (pattern853df == null) 
        	return null;

        // get the enumeration information (with captions) from subfields a-f
        for (char code = 'a'; code <= 'f'; code++)
        {
        	String label = MarcUtils.getSubfieldData(pattern853df, code);
            String data = MarcUtils.getSubfieldData(df863, code);
            if (label == null || data == null) 
            	break;
            if (code != 'a')  
            	result.append(", ");
            // leave out any label with parens.
            if (label.startsWith("(") && label.endsWith(")")) 
            	label = "";
            result.append(label + data);
        }
        
        
        // get alternate enumeration information (with captions) from subfields g and h
        //   if it's not empty, append to the end within parens.
        StringBuffer alt = new StringBuffer();
        for (char code = 'g'; code <= 'h'; code++)
        {
            String label = MarcUtils.getSubfieldData(pattern853df, code);
            String data = MarcUtils.getSubfieldData(df863, code);
            if (label == null || data == null) 
            	break;
            if (code != 'g')  
            	alt.append(", ");
            alt.append(label + data);
        }
        if (alt.length() != 0)
            result.append(" (" + alt + ")");

        
        // get the date (chronology information) from subfields i-m
        StringBuffer dateStr = new StringBuffer();
        boolean prependStr = false;
        String strToPrepend = "";
        for (char code = 'i'; code <= 'm'; code++)
        {
            String label = MarcUtils.getSubfieldData(pattern853df, code);
            String data = MarcUtils.getSubfieldData(df863, code);
            if (label == null || data == null) 
            	break;
            if (label.equalsIgnoreCase("(month)") || label.equalsIgnoreCase("(season)"))
            {
                data = expandMonthOrSeason(data);
                strToPrepend = ":";
            }
            else if (label.equalsIgnoreCase("(day)"))
            {
                data = expandMonthOrSeason(data);
                strToPrepend = " ";
            }
            if (prependStr)
                dateStr.append(strToPrepend).append(data);
            else
                dateStr.append(data);

            prependStr = true;
        }
        if (dateStr.length() > 0)
        {
            if (result.length() > 0)  
            	result.append(" (").append(dateStr).append(")");
            else 
            	result.append(dateStr);
        }    
        
        return result.toString();
    }


    private String expandMonthOrSeason(String data)
    {
        data = data.replaceAll("01", "Jan");
        data = data.replaceAll("02", "Feb");
        data = data.replaceAll("03", "Mar");
        data = data.replaceAll("04", "Apr");
        data = data.replaceAll("05", "May");
        data = data.replaceAll("06", "Jun");
        data = data.replaceAll("07", "Jul");
        data = data.replaceAll("08", "Aug");
        data = data.replaceAll("09", "Sept");
        data = data.replaceAll("10", "Oct");
        data = data.replaceAll("11", "Nov");
        data = data.replaceAll("12", "Dec");
        data = data.replaceAll("21", "Spring");
        data = data.replaceAll("22", "Summer");
        data = data.replaceAll("23", "Autumn");
        data = data.replaceAll("24", "Winter");
        return(data);
    }
	
}
