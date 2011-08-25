package edu.stanford;

import java.util.*;

import org.apache.log4j.Logger;
import org.marc4j.marc.*;
import org.solrmarc.tools.MarcUtils;

/**
 * Utility methods for item information Stanford SolrMarc
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
    private boolean justGot852 = false;
    private boolean df852hasEqualsSubfield = false;
    private String resultPrefixFrom852 = "";

    private boolean haveOpenHoldings = false;
    private boolean have866for852 = false;
    private boolean haveIgnored866for852 = false;  // used for reporting errors
    private boolean wroteMult866errMsg = false;

    private boolean have867for852 = false;
    private boolean haveIgnored867for852 = false;  // used for reporting errors
    private boolean wroteMult867errMsg = false;

    private boolean have868for852 = false;
    private boolean haveIgnored868for852 = false;  // used for reporting errors
    private boolean wroteMult868errMsg = false;

	
	String resultStr = "";

	/** (ordered) set of mhld_display field values to be returned by getMhldDislayValues() */
	Set<String> result = new LinkedHashSet<String>();

	/**
	 * Default Constructor: private, so it can't be instantiated by other objects
	 */	
	public MhldDisplayUtil(Record record, String id)
	{ 
		this.record = record;
		this.id = id;
	}

	
	/**
	 * given a marc Record object containing (sets of) mhld fields, return a set of mhld_display values
	 * @return set of fields from mhlds:
	 *   library + SEP + 
	 *   location + SEP + 
	 *   comment + SEP + 
	 *   ""|Index|Supplement + SEP + 
	 *   latest received + SEP + 
	 *   library has 
	 */
	Set<String> getMhldDisplayValues() 
	{
		result = new LinkedHashSet<String>();
		
		// note:  have to read fields sequentially to associate mhld 8xx fields with preceding 852.
		List<DataField> allDataFieldsList = record.getDataFields();
		for (DataField df : allDataFieldsList)
		{
			if (df.getTag().equals("852"))
			{
				// if there were no intervening fields between the previous 852
				//   and this one, then output the previous 852 information
				if (justGot852)
					result.add(resultPrefixFrom852 + SEP);
				else if (resultStr.length() > 0)
					result.add(resultStr);
				
				init852Vars();
				
				String comment = "";
				String subz = MarcUtils.getSubfieldData(df, 'z');
				if (subz != null)
				{
					// skip mhld if 852z has "All holdings transferred"
					if (subz.toLowerCase().contains("all holdings transferred"))
						continue;
					else
						comment = subz;
				}
				// finish comment value
				String sub3 = MarcUtils.getSubfieldData(df, '3');
				if (sub3 != null && sub3.length() > 0)
					comment = comment + " " + sub3;
				String libraryCode = MarcUtils.getSubfieldData(df, 'b');
				String locationCode = MarcUtils.getSubfieldData(df, 'c');
				
				resultPrefixFrom852 = libraryCode + SEP + locationCode + SEP + comment + SEP;				
				
				String subEquals = MarcUtils.getSubfieldData(df, '=');
				if (subEquals != null && subEquals.length() > 0)
					df852hasEqualsSubfield = true;

				justGot852 = true;

			} // end 852 field
			
			else if (df.getTag().equals("866"))
				process86x(df, "866");
			else if (df.getTag().equals("867"))
				process86x(df, "867");
			else if (df.getTag().equals("868"))
				process86x(df, "868");
			
		} // end looping through fields

// FIXME:  need test for outputting final 852		
		if (justGot852)
			result.add(resultPrefixFrom852 + SEP + SEP);
		else if (resultStr.length() > 0)
			result.add(resultStr);
	
		return result;
	}
	
	
	void init852Vars()
	{
		justGot852 = false;
		df852hasEqualsSubfield = false;
		haveIgnored866for852 = false;
		wroteMult866errMsg = false;
		have866for852 = false;
		haveOpenHoldings = false;
		resultPrefixFrom852 = "";
		resultStr = "";
	}
	
	
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
			
			String type = "";
			if (tag.equals("866") && !have866for852)
				have866for852 = true;
			else if (tag.equals("867"))
			{
				type = "Supplement";
				if (!have867for852)
					have867for852 = true;
			}
			else if (tag.equals("868"))
			{
				type = "Index";
				if (!have868for852)
					have868for852 = true;
			}
			
			resultStr = resultPrefixFrom852 + type + SEP + SEP + suba;
		}
		justGot852 = false;	
	}

	
	/**
	 * 
	 * @param df852 - an 852 field from 
	 * @return
	 */
	static boolean skip852(DataField df852)
	{
		// skip mhld if 852z has "All holdings transferred"
		String subz = MarcUtils.getSubfieldData(df852, 'z');
		if (subz != null && subz.toLowerCase().contains("all holdings transferred"))
			return true;
		else
			return false;
	}
	
	
