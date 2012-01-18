package org.solrmarc.tools; 

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Manifest; 

public class GetDefaultConfig 
{ 

    /** 
     * @param args 
     */ 
    public static void main(String[] args) 
    {
        String configProperties = GetDefaultConfig.getConfigPropsFileName("");
        System.out.println(configProperties); 
    }

    /** 
     * Extract the manifest attribute Default-Config-File from the top level jar file 
     *  I think this is fooched now that I'm not using "onejar"
     * @deprecated 
     */
    public static String getJarFileName()
    { 
        String jarFilename = null; 
        Class<?> bootClass;
        try
        {
//        	bootClass = Class.forName("com.simontuffs.onejar.Boot");
            bootClass = Class.forName("org.solrmarc.marc.MarcImporter");
            jarFilename = bootClass.getMethod("getMyJarPath").invoke(null).toString();
        }
        catch (Exception e)
        {
            // Program not running from within a OneJar 
            jarFilename = null;
        }
        return(jarFilename);
    }
    
// FIXME: code duplicates MarcHandler.getConfigPropsFileName()    
    /** 
     * Extract the manifest attribute Default-Config-File from the top level jar file 
     */
    public static String getConfigPropsFileName(String defaultValue)
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
        	configPropsFileName = manifest.getMainAttributes().getValue("Config-Properties-File");
        
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
//    	
//        String jarName = getJarFileName();
//        if (jarName != null)
//        {
//            try { 
//                JarFile jarFile = new JarFile(jarName); 
//                Manifest manifest = jarFile.getManifest(); 
//                String defConfig = manifest.getMainAttributes().getValue("Config-Properties-File"); 
//                if (defConfig != null && defConfig.length() > 0) 
//                    configPropsFileName = defConfig; 
//                else 
//                { 
//                    Enumeration entries = jarFile.entries(); 
//                    while (entries.hasMoreElements()) 
//                    { 
//                        ZipEntry entry = (ZipEntry)entries.nextElement(); 
//                        if (entry.getName().contains("config.properties")) 
//                            configPropsFileName = entry.getName(); 
//                    } 
//                } 
//            } 
//            catch (Exception e) 
//            { 
//                // no manifest property defining the config 
//            }
//        }
//        
//        if (configPropsFileName == null)
//            configPropsFileName = defaultValue;
//
//        return(configPropsFileName);
    }
}
