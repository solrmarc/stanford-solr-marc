package org.solrmarc.tools; 

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.Manifest; 
import java.util.zip.ZipEntry;

public class JarUtils 
{ 
	public final static String MANIFEST_CONFIG_PROPS_FILE_NAME = "Config-Properties-File";

    /** 
     * @param args 
     */ 
    public static void main(String[] args) 
    {
        String configProperties = JarUtils.getConfigPropsFnameFromClassLoader(null);
        System.out.println(configProperties); 
    }

    /** 
     * Get the jar file name from the onejar.Boot jar
     *  I think this is fooched now that I'm not using "onejar"
     * @deprecated 
     */
    public static String getJarFileName()
    { 
        String jarFilename = null; 
// FIXME:  hardcoding a value from build.properties!  Bad!  Need system property for this?  Or use value in _config.properties, if we don't get jar file first??        
        jarFilename = "SolrMarc";
//        Class<?> bootClass;
//        try
//        {
////        	bootClass = Class.forName("com.simontuffs.onejar.Boot");
//            bootClass = Class.forName("org.solrmarc.marc.MarcImporter");
//            jarFilename = bootClass.getMethod("getMyJarPath").invoke(null).toString();
//        }
//        catch (Exception e)
//        {
//            // Program not running from within a OneJar 
//            jarFilename = null;
//        }
        return(jarFilename);
    }
    
    /** 
     * Extract the manifest attribute Default-Config-File from the top level jar file;
     *   if there is no such manifest entry, then look for a jar file entry ending
     *   in _config.properties.
     */
    public static String getConfigPropsFileNameFromJar(String jarName, String defaultValue)
    {
    	String configPropsFileName = null;

        if (jarName != null)
        {
            try 
            { 
                JarFile jarFile = new JarFile(jarName); 
                Manifest manifest = jarFile.getManifest(); 
                configPropsFileName = getConfigPropsFnameFromManifest(manifest);
                if (configPropsFileName != null && configPropsFileName.length() > 0)
                	return configPropsFileName;
                else 
                { 
                    Enumeration entries = jarFile.entries(); 
                    while (entries.hasMoreElements()) 
                    { 
                        ZipEntry entry = (ZipEntry) entries.nextElement(); 
                    	String fname = entry.getName();
                    	if (fname.endsWith("_config.properties"))
                            configPropsFileName = fname; 
                    } 
                } 
            } 
            catch (Exception e) {  /* no manifest property defining the config */ }
        }
        
        if (configPropsFileName == null)
            configPropsFileName = defaultValue;

        return configPropsFileName;
    }

    
    public static String getConfigPropsFnameFromManifest(Manifest manifest)
    {
        String configPropsFileName = manifest.getMainAttributes().getValue(MANIFEST_CONFIG_PROPS_FILE_NAME);
        if (configPropsFileName != null && configPropsFileName.length() > 0)
        	return configPropsFileName;
        else 
        	return null;
    	
    }
    
    
// FIXME: code duplicates MarcHandler.getConfigPropsFileName()    
    /** 
     * Extract the manifest attribute Default-Config-File from the top level jar file 
     */
    public static String getConfigPropsFnameFromClassLoader(String defaultValue)
    {
    	String configPropsFileName = null;

    	// look for it in the manifest of the first jar loaded
    	Object obj = new Object();
        ClassLoader classLoader = obj.getClass().getClassLoader();
        URL manifestUrl = classLoader.getResource("META-INF/MANIFEST.MF");
        Manifest manifest = null;
        try
        {
        	manifest = new Manifest (manifestUrl.openStream());
        }
        catch (IOException e)
        {
        	//ignore;
        }
        if (manifest != null)
        	configPropsFileName = manifest.getMainAttributes().getValue(MANIFEST_CONFIG_PROPS_FILE_NAME);
        
        // if we didn't find it, look for the first file ending _config.properties in the jar's classpath
        if (configPropsFileName == null || configPropsFileName.length() == 0)
        {
        	// look for file ending in _config.properties
            //Get the URLs
            URL[] urls = ((URLClassLoader) classLoader).getURLs();
            for (int i=0; i< urls.length; i++)
            {
            	String fname = urls[i].getFile();
            	if (fname.endsWith("_config.properties"))
            	{
            		configPropsFileName = fname;
            		break;
            	}
            }
        }
        return(configPropsFileName);
    }
    
}
