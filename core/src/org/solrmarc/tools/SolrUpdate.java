package org.solrmarc.tools;

import java.io.*;
import java.net.*;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.solrmarc.tools.JarUtils;


public class SolrUpdate
{
	// Initialize logging category
	protected static Logger logger = Logger.getLogger(SolrUpdate.class.getName());
    private static boolean verbose = false;

    /**
     * @param args - main may be here just for testing
     */
    public static void main(String[] args)
    {
        String solrServerURL = null;
        Properties configProps;
        
        if(args.length > 0)
        {
            for (String arg : args)
            {
                if (arg.startsWith("http"))
                    solrServerURL = arg;
                if (arg.equals("-v"))
                    verbose = true;
            }
        }
        if (solrServerURL == null)
        {
            String configProperties = JarUtils.getConfigPropsFnameFromManifest(null);
            if (configProperties != null)
            {
                String homeDir = getHomeDir();
                configProps = PropertiesUtils.loadProperties(new String[]{homeDir}, configProperties, false);
                // Ths URL of the currently running Solr server
                solrServerURL = PropertiesUtils.getProperty(configProps, "solr.hosturl");
            }
        }
        
        try
        {
            if (verbose) 
                System.out.println("Connecting to solr server at URL: " + solrServerURL);
            else 
                logger.info("Connecting to solr server at URL: " + solrServerURL);
            sendCommitToSolr(solrServerURL);
        }
        catch (MalformedURLException me)
        {
            if (verbose) 
                System.out.println("Specified URL is malformed: " + solrServerURL);
            else 
                logger.error("Specified URL is malformed: " + solrServerURL);
        }
        catch (IOException ioe)
        {
            if (verbose) 
                System.out.println("Unable to establish connection to solr server at URL: " + solrServerURL);
            else 
                logger.error("Unable to establish connection to solr server at URL: " + solrServerURL);
        }
    }
    
    private static String getHomeDir()
    {
        String result = JarUtils.getJarFileName();       
        if (result == null)
        {
            result = new File(".").getAbsolutePath();
            logger.debug("Setting homeDir to \".\"");
        }
        if (result != null) 
        	result = new File(result).getParent();
        logger.debug("Setting homeDir to: "+ result);
        return(result);
    }

    /**
     * If there is a running Solr server instance looking at the same index
     * that is being updated by this process, this function can be used to signal 
     * that server that the indexes have changed, so that it will find the new data
     * with out having to be restarted.
     * 
     * @param solrHostUpdateURL  the URL of the Solr server update request handler
     * for example:    http://localhost:8983/solr/update
     */
    public static void sendCommitToSolr(String solrHostUpdateURL) throws IOException
    {
        if (solrHostUpdateURL == null || solrHostUpdateURL.length() == 0) 
        	return;
        
        URL url = new URL(solrHostUpdateURL);
        URLConnection urlConn = url.openConnection();

        urlConn.setDoInput (true); // we want input (ie. response) from HTTP

        // set up the POST header for the commit command
        urlConn.setDoOutput (true); // we want output (ie. a request) for HTTP
        urlConn.setUseCaches (false);  // we don't want caching
        urlConn.setRequestProperty("Content-Type", "text/xml");
        urlConn.setRequestProperty("charset", "utf-8");

        // POST commit command to Solr
        DataOutputStream postContentOutStream = new DataOutputStream(urlConn.getOutputStream ());
        postContentOutStream.writeBytes("<commit/>");
        postContentOutStream.flush();
        postContentOutStream.close();

        // Get response data.
        BufferedReader responseAsReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream ()));

        String str;
        while (null != ((str = responseAsReader.readLine())))
        {
            if (verbose) 
                System.out.println(str);
            else 
                logger.info(str);
        }

        responseAsReader.close();

        // Display response.
//        System.exit(0);
     }


}
