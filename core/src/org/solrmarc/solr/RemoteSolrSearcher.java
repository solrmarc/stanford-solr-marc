package org.solrmarc.solr;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.*;
import org.marc4j.marc.Record;

public class RemoteSolrSearcher
{
    static boolean verbose = false;
    static boolean veryverbose = false;
    String solrBaseURL;
    String query;
    /** solr field containing encoding marc record */
    String marcRecFld;
    /** name of Solr field containing ids */
    String id_fname;
    /** a reqHandler with deftype lucene (for id matching) */
    String defaultReqHandler;

    public RemoteSolrSearcher(String solrBaseURL, String query, String marcRecFld)
    {
    	this(solrBaseURL, query, marcRecFld, "id", "standard");
    }

    public RemoteSolrSearcher(String solrBaseURL, String query, String marcRecFld, String id_fname, String defaultReqHandler)
    {
        this.solrBaseURL = solrBaseURL;
        this.marcRecFld = marcRecFld;
        this.id_fname = id_fname;
        this.defaultReqHandler = defaultReqHandler;
        this.query = query;
        if (verbose) System.err.println("URL = "+ solrBaseURL + "  query = "+ query);
    }

    public int handleAll()
    {
    	MarcStreamWriter output = new MarcStreamWriter(System.out, "UTF8", true);
        if (marcRecFld == null) marcRecFld = "marc_display";

        String encQuery;
        try
        {
            encQuery = java.net.URLEncoder.encode(query, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            encQuery = query;
        }

        if (verbose) System.err.println("encoded query = "+ encQuery);

        String resultSet[] = getIdSet(encQuery);
        String recordStr = null;
        for (String id : resultSet)
        {
            recordStr = getFieldFromDocumentGivenDocID(id, marcRecFld);
            Record record = null;
            if (recordStr.startsWith("<?xml version"))
                record = getRecordFromXMLString(recordStr);
            else if (recordStr.startsWith("{\""))
                record = getRecordFromJsonString(recordStr);
            else
                record = getRecordFromRawMarc(recordStr);

            if (record != null)
            {
                output.write(record);
                System.out.flush();
            }
        }
        output.close();
        return 0;
    }

    private String getFieldFromDocumentGivenDocID(String id, String solrFieldContainingEncodedMarcRecord2)
    {
    	return getFieldFromDocumentGivenDocID(id, solrFieldContainingEncodedMarcRecord2, defaultReqHandler);
    }

    /**
     *
     * @param id the id of the record sought
     * @param marcRecFld Solr field containing the encoded marc record
     * @param reqHandler name of Solr request handler with deftype lucene
     * @return
     */
    private String getFieldFromDocumentGivenDocID(String id, String marcRecFld, String reqHandler)
    {
        String fullURLStr = solrBaseURL + "/select/?q=" + id_fname + "%3A"+ id + "&qt=" + reqHandler + "&fl="+marcRecFld + "&wt=json&indent=on";
        if (verbose) System.err.println("encoded document retrieval url = "+ fullURLStr);
        URL fullURL = null;
        try
        {
            fullURL = new URL(fullURLStr);
        }
        catch (MalformedURLException e1)
        {
            e1.printStackTrace();
        }

        BufferedReader sIn = null;
        try
        {
            sIn = new BufferedReader( new InputStreamReader(fullURL.openStream(), "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String line;
        String result = null;
        try
        {
            while ((line = sIn.readLine()) != null)
            {
                if (line.contains(marcRecFld+"\":"))
                {
                    if (line.contains("\"<?xml version"))
                    {
                        result = line.replaceFirst(".*<\\?xml", "<?xml");
                        result = result.replaceFirst("</collection>.*", "</collection>");
                        result = result.replaceAll("\\\\\"", "\"");
                    }
                    else if (line.contains(marcRecFld+"\":[\"{"))
                    {
                        result = line.replaceFirst("[^:]*:\\[\"[{]", "{");
                        result = result.replaceFirst("\\\\n\"][}]]", "");
                        result = result.replaceAll("\\\\\"", "\"");
                        result = result.replace("\\\\", "\\");
                    }
                    else
                    {
                        result = line.replaceFirst("[^:]*:\"", "");
                        result = result.replaceFirst("\"}]$", "");
                        result = result.replaceAll("\\\\\"", "\"");
                        result = normalizeUnicode(result);
                    }
                }
                else
                    continue;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return(result);
    }

    private String normalizeUnicode(String origStr)
    {
        Pattern pattern = Pattern.compile("(\\\\u([0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]))|(#(29|30|31);)");
        Matcher matcher = pattern.matcher(origStr);
        StringBuffer result = new StringBuffer();
        int prevEnd = 0;
        while(matcher.find())
        {
            result.append(origStr.substring(prevEnd, matcher.start()));
            result.append(getChar(matcher.group()));
            prevEnd = matcher.end();
        }
        result.append(origStr.substring(prevEnd));
        return(result.toString());
    }

    private String getChar(String charCodePoint)
    {
        int charNum;
        if (charCodePoint.startsWith("\\u"))
            charNum = Integer.parseInt(charCodePoint.substring(2), 16);
        else
            charNum = Integer.parseInt(charCodePoint.substring(1, 3));

        return ("" + ((char)charNum));
    }


    public String[] getIdSet(String query)
    {
    	return getIdSet(query, defaultReqHandler);
    }


    /**
     *
     * @param query
     * @param reqHandler Solr request handler for query
     * @return array of Strings matching the query
     */
    public String[] getIdSet(String query, String reqHandler)
    {
        int setSize = getIdSetSize(query);
        String resultSet[] = new String[setSize];

        String fullURLStr = solrBaseURL + "/select/?q=" + query + "&qt=" + reqHandler + "&fl="+ id_fname + "&rows=" + setSize + "&start=0&wt=json&indent=on";

        if (verbose) System.err.println("Full URL for search = "+ fullURLStr);

        URL fullURL = null;
        try
        {
            fullURL = new URL(fullURLStr);
        }
        catch (MalformedURLException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        BufferedReader sIn = null;
        try
        {
            sIn = new BufferedReader( new InputStreamReader(fullURL.openStream(), "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try
        {
            String line;
            int count = 0;
            while ((line = sIn.readLine()) != null)
            {
                if (line.contains("\"" + id_fname + "\":"))
                {
                    String id = line.replaceFirst(".*:[^\"]?\"([-A-Za-z0-9_]*).*", "$1");
                    if (veryverbose)
                    	System.err.println("record num = "+ (count) +" "+ id_fname +" = " + id);
                    resultSet[count++] = id;
                }
            }
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return(resultSet);
    }


    public int getIdSetSize(String query)
    {
    	return getIdSetSize(query, defaultReqHandler);
    }

    /**
     * @param query
     * @param reqHandler Solr request handler
     * @return number of documents matching the query
     */
    public int getIdSetSize(String query, String reqHandler)
    {
        String fullURLStr = solrBaseURL + "/select/?q=" + query + "&qt=" + reqHandler + "&wt=json&indent=on&start=0&rows=0";
        if (verbose) System.err.println("Full URL for search = "+ fullURLStr);
        URL fullURL = null;
        try
        {
            fullURL = new URL(fullURLStr);
        }
        catch (MalformedURLException e1)
        {
            e1.printStackTrace();
        }

        BufferedReader sIn = null;
        try
        {
            sIn = new BufferedReader( new InputStreamReader(fullURL.openStream(), "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        int numFound = 0;
        try
        {
            String line;
            while ((line = sIn.readLine()) != null)
            {
                if (line.contains("\"numFound\""))
                {
                    String numFoundStr = line.replaceFirst(".*numFound[^0-9]*([0-9]*).*", "$1");
                    numFound = Integer.parseInt(numFoundStr);
                    if (verbose) System.err.println("numFound = "+ numFound);
                }
            }
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return(numFound);
    }


    /**
     * Extract the marc record from binary marc
     * @param marcRecordStr
     * @return
     */
    private Record getRecordFromRawMarc(String marcRecordStr)
    {
        MarcStreamReader reader;
        boolean tryAgain = false;
        do {
            try {
                tryAgain = false;
                reader = new MarcStreamReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext())
                {
                    Record record = reader.next();
                    return(record);
                }
            }
            catch( MarcException me)
            {
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
     * Extract the marc record from JSON string
     * @param marcRecordStr
     * @return
     */
    private Record getRecordFromJsonString(String marcRecordStr)
    {
        MarcJsonReader reader;
        boolean tryAgain = false;
        do {
            try {
                tryAgain = false;
                reader = new MarcJsonReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext())
                {
                    Record record = reader.next();
                    return(record);
                }
            }
            catch( MarcException me)
            {
                me.printStackTrace();
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        } while (tryAgain);
        return(null);
    }

    // error output
    static BufferedWriter errOut = null;

    /**
     * Extract marc record from MarcXML
     * @param marcRecordStr MarcXML string
     * @return marc4j Record
     */
    public Record getRecordFromXMLString(String marcRecordStr)
    {
        MarcXmlReader reader;
        boolean tryAgain = false;
        do {
            try {
                tryAgain = false;
                reader = new MarcXmlReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext())
                {
                    Record record = reader.next();
//                    if (verbose)
//                    {
//                        System.out.println(record.toString());
//                        System.out.flush();
//                    }
                    return(record);
                }
            }
            catch( MarcException me)
            {
                try
                {
                    errOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("badRecs.xml"))));
                    errOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\">");
                }
                catch (FileNotFoundException e)
                {
                    // e.printStackTrace();
                    System.err.println(e.getMessage());
                }
                catch (IOException e)
                {
                    // e.printStackTrace();
                    System.err.println(e.getMessage());
                }
                String trimmed = marcRecordStr.substring(marcRecordStr.indexOf("<record>"));
                trimmed = trimmed.replaceFirst("</collection>", "");
                trimmed = trimmed.replaceAll("><", ">\n<");
                try
                {
                    errOut.write(trimmed);
                }
                catch (IOException e)
                {
                    // e.printStackTrace();
                    System.err.println(e.getMessage());
                }
                if (marcRecordStr.contains("<subfield code=\"&#31;\">"))
                {
                    // rewrite input string and try again.
                    marcRecordStr = marcRecordStr.replaceAll("<subfield code=\"&#31;\">(.)", "<subfield code=\"$1\">");
                    tryAgain = true;
                }
                else if (extractLeader(marcRecordStr).contains("&#")) //.("<leader>[^<>&]*&#[0-9]+;[^<>&]*</leader>"))
                {
                    // rewrite input string and try again.
                    // 07585nam a2200301 a 4500
                    String leader = extractLeader(marcRecordStr).replaceAll("&#[0-9]+;", "0");
                    marcRecordStr = marcRecordStr.replaceAll("<leader>[^<]*</leader>", leader);
                    tryAgain = true;
                }
                else
                {
                    me.printStackTrace();
                    //System.out.println("The bad record is: "+ marcRecordStr);
                    System.err.println("The bad record is: "+ marcRecordStr);
                }
            }
            catch (UnsupportedEncodingException e)
            {
                // e.printStackTrace();
                System.err.println(e.getMessage());
            }
        } while (tryAgain);
        return(null);

    }


    /**
     * Extract the leader from the marc record string
     * @param marcRecordStr marc record as a String
     * @return Leader leader string for the marc record
     */
    private String extractLeader(String marcRecordStr)
    {
        final String leadertag1 = "<leader>";
        final String leadertag2 = "</leader>";
        String leader = null;
        try {
            leader = marcRecordStr.substring(marcRecordStr.indexOf(leadertag1), marcRecordStr.indexOf(leadertag2)+leadertag2.length() );
        }
        catch (IndexOutOfBoundsException e)
        {}
        return leader;
    }

    public static void main(String args[])
    {
        String baseURLStr = "http://localhost:8983/solr";
        String query = null;
        String field = "marc_display";
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("-v")) verbose = true;
            else if (args[i].equals("-vv")) { verbose = true; veryverbose = true; }
            else if (args[i].startsWith("http")) baseURLStr = args[i];
            else if (args[i].contains(":")) query = args[i];
            else field = args[i];
        }
        RemoteSolrSearcher searcher = new RemoteSolrSearcher(baseURLStr, query, field);
        searcher.handleAll();
        System.exit(0);
    }
}
