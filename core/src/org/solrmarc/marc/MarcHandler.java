package org.solrmarc.marc;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.apache.log4j.Logger;
import org.marc4j.*;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.marcoverride.MarcUnprettyXmlReader;
import org.solrmarc.tools.*;

public abstract class MarcHandler {

	abstract protected int handleAll() throws IOException;

	protected SolrIndexer indexer;
	protected MarcReader reader;
	protected boolean verbose = false;
	protected ErrorHandler errors = null;
	protected boolean includeErrors = false;
    /** The full class name of SolrIndexer or the subclass to be used */
	protected String indexerClassName;
	protected String addnlArgs[] = null;
	protected Properties configProps;
    protected boolean inputTypeXML = false;
    protected boolean inputTypeJSON = false;
	protected boolean permissiveReader;
	protected String defaultEncoding;
    protected boolean to_utf_8;
    protected String combineConsecutiveRecordsFields = null;
// FIXME:  should just have the reader class declared outright rather than indirectly
    /** set to true to use CombineMultBibsMhldsReader */  
    protected boolean useStanfordCombiningReader = false;
	protected boolean showConfig = false;
	protected boolean showInputFile = false;
	protected String unicodeNormalize = null;
	
	private String solrmarcPath;
	private String siteSpecificPath;
    protected String homeDir = ".";

    /** The name of the _index.properties file */
	private String indexPropsFname;
    private final static String TRANS_MAP_DIR = "translation_maps";
	
    // Initialize logging category
    static Logger logger = Logger.getLogger(MarcHandler.class.getName());
	    
	public MarcHandler()
	{
	}
	
    /** 
     * Extract the manifest attribute Default-Config-File from the top level jar file 
     */
    public String getConfigPropsFileName()
    { 
    	String configPropsFname = null;

        ClassLoader classLoader = getClass().getClassLoader();

        // Note that the first jar loaded is the java classes.jar file, so 
        //  no need to look for the configPropsFname in classes.jar manifest

		// look for jar, then look for manifest inside jar or _config.properties file inside jar
        URL[] classLoaderUrls = ((URLClassLoader) classLoader).getURLs();
        for (URL classLoaderUrl : classLoaderUrls)
		{
			String fname = classLoaderUrl.getFile();
			if (fname.endsWith(".jar"))
			{
				configPropsFname = JarUtils.getConfigPropsFileNameFromJar(fname, null);
				if (configPropsFname != null && configPropsFname.length() > 0)
					break;
			}
		}

        logger.debug("MarcHandler.getConfigPropsFileName() returns: " + configPropsFname);
        return(configPropsFname);
    }
	
    
	public void init(String args[]) 
			throws FileNotFoundException
	{
        String configPropsFname = getConfigPropsFileName();
        
        
    	// set the System properties for marc data sources and 
    	//   solr.commit_at_end;  put rest in addnlArgs list
        List<String> addnlArgList = new ArrayList<String>();
        if (args.length > 0)
        {
            for (String arg : args)
            {
				if (arg == null)
					continue;
                String lc_arg = arg.toLowerCase();
                if (lc_arg.endsWith(".properties"))
                    configPropsFname = arg;
                else if (lc_arg.endsWith(".mrc") || lc_arg.endsWith(".marc"))
                {
                    System.setProperty("marc.path", arg);
                    System.setProperty("marc.source", "FILE");
                }
                else if (lc_arg.endsWith(".json") )
                {
                    System.setProperty("marc.path", arg);
                    System.setProperty("marc.source", "FILE");
                }
                else if (arg.equals("NONE"))
                    System.setProperty("marc.source", "NONE");
                else if (lc_arg.endsWith(".xml"))
                {
                    System.setProperty("marc.path", arg);
                    System.setProperty("marc.source", "FILE");
                }
                else if (lc_arg.endsWith(".del"))
                    System.setProperty("marc.ids_to_delete", arg);
                else if (arg.equals("DELETE_ONLY"))
                {
                    System.setProperty("marc.source", "NONE");
                    System.setProperty("marc.ids_to_delete", "stdin");
                }
                else if (lc_arg.equals("-nocommit"))
                    System.setProperty("solr.commit_at_end", "false");
                else
                    addnlArgList.add(arg);
            }
        }
        addnlArgs = addnlArgList.toArray(new String[0]);
        
        initLocal();
        if (configPropsFname != null) 
        {
            logger.debug("Loading config properties from " + configPropsFname);
            loadProperties(configPropsFname);
        }

        //  Load the custom Indexer (or the standard one)
        //  note the values of class vars indexerName and indexerProps 
        //  are initialized from the values in the config properties file, 
        //  loaded in the call to loadProperties above.
        if (indexerClassName != null)
            loadIndexer(indexerClassName, indexPropsFname); 
        
        processAdditionalArgs();
	}
		
