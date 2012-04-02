package org.solrmarc.tools;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.marc4j.*;
import org.marc4j.marc.Record;
import org.solrmarc.marc.SolrReIndexer;

/**
 * Methods used when reading strings to be parsed as Marc4j Record objects
 * @author Naomi Dushay
 */
public class MarcReadingUtils
{
    // Initialize logging category
    static Logger logger = Logger.getLogger(SolrReIndexer.class.getName());

    /**
     * Create marc4j Record object from MarcXML
     * @param marcxmlStr MarcXML string
     * @return marc4j Record
     */
    public static Record getRecordFromXMLString(String marcxmlStr, boolean writeToErr)
    {
        MarcXmlReader reader;
        boolean tryAgain = false;
        boolean errFileStarted = false;
    	BufferedWriter errOut = null;
        do
        {
            try
            {
                tryAgain = false;
                reader = new MarcXmlReader(new ByteArrayInputStream(marcxmlStr.getBytes("UTF8")));
                if (reader.hasNext())
                    return (reader.next());
            }
            catch( MarcException me)
            {
            	if (writeToErr && !errFileStarted)
                {
            		// start the error file
                    try
                    {
                        errOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("badRecs.xml"))));
                        errOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\">");
                        errFileStarted = true;
                    }
                    catch (FileNotFoundException e)
                    {
                        // e.printStackTrace();
                        logger.error(e.getMessage());
                    }
                    catch (IOException e)
                    {
                        // e.printStackTrace();
                        logger.error(e.getMessage());
                    }
                }

                if (writeToErr && errFileStarted)
                {
                	// output this record and readjust the closing tags
                    String trimmed = marcxmlStr.substring(marcxmlStr.indexOf("<record>"));
                    trimmed = trimmed.replaceFirst("</collection>", "");
                    trimmed = trimmed.replaceAll("><", ">\n<");
                    try
                    {
                        errOut.write(trimmed);
                    }
                    catch (IOException e)
                    {
                        // e.printStackTrace();
                        logger.error(e.getMessage());
                    }
                }

                if (marcxmlStr.contains("<subfield code=\"&#31;\">"))
                {
                    // rewrite input string and try again.
                    marcxmlStr = marcxmlStr.replaceAll("<subfield code=\"&#31;\">(.)", "<subfield code=\"$1\">");
                    tryAgain = true;
                }
                else if (MarcReadingUtils.extractLeader(marcxmlStr).contains("&#")) //.("<leader>[^<>&]*&#[0-9]+;[^<>&]*</leader>"))
                {
                    // rewrite input string and try again.
                    // 07585nam a2200301 a 4500
                    String leader = MarcReadingUtils.extractLeader(marcxmlStr).replaceAll("&#[0-9]+;", "0");
                    marcxmlStr = marcxmlStr.replaceAll("<leader>[^<]*</leader>", leader);
                    tryAgain = true;
                }
                else
                	// give up
                    me.printStackTrace();
            }
            catch (UnsupportedEncodingException e)
            {
                logger.error(e.getMessage());
            }

        } while (tryAgain);

        return(null);
    }


    /**
     * Create marc4j Record object from Marc as JSON String
     * @param marcJsonStr the marc record as json, as a string
     * @return marc4j Record
     */
    public static Record getRecordFromJSONString(String marcJsonStr, boolean verbose)
    {
        MarcJsonReader reader;
        int tries = 0;
        boolean tryAgain = false;
        do
        {
            try
            {
                tries++;
                tryAgain = false;
                reader = new MarcJsonReader(new ByteArrayInputStream(marcJsonStr.getBytes("UTF8")));
                if (reader.hasNext())
                {
                    Record record = reader.next();
                    if (verbose)
                        System.out.println(record.toString());
                    return(record);
                }
            }
            catch( MarcException me)
            {
//                if (tries == 1)
//                {
//                    tryAgain = true;
//                    marcRecordStr = normalizeUnicode(marcRecordStr);
//                }
//                else
//                    me.printStackTrace();
            	me.printStackTrace();
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }

        } while (tryAgain);

        return(null);
    }


    /**
     * Create marc4j Record object from binary Marc as String
     * @param marc21Str the binary marc record, as a string
     * @return marc4j Record
     */
    public static Record getRecordFromBinaryMarc(String marcRecordStr, boolean verbose)
    {
        MarcStreamReader reader;
        int tries = 0;
        boolean tryAgain = false;
        do {
            try {
                tries++;
                tryAgain = false;
                reader = new MarcStreamReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext())
                {
                    Record record = reader.next();
                    if (verbose)
                        System.out.println(record.toString());
                    return(record);
                }
            }
            catch( MarcException me)
            {
                if (tries == 1)
                {
                    tryAgain = true;
                    marcRecordStr = normalizeUnicode(marcRecordStr);
                }
                else
                    me.printStackTrace();
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        } while (tryAgain);
        return(null);
    }



    /**
     * looks for strings   uXXXX or #29; or #30; or #31; and replaces them with
     *  something parseable.
     */
    private static String normalizeUnicode(String origString)
    {
        Pattern pattern = Pattern.compile("(\\\\u([0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]))|(#(29|30|31);)");
        Matcher matcher = pattern.matcher(origString);

        StringBuffer result = new StringBuffer();
        int prevEnd = 0;
        while (matcher.find())
        {
            result.append(origString.substring(prevEnd, matcher.start()));
            result.append(getChar(matcher.group()));
            prevEnd = matcher.end();
        }
        result.append(origString.substring(prevEnd));

        return (result.toString());
    }

    /**
     * convert uXXXX or #29; or #30; or #31; to the appropriate character for
     *  marc4j parsing
     */
    private static String getChar(String charCodePointStr)
    {
        int charNum;
        if (charCodePointStr.startsWith("\\u"))
            charNum = Integer.parseInt(charCodePointStr.substring(1), 16);
        else
            charNum = Integer.parseInt(charCodePointStr.substring(1, 3));

        return("" + ((char) charNum));
    }


    /**
     * Extract the leader from the marc xml
     * @param marcxmlAsStr marc xml record as a String
     * @return Leader String containing leader, including opening and closing tags
     */
    public static String extractLeader(String marcxmlAsStr)
    {
        final String leadertag1 = "<leader>";
        final String leadertag2 = "</leader>";
        String leader = null;
        try
        {
            leader = marcxmlAsStr.substring(marcxmlAsStr.indexOf(leadertag1), marcxmlAsStr.indexOf(leadertag2)+leadertag2.length() );
        }
        catch (IndexOutOfBoundsException e)
        {}

        return leader;
    }


}