/*	
    public Set<String> getSummaryHoldingsInfo(Record record, String libraryMapName, String locationMapName)
    {
        Set<String> result = new LinkedHashSet<String>();
        Set<String> ivyresult = new LinkedHashSet<String>();
        String fieldsToUseStr = "852|853|863|866|867";
        String fieldsToUse[] = fieldsToUseStr.split("[|]");
        String libMapName = loadTranslationMap(null, libraryMapName);
        String locMapName = loadTranslationMap(null, locationMapName);
        List<VariableField> fields = record.getVariableFields();
        DataField libraryField = null;
        for (int i = 0; i < fields.size(); i++)
        {
            String holdingsField;
            VariableField vf = fields.get(i);
            if (!(vf instanceof DataField))  continue;
            DataField df = (DataField)vf;
            if (df.getTag().equals("852"))  
            {
                libraryField = df;
                if (getSubfieldVal(libraryField, 'z', null) != null)
                {
                    holdingsField = buildHoldingsField(libraryField, libMapName, locMapName, "", getSubfieldVal(libraryField, 'z', ""), "");
                    addHoldingsField(result, ivyresult, holdingsField);
                }
            }
            else if (df.getTag().equals("853"))  continue; // ignore 853's here.
            else if (df.getTag().equals("866"))  
            {
                holdingsField = buildHoldingsField(libraryField, libMapName, locMapName, getSubfieldVal(df, 'a', ""), getSubfieldVal(df, 'z', ""), "Library has");
                addHoldingsField(result, ivyresult, holdingsField);
            }
            else if (df.getTag().equals("867"))
            {
                holdingsField = buildHoldingsField(libraryField, libMapName, locMapName, getSubfieldVal(df, "z+a", ""), getSubfieldVal(df, "-z", ""), "Suppl text holdings");
                addHoldingsField(result, ivyresult, holdingsField);
            }
            else if (df.getTag().equals("868"))
            {
                holdingsField = buildHoldingsField(libraryField, libMapName, locMapName, getSubfieldVal(df, 'a', ""), getSubfieldVal(df, 'z', ""), "Index text holdings");
                addHoldingsField(result, ivyresult, holdingsField);
            }
            else if (df.getTag().equals("863"))
            {
                // look ahead for other 863's to combine                
                String linktag = df.getSubfield('8') != null ? df.getSubfield('8').getData() : null;
                int j = i+1;
                for (; j < fields.size(); j++)
                {
                    VariableField nvf = fields.get(j);
                    if (!(nvf instanceof DataField))  break;
                    DataField ndf = (DataField)nvf;
                    String nlinktag = ndf.getSubfield('8') != null ? ndf.getSubfield('8').getData() : null;
                    if (linktag == null || nlinktag == null || !getLinkPrefix(linktag).equals(getLinkPrefix(nlinktag))) 
                        break;                   
                }
                DataField labelField = null;
                if (linktag != null) labelField = getLabelField(record, getLinkPrefix(linktag));
                if (labelField != null && j == i + 1) 
                {
                    holdingsField = buildHoldingsField(libraryField, libMapName, locMapName, processEncodedField(df, labelField), getSubfieldVal(df, 'z', ""), "Library has");
                    addHoldingsField(result, ivyresult, holdingsField);
                }
                else if (labelField != null && j > i + 1) 
                {
                    VariableField nvf = fields.get(j-1);
                    DataField ndf = (DataField)nvf;
                    holdingsField = buildHoldingsField(libraryField, libMapName, locMapName, processEncodedFieldRange(df, ndf, labelField), getSubfieldVal(df, 'z', ""), "Library has");
                    addHoldingsField(result, ivyresult, holdingsField);
                    i = j - 1;
                }
            }
        }
        if (ivyresult.size() != 0)
        {
            for (String ivy : ivyresult)
            {
                result.add(ivy);
            }
        }
        return(result);
    }

    private void addHoldingsField(Set<String> result, Set<String> ivyresult, String holdingsField)
    {
        if (holdingsField != null)
        {
            if (holdingsField.startsWith("Ivy"))
                ivyresult.add(holdingsField);
            else
                result.add(holdingsField);
        }
    }

    private String buildHoldingsField(DataField libraryField, String libMapName, String locMapName, String holdingsValue, String publicNote, String holdingsType)
    {
        if (libraryField == null || ((holdingsValue == null || holdingsValue.length() == 0) && (publicNote.length() == 0 ))) 
        	return(null);
        String libraryName = libraryField.getSubfield('b') != null ? Utils.remap(libraryField.getSubfield('b').getData(), findMap(libMapName), false) : null;
        String locName = libraryField.getSubfield('c') != null ? Utils.remap(libraryField.getSubfield('c').getData(), findMap(locMapName), false) : null;
        return(libraryName +"|"+ locName +"|"+ holdingsValue+"|"+publicNote+"|"+holdingsType);
    }
/*
    private String getSubfieldVal(DataField df, String subfieldTags, String defValue)
    {
        List<Subfield> subfields = (List<Subfield>)df.getSubfields();
        if (subfields.size() == 0)  return(defValue);
        String result = "";
        boolean found_a = false;
        boolean getBefore_a = subfieldTags.contains("+");
        for (Subfield sf : subfields)
        {
            if (sf.getCode() == 'a')
            {
                if (subfieldTags.contains(""+sf.getCode()))
                {
                    result = result + ((result.length() > 0) ? " " : "") + sf.getData();
                }
                found_a = true;
            }
            else if (getBefore_a && !found_a && sf.getCode() != 'a' && subfieldTags.contains(""+sf.getCode()) ) 
            {
                result = result + ((result.length() > 0) ? " " : "") + sf.getData();
            }
            else if (!getBefore_a && found_a && sf.getCode() != 'a' && subfieldTags.contains(""+sf.getCode()) )
            {
                result = result + ((result.length() > 0) ? " " : "") + sf.getData();
            }
        }
        return result;
    }
    
    private String getSubfieldVal(DataField df, char subfieldTag, String defValue)
    {
        List<Subfield> subfields = (List<Subfield>)df.getSubfields(subfieldTag);
        if (subfields.size() == 0)  return(defValue);
        String result = "";
        for (Subfield sf : subfields)
        {
            result = result + sf.getData();
        }
        return result;
    }
 
    private String processEncodedField(DataField df, DataField labelField)
    {
        boolean normalize_date = false;
        if (labelField == null) return(null);
        StringBuffer result = new StringBuffer();
        for (char subfield = 'a'; subfield <= 'f'; subfield++)
        {
            String label = getSubfieldVal(labelField, subfield, null);
            String data = getSubfieldVal(df, subfield, null);
            if (label == null || data == null) break;
            if (subfield != 'a')  result.append(", ");
            if (label.startsWith("(") && label.endsWith(")")) label = "";
            result.append(label);
            result.append(data);
        }
        StringBuffer alt = new StringBuffer();
        for (char subfield = 'g'; subfield <= 'h'; subfield++)
        {
            String label = getSubfieldVal(labelField, subfield, null);
            String data = getSubfieldVal(df, subfield, null);
            if (label == null || data == null) break;
            if (subfield != 'g')  alt.append(", ");
            alt.append(label);
            alt.append(data);
        }
        if (alt.length() != 0)
        {
            result.append(" ("+alt+")");
        }
        String year = null;
        StringBuffer date = new StringBuffer();
        if (normalize_date)
        {
            for (char subfield = 'i'; subfield <= 'm'; subfield++)
            {
                boolean appendComma = false;
                String label = getSubfieldVal(labelField, subfield, null);
                String data = getSubfieldVal(df, subfield, null);
                if (label == null || data == null) break;
            //    if (subfield != 'i')  result.append(", ");
                if (label.equalsIgnoreCase("(month)") || label.equalsIgnoreCase("(season)"))
                {
                    data = expandMonthOrSeason(data);
                }
                else if (year != null && !label.equalsIgnoreCase("(day)"))
                {
                    date.append(year);
                    year = null;
                }
                else
                {
                    appendComma = true;
                }
                if (label.equalsIgnoreCase("(year)"))
                {
                    year = data;
                }
                else if (label.equalsIgnoreCase("(day)"))
                {
                    date.append(" ").append(data);
                    if (appendComma) date.append(", ");
                }
                else
                {
                    date.append(data);
                    if (appendComma) date.append(", ");
                }
            }
            if (year != null) date.append(year);
        }
        else
        {
            boolean prependStr = false;
            String strToPrepend = "";
            for (char subfield = 'i'; subfield <= 'm'; subfield++)
            {
                String label = getSubfieldVal(labelField, subfield, null);
                String data = getSubfieldVal(df, subfield, null);
                if (label == null || data == null) break;
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
                {
                    date.append(strToPrepend).append(data);
                }
                else
                {
                    date.append(data);
                }
                prependStr = true;
            }
        }
        if (date.length() > 0)
        {
            if (result.length() > 0)  result.append(" (").append(date).append(")");
            else result.append(date);
        }    
        return result.toString();
    }
    
    private String processEncodedFieldRange(DataField df1, DataField df2, DataField labelField)
    {
        boolean normalize_date = false;
        if (labelField == null) return(null);
        StringBuffer result = new StringBuffer();
        StringBuffer vol1 = new StringBuffer();
        StringBuffer vol2 = new StringBuffer();
        for (char subfield = 'a'; subfield <= 'f'; subfield++)
        {
            String label = getSubfieldVal(labelField, subfield, null);
            String data1 = getSubfieldVal(df1, subfield, null);
            String data2 = getSubfieldVal(df2, subfield, null);
            if (label == null || data1 == null || data2 == null) break;
            if (subfield != 'a')  
            {
                vol1.append(", ");
                vol2.append(", ");
            }
            if (label.startsWith("(") && label.endsWith(")")) label = "";
            vol1.append(label);
            vol1.append(data1);
            vol2.append(label);
            vol2.append(data2);
        }
        result.append(rangify(vol1.toString(), vol2.toString()));
        StringBuffer alt = new StringBuffer();
        for (char subfield = 'g'; subfield <= 'h'; subfield++)
        {
            String label = getSubfieldVal(labelField, subfield, null);
            String data1 = getSubfieldVal(df1, subfield, null);
            String data2 = getSubfieldVal(df2, subfield, null);
            if (label == null || data1 == null || data2 == null) break;
            if (subfield != 'g')  alt.append(", ");
            alt.append(label);
            alt.append(rangify(data1, data2));
        }
        if (alt.length() != 0)
        {
            result.append(" ("+alt+")");
        }
        StringBuffer date1 = new StringBuffer();
        StringBuffer date2 = new StringBuffer();
        {
            boolean prependStr = false;
            String strToPrepend = "";
            for (char subfield = 'i'; subfield <= 'm'; subfield++)
            {
                String label = getSubfieldVal(labelField, subfield, null);
                String data1 = getSubfieldVal(df1, subfield, null);
                String data2 = getSubfieldVal(df2, subfield, null);
                if (label == null || data1 == null || data2 == null) break;
                if (label.equalsIgnoreCase("(month)") || label.equalsIgnoreCase("(season)"))
                {
                    data1 = expandMonthOrSeason(data1);
                    data2 = expandMonthOrSeason(data2);
                    strToPrepend = ":";
                }
                else if (label.equalsIgnoreCase("(day)"))
                {
                    strToPrepend = " ";
                }
                if (prependStr)
                {
                    date1.append(strToPrepend).append(data1);
                    date2.append(strToPrepend).append(data2);
                }
                else
                {
                    date1.append(data1);
                    date2.append(data2);
                }
                prependStr = true;
            }
        }
        if (date1.length() > 0 && date2.length() > 0)
        {
            if (result.length() > 0)  result.append(" (").append(rangify(date1.toString(), date2.toString())).append(")");
            else result.append(rangify(date1.toString(), date2.toString()));
        }    
        return result.toString();
    }

    private Object rangify(String data1, String data2)
    {
        int i;
        if (data1.equals(data2)) return(data1);
        for (i = 0; i < data1.length() && i < data2.length(); i++)
        {
            if (data1.charAt(i) != data2.charAt(i)) break;
        }
        int preBackstep = i;
        if ( i < data1.length() && i < data2.length() && Character.isDigit(data1.charAt(i)) && Character.isDigit(data2.charAt(i)))
        {
            while (Character.isDigit(data1.charAt(i)) && Character.isDigit(data2.charAt(i)) &&
                i > 0 && Character.isDigit(data1.charAt(i-1)) && Character.isDigit(data2.charAt(i-1)))
            {
                i--;
            }
        }
        else if ( i < data1.length() && i < data2.length() && Character.isLetter(data1.charAt(i)) && Character.isLetter(data2.charAt(i)))
        {
            while (Character.isLetter(data1.charAt(i)) && Character.isLetter(data2.charAt(i)) &&
                i > 0 && Character.isLetter(data1.charAt(i-1)) && Character.isLetter(data2.charAt(i-1)))
            {
                i--;
            }
        }
        String result;
        if (i <= 3 && data1.length() > 6  && data2.length() > 6 && preBackstep < 6)
            result = data1 + "-" + data2;
        else if ( i < data1.length() && i < data2.length())
            result = data1.substring(0, i) + data1.substring(i) + "-" + data2.substring(i);
        else 
            result = data1;
        return result;
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

    private DataField getLabelField(Record record, String linkPrefix)
    {
        if (linkPrefix == null) return(null);
        List<VariableField> fields = (List<VariableField>)record.getVariableFields("853");
        for (VariableField vf : fields)
        {
            if (!(vf instanceof DataField))  continue;
            DataField df = (DataField)vf;
            String link = df.getSubfield('8') != null ? df.getSubfield('8').getData() : null;
            if (link != null && link.equals(linkPrefix))
            {
                return(df);
            }
        }
        return(null);
    }

    private String getLinkPrefix(String linktag)
    {
        String prefix = null;
        int index;
        if ((index = linktag.indexOf('.')) == -1) 
            prefix = linktag;
        else 
            prefix = linktag.substring(0, index);
        return(prefix);
    }
*/
}