    /** 
     * initLocal - local init for subclasses of MarcHandler
     */
	protected void initLocal()
    {
        // do nothing here, override in subclasses.
    }
    
    /** 
     * processAdditionalArgs - local init for subclasses of MarcHandler
     */
    protected void processAdditionalArgs()
    {
        // do nothing here, override in subclasses.
    }
    
    
	/**
	 * Initialize class variables form values in the config properties file
	 * @param configPropsFname name of the _config.properties file
	 * @throws FileNotFoundException if marc record file not found
	 */
	public void loadProperties(String configPropsFname) 
			throws FileNotFoundException
	{
        homeDir = getHomeDir();
        logger.debug("Current Directory = "+ (new File(".").getAbsolutePath()));
        
        if (configPropsFname.equals("null.properties"))
            configProps = new Properties();
        else
            configProps = PropertiesUtils.loadProperties(new String[]{homeDir}, configPropsFname, showConfig, "config.file.dir");

        loadLocalProperties();
        
        solrmarcPath = PropertiesUtils.getProperty(configProps, "solrmarc.path");
        solrmarcPath = normalizePathsProperty(homeDir, solrmarcPath);

        siteSpecificPath = PropertiesUtils.getProperty(configProps, "solrmarc.site.path");
        siteSpecificPath = normalizePathsProperty(homeDir, siteSpecificPath);
 
        // class name of SolrIndexer or the subclass to be used
        indexerClassName = PropertiesUtils.getProperty(configProps, "solrmarc.indexing.class");
        if (indexerClassName == null) 
        	indexerClassName = SolrIndexer.class.getName();
        
        // _index.properties file
        indexPropsFname = PropertiesUtils.getProperty(configProps, "solrmarc.indexing.properties");

        combineConsecutiveRecordsFields = PropertiesUtils.getProperty(configProps, "marc.combine_records");
        if (combineConsecutiveRecordsFields != null && combineConsecutiveRecordsFields.length() == 0) 
            combineConsecutiveRecordsFields = null;
        
        useStanfordCombiningReader = Boolean.parseBoolean(PropertiesUtils.getProperty(configProps, "stanford.combining.reader"));
        
        permissiveReader = Boolean.parseBoolean(PropertiesUtils.getProperty(configProps, "marc.permissive"));
        
        if (PropertiesUtils.getProperty(configProps, "marc.default_encoding") != null)
            defaultEncoding = PropertiesUtils.getProperty(configProps, "marc.default_encoding").trim();    
        else
            defaultEncoding = "BESTGUESS";

        verbose = Boolean.parseBoolean(PropertiesUtils.getProperty(configProps, "marc.verbose"));

        includeErrors = Boolean.parseBoolean(PropertiesUtils.getProperty(configProps, "marc.include_errors"));

        to_utf_8 = Boolean.parseBoolean(PropertiesUtils.getProperty(configProps, "marc.to_utf_8"));

        unicodeNormalize = PropertiesUtils.getProperty(configProps, "marc.unicode_normalize");
        if (unicodeNormalize != null) 
            unicodeNormalize = handleUnicodeNormalizeParm(unicodeNormalize);

        // assign MarcFactory
        String marcFactoryOverride = PropertiesUtils.getProperty(configProps, "marc.override");
        if (marcFactoryOverride != null)
            System.setProperty("org.marc4j.marc.MarcFactory", marcFactoryOverride.trim());
        else  // no override, tell solrmarc to use the default.
        	System.setProperty("org.marc4j.marc.MarcFactory", "org.marc4j.marc.impl.MarcFactoryImpl");

        reader = null;

        String marcRecsFname = PropertiesUtils.getProperty(configProps, "marc.path");
        String source = PropertiesUtils.getProperty(configProps, "marc.source", "STDIN").trim();
        if (source.equals("FILE") && marcRecsFname == null)
        	return;
        if (marcRecsFname != null)  
        	marcRecsFname = marcRecsFname.trim();

        loadReader(source, marcRecsFname);
	}
	
