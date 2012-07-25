package org.solrmarc.tools;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.apache.log4j.*;


public class PropertiesUtils {

    protected static Logger logger = Logger.getLogger(PropertiesUtils.class.getName());

    /**
     * Default Constructor,  private, so it can't be instantiated by other objects
     */
    private PropertiesUtils(){ }

    /**
     * Check first for a particular property in the System Properties, so that the -Dprop="value" command line arg
     * mechanism can be used to override values defined in the passed in property file.  This is especially useful
     * for defining the marc.source property to define which file to operate on, in a shell script loop.
     * @param props - property set in which to look.
     * @param propname - name of the property to lookup.
     * @return String - value stored for that property (or null if it doesn't exist)
     */
    public static String getProperty(Properties props, String propname)
    {
        return getProperty(props, propname, null);
    }

    /**
     * Check first for a particular property in the System Properties, so that the -Dprop="value" command line arg
     * mechanism can be used to override values defined in the passed in property file.  This is especially useful
     * for defining the marc.source property to define which file to operate on, in a shell script loop.
     * @param props - property set in which to look.
     * @param propname - name of the property to lookup.
     * @param defVal - the default value to use if property is not defined
     * @return String - value stored for that property (or the  if it doesn't exist)
     */
    public static String getProperty(Properties props, String propname, String defVal)
    {
        String prop = System.getProperty(propname);
        if (prop != null)
            return(prop);
        else
        {
        	prop = props.getProperty(propname);
            if (prop != null)
                return(prop);
        }
        return defVal;
    }

    /**
     * load a properties file into a Properties object
     * @param propertyPaths the directories to search for the properties file
     * @param propertyFileName name of the sought properties file
     * @return Properties object
     */
    public static Properties loadProperties(String propertyPaths[], String propertyFileName)
    {
        return(loadProperties(propertyPaths, propertyFileName, false, null));
    }
    /**
     * load a properties file into a Properties object
     * @param propertyPaths the directories to search for the properties file
     * @param propertyFileName name of the sought properties file
     * @param showName whether the name of the file/resource being read should be shown.
     * @return Properties object
     */
    public static Properties loadProperties(String propertyPaths[], String propertyFileName, boolean showName)
    {
        return(loadProperties(propertyPaths, propertyFileName, showName, null));
    }
    /**
     * load a properties file into a Properties object
     * @param propertyPaths the directories to search for the properties file
     * @param propertyFileName name of the sought properties file
     * @param showName whether the name of the file/resource being read should be shown.
     * @return Properties object
     */
    public static Properties loadProperties(String propertyPaths[], String propertyFileName, boolean showName, String filenameProperty)
    {
        String inputStreamSource[] = new String[]{null};
        InputStream in = getPropertyFileInputStream(propertyPaths, propertyFileName, showName, inputStreamSource);
        String errmsg = "Fatal error: Unable to find specified properties file: " + propertyFileName;

        // load the properties
        Properties props = new Properties();
        try
        {
            props.load(in);
            in.close();
            if (filenameProperty != null && inputStreamSource[0] != null)
            {
                File tmpFile = new File(inputStreamSource[0]);

                props.setProperty(filenameProperty, tmpFile.getParent());
            }
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(errmsg);
        }
        return props;
    }


    public static InputStream getPropertyFileInputStream(String[] propertyPaths, String propertyFileName)
    {
        return(getPropertyFileInputStream(propertyPaths, propertyFileName, false));
    }

    public static InputStream getPropertyFileInputStream(String[] propertyPaths, String propertyFileName, boolean showName)
    {
        return(getPropertyFileInputStream(propertyPaths, propertyFileName, false, null));
    }

    public static InputStream getPropertyFileInputStream(String[] propertyPaths, String propertyFileName, boolean showName, String inputSource[])
        {
        InputStream in = null;
        // look for properties file in paths
        if (propertyPaths != null)
        {
            File propertyFile = new File(propertyFileName);

            int pathCnt = 0;
            do
            {
                if (propertyFile.exists() && propertyFile.isFile() && propertyFile.canRead())
                {
                    try
                    {
                        in = new FileInputStream(propertyFile);
                        if (inputSource != null && inputSource.length >= 1)
                        {
                            inputSource[0] = propertyFile.getAbsolutePath();
                        }
                        if (showName)
                            logger.info("Opening file: "+ propertyFile.getAbsolutePath());
                        else
                            logger.debug("Opening file: "+ propertyFile.getAbsolutePath());
                    }
                    catch (FileNotFoundException e)
                    {
                        // simply eat this exception since we should only try to open the file if we previously
                        // determined that the file exists and is readable.
                    }
                    break;   // we found it!
                }
                if (propertyPaths != null && pathCnt < propertyPaths.length)
                {
                    propertyFile = new File(propertyPaths[pathCnt], propertyFileName);
                }
                pathCnt++;
            } while (propertyPaths != null && pathCnt <= propertyPaths.length);
        }
        // if we didn't find it as a file, look for it as a URL
        String errmsg = "Fatal error: Unable to find specified properties file: " + propertyFileName;
        if (in == null)
        {
            PropertiesUtils utilObj = new PropertiesUtils();
            URL url = utilObj.getClass().getClassLoader().getResource(propertyFileName);
            if (url == null)
                url = utilObj.getClass().getResource("/" + propertyFileName);
            if (url == null)
            {
                logger.error(errmsg);
                throw new IllegalArgumentException(errmsg);
            }
            if (showName)
                logger.info("Opening resource via URL: "+ url.toString());
            else
                logger.debug("Opening resource via URL: "+ url.toString());

/*
            if (url == null)
                url = utilObj.getClass().getClassLoader().getResource(propertyPath + "/" + propertyFileName);
            if (url == null)
                url = utilObj.getClass().getResource("/" + propertyPath + "/" + propertyFileName);
*/
            if (url != null)
            {
                try
                {
                    in = url.openStream();
                }
                catch (IOException e)
                {
                    throw new IllegalArgumentException(errmsg);
                }
            }
        }
        return(in);
    }

	/**
	 * load list contained in the file.  list file should contain a series of
	 * values (one per line);  comments are preceded by a '#' character
	 * @param listFilename the name of the file containing the data
	 * @param possiblePaths array of paths in which to seek the list file
	 * @return a List of the values read from the file
	 */
	public static Set<String> loadPropertiesSet(String[] possiblePaths, String listFilename)   {
		Set<String> result = new LinkedHashSet<String>();
	    InputStream propFileIS = getPropertyFileInputStream(possiblePaths, listFilename);
	    BufferedReader propFileBR = new BufferedReader(new InputStreamReader(propFileIS));
	    String line;
	    try
	    {
	        while ((line = propFileBR.readLine()) != null)
	        {
	            String linePieces[] = line.split("#");
	            String value = linePieces[0].trim();
	            if (value.length() > 0)
	            	result.add(value);
	        }
	    }
	    catch (IOException e)
	    {
	    	System.err.println("error reading " + listFilename);
	        e.printStackTrace();
	    }
	    return result;
	}


}