    /** 
     * loadLocalProperties - local init for subclasses of MarcHandler
     */
    protected void loadLocalProperties()
    {
        // do nothing here, override in subclasses.
    }
    
	/**
	 *  We only get here if the parm (unicodeNormalize2) is not null 
	 *  compare it against the valid values and return the correct value to use as the parm
	 */
	private String handleUnicodeNormalizeParm(String parm)
    {
	    if (parm == null) 
	    	return null;
        if (parm.equalsIgnoreCase("KC") || parm.equalsIgnoreCase("CompatibilityCompose"))
            parm = "KC";
        else if (parm.equalsIgnoreCase("C") || parm.equalsIgnoreCase("Compose") || parm.equalsIgnoreCase("true"))
            parm = "C";
        else if (parm.equalsIgnoreCase("D") || parm.equalsIgnoreCase("Decompose"))
            parm = "D";
        else if (parm.equalsIgnoreCase("KD") || parm.equalsIgnoreCase("CompatibiltyDecompose"))
            parm = "KD";
        else 
            parm = null;
        return parm;
    }

    private String getHomeDir()
    {
    	String result = new File(".").getAbsolutePath();
        logger.debug("Setting homeDir to: "+ result);
        return(result);
    }
	
    /**
     * normalizePathsProperty - normalize one or more : separated paths using the normalizePathProperty
     *     method
     *     
     * @param homeDir - directory where the OneJar jar file containing all of the program is found
     * @param path - one or more : separated paths to be normalized. 
     * @return  normalized form of  "homeDir/path"
     */
    private String normalizePathsProperty(String homeDir, String path)
    {
        if (path == null) return(null);
        String paths[] = path.split("[|]");
        StringBuffer result = new StringBuffer();
        for (String part : paths)
        {
            String resolved = normalizePathProperty(homeDir, part);
            if (result.length() > 0) 
            	result.append("|");
            result.append(resolved);
        }
        return(result.toString());
    }
    
    /**
	 * normalizePathProperty - if the passed in path is a relative path, make it be relative 
	 *     to the "home" directory (which is where the OneJar jar file containing all of the program
	 *     is found) rather than being relative to the "current" directory (which is whatever directory 
	 *     the user happens to be in when the the JVM was started. Also resolve the property strings
	 *     ${config.file.dir} as the directory containing the config file  and
	 *     ${solrmarc.jar.dir} as the directory containing the OneJar jar file.
	 *     Note if the passed-in path is null, or is already an absolute path, simply return that value.
	 *     
	 * @param homeDir - directory where the OneJar jar file containing all of the program is found
	 * @param path - a possibly relative file path to be normalized. 
	 * @return  normalized form of  "homeDir/path"
	 */
	private String normalizePathProperty(String homeDir, String path)
    {
        if (path != null)
        {
            if (path.contains("${config.file.dir}") && configProps.getProperty("config.file.dir") != null)
                path = path.replace("${config.file.dir}", configProps.getProperty("config.file.dir"));
            if (path.contains("${solrmarc.jar.dir}") && homeDir != null)
                path = path.replace("${solrmarc.jar.dir}", homeDir);
            while (path.matches(".*$\\{[a-z.]+\\}.*"))
            {
                String pattern = path.replaceFirst("$\\{([a-z.]+)\\}", "$1"); 
                String replace = PropertiesUtils.getProperty(configProps, pattern);
                if (pattern != null && replace != null)
                    path.replace("${"+pattern+"}", replace);
                else
                    break;
            }
            File smPath = new File(path);
            if (smPath != null && !smPath.isAbsolute()) 
            {
                smPath = new File(homeDir, path);
                try
                {
                    path = smPath.getCanonicalPath();
                }
                catch (IOException e)
                {
                    path = smPath.getAbsolutePath();
                }
            }
        }
        return(path);
    }

	/**
	 * instantiates reader (MarcReader), if it's null; otherwise does nothing
	 * @param source  "FILE" or "STDIN" or null (same as FILE)
	 * @param marcRecsFilename - name of file containing marc records
	 */
    public void loadReader(String source, String marcRecsFilename) 
    		throws FileNotFoundException
	{
        if (source.equals("FILE") || source.equals("STDIN"))
        {
        	// setup the input source
        	InputStream marcRecsInputStream = null;
        	if (source.equals("FILE")) 
        	{
                if (marcRecsFilename != null && marcRecsFilename.toLowerCase().endsWith(".xml")) 
                    inputTypeXML = true;
                else if (marcRecsFilename != null && marcRecsFilename.toLowerCase().endsWith(".json")) 
                    inputTypeJSON = true;
        		try 
        		{
            		if (marcRecsFilename == null)
            			logger.warn("no specified MARC data file");
            		else
            		{
                        if (showInputFile)
                            logger.info("Attempting to open data file: "+ new File(marcRecsFilename).getAbsolutePath());
                        else 
                            logger.debug("Attempting to open data file: "+ new File(marcRecsFilename).getAbsolutePath());
    					marcRecsInputStream = new FileInputStream(marcRecsFilename);
            		}

				} 
        		catch (FileNotFoundException e) 
        		{
		        	logger.fatal("Fatal error: Unable to open specified MARC data file: " + marcRecsFilename);
		        	throw new IllegalArgumentException("Fatal error: Unable to open specified MARC data file: " + marcRecsFilename);
				}
        	}
        	else // try to read data from stdin
        	{
                if (showInputFile)
                    logger.info("Attempting to read data from stdin ");
                else
                    logger.debug("Attempting to read data from stdin ");
        	    marcRecsInputStream = new BufferedInputStream(System.in);
        	    
        	    // read a little bit to try it
        		marcRecsInputStream.mark(10);
        		int firstByte = -1;
        		try 
        		{ 
        		    firstByte = marcRecsInputStream.read();
                    marcRecsInputStream.reset();
        		}
        		catch (IOException e)
        		{
                    logger.fatal("Fatal error: Exception reading from stdin");
                    throw new IllegalArgumentException("Fatal error: Exception reading from stdin");
        		}
        		if (firstByte == '<') 
        			inputTypeXML = true;
        		else if (firstByte == '{') 
        			inputTypeJSON = true;  
        	}
        	
        	// marcRecInputStream should be instantiated now
        	
        	// instantiate reader (the MarcReader)
            if (inputTypeXML)
                reader = new MarcUnprettyXmlReader(marcRecsInputStream);
            else if (inputTypeJSON)
                reader = new MarcJsonReader(marcRecsInputStream);
            else if (permissiveReader)
            {
                errors = new ErrorHandler();
                reader = new MarcPermissiveStreamReader(marcRecsInputStream, errors, to_utf_8, defaultEncoding);
            }
            else
                reader = new MarcPermissiveStreamReader(marcRecsInputStream, false, to_utf_8, defaultEncoding);
        }
        else if (source.equals("DIR"))
            reader = new MarcDirStreamReader(PropertiesUtils.getProperty(configProps, "marc.path").trim(), permissiveReader, to_utf_8);

        // reader is now set up for particular input source
        
        // do we need to wrap in a CombiningReader?
        if (reader != null && combineConsecutiveRecordsFields != null)
        {
        	if (useStanfordCombiningReader)
        		reader = new CombineMultBibsMhldsReader(reader, combineConsecutiveRecordsFields);
        	else
        	{
	            String combineLeftField = PropertiesUtils.getProperty(configProps, "marc.combine_records.left_field");
	            String combineRightField = PropertiesUtils.getProperty(configProps, "marc.combine_records.right_field");
	            if (errors == null)
	                reader = new MarcCombiningReader(reader, combineConsecutiveRecordsFields, combineLeftField, combineRightField);
	            else
	            {
	                ErrorHandler errors2 = errors;
	                errors = new ErrorHandler();
	                reader = new MarcCombiningReader(reader, errors, errors2, combineConsecutiveRecordsFields, combineLeftField, combineRightField);
	            }
        	}
        }
        
        // do we need to wrap in a MarcFilteredReader?
        String marcIncludeIfPresent = PropertiesUtils.getProperty(configProps, "marc.include_if_present");
        String marcIncludeIfMissing = PropertiesUtils.getProperty(configProps, "marc.include_if_missing");
        String marcDeleteSubfields = PropertiesUtils.getProperty(configProps, "marc.delete_subfields");
        if (marcDeleteSubfields != null)  
        	marcDeleteSubfields = marcDeleteSubfields.trim();
        if (reader != null && (marcIncludeIfPresent != null || marcIncludeIfMissing != null || marcDeleteSubfields != null))
            reader = new MarcFilteredReader(reader, marcIncludeIfPresent, marcIncludeIfMissing, marcDeleteSubfields);

        // Do translating last so that if we are Filtering as well as translating, we don't expend the 
        // effort to translate records, which may then be filtered out and discarded.
        if (reader != null && to_utf_8 && unicodeNormalize != null)
            reader = new MarcTranslatedReader(reader, unicodeNormalize);

        return;
	}

    /**
     * add path and path/TRANS_MAP_DIR to propertySearchPath; add path to propertySearchSet
     * @param pathToAdd
     * @param propertySearchPath
     * @param propertySearchSet
     */
    static protected void addToPropertySearchPath(String pathToAdd, ArrayList<String> propertySearchPath, Set<String> propertySearchSet)
    {
        if (!propertySearchSet.contains(pathToAdd))
        {
            propertySearchPath.add(pathToAdd);
            propertySearchPath.add(pathToAdd + File.separator + TRANS_MAP_DIR);
            propertySearchSet.add(pathToAdd);
        }
    }
    
    protected String[] makePropertySearchPath(String solrmarcPath, String siteSpecificPath, String configFilePath, String homeDir)
    {
		ArrayList<String> propertySearchPath = new ArrayList<String>();
		Set<String> propertySearchSet = new HashSet<String>();
		if (siteSpecificPath != null)
		{
			String sitePaths[] = siteSpecificPath.split("[|]");
			for (String site : sitePaths)
			{
				addToPropertySearchPath(site, propertySearchPath, propertySearchSet);
			}
		}
		if (solrmarcPath != null)
		{
			String smPaths[] = solrmarcPath.split("[|]");
		    for (String path : smPaths)
		    {
		    	addToPropertySearchPath(path, propertySearchPath, propertySearchSet);
		    }
		}
		if (configFilePath != null)
			addToPropertySearchPath(configFilePath, propertySearchPath, propertySearchSet);
		if (homeDir != null)
			addToPropertySearchPath(homeDir, propertySearchPath, propertySearchSet);

//FIXME:  this is a horrid way to get "dist" directory for production scripts without "hardcoding"        
		// add parent path of any jars in classpath
		ClassLoader classLoader = getClass().getClassLoader();
		URL[] classLoaderUrls = ((URLClassLoader) classLoader).getURLs();
		for (URL classLoaderUrl : classLoaderUrls)
		{
			String fname = classLoaderUrl.getFile();
			if (fname.endsWith(".jar"))
				addToPropertySearchPath(new File(fname).getParent(), propertySearchPath, propertySearchSet);
		}

        return(propertySearchPath.toArray(new String[0]));
    }
                    
	/**
	 * Load the Custom Indexer routine
	 */
	public void loadIndexer(String indexerName, String indexPropsFname) 
	{
	    // Setup the SolrMarc Indexer
        Class<?> indexerClass = null;

        try {
            indexerClass = Class.forName(indexerName);
        }
        catch (ClassNotFoundException e)
        {
            logger.error("Cannot load class: " + indexerName);
            Class<?> baseIndexerClass = SolrIndexer.class;
            String baseName = baseIndexerClass.getPackage().getName();
            String fullName = baseName + "." + indexerName;
            try {
                indexerClass = Class.forName(fullName);
            }
            catch (ClassNotFoundException e1)
            {
                logger.fatal("Cannot find custom indexer class named: "+ indexerName);
                throw new IllegalArgumentException("Error configuring Indexer from properties file.  Exiting...");
            }
        }
	    try
	    {
	        Constructor<?> constructor = indexerClass.getConstructor(new Class[]{String.class, String[].class});
	        String configFilePath = PropertiesUtils.getProperty(configProps, "config.file.dir");
	        String propertySearchPath[] = makePropertySearchPath(solrmarcPath, siteSpecificPath, configFilePath, homeDir);

	        Object instance = constructor.newInstance(indexPropsFname, propertySearchPath);
	
	        if (instance instanceof SolrIndexer)
	            indexer = (SolrIndexer)instance;
	        else
	        {
	            logger.fatal("Error: Custom Indexer " + indexerName + " must be subclass of SolrIndexer. ");
	            throw new IllegalArgumentException("Error: Custom Indexer " + indexerName + " must be subclass of SolrIndexer. ");
	        }
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	
	        if (e instanceof IllegalArgumentException)
	        {
	            logger.fatal("Error configuring Indexer from properties file.  Exiting...");
	            throw ((IllegalArgumentException) e);
	        }            
	
	        logger.fatal("Unable to load Custom indexer: " + indexerName);
	        throw new IllegalArgumentException("Error configuring Indexer from properties file.  Exiting...");
	    }
	}
}
