package org.solrmarc.solr;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.*;
//import org.apache.solr.client.solrj.impl.BinaryRequestWriterV1;
//import org.apache.solr.client.solrj.impl.BinaryResponseParserV1;
import org.apache.solr.client.solrj.request.RequestWriter;
//import org.solrmarc.tools.Utils;
import org.xml.sax.InputSource;

public class SolrCoreLoader
{
    
//    public void initialize(String libDir) throws Exception {
//        File dependencyDirectory = new File(libDir);
//        File[] files = dependencyDirectory.listFiles();
//        ArrayList<URL> urls = new ArrayList<URL>();
//        for (int i = 0; i < files.length; i++) {
//            if (files[i].getName().endsWith(".jar")) {
//            urls.add(files[i].toURL());
//            //urls.add(files[i].toURI().toURL());
//            }
//        }
//        classLoader = new JarFileClassLoader("Scheduler CL" + System.currentTimeMillis(), 
//            urls.toArray(new URL[urls.size()]), 
//            GFClassLoader.class.getClassLoader());
//        }

	
//    public static SolrProxy loadCore(String solrCoreDir, String solrDataDir, String solrCoreName, Logger logger)
//    {
//        return loadEmbeddedCore(solrCoreDir, solrDataDir, solrCoreName, true, logger);
//    }

    public static SolrProxy loadEmbeddedCore(String solrCoreDir, String solrDataDir, String solrCoreName, boolean useBinaryRequestHandler, Logger logger)
    {
    	logger.warn("loading EMBEDDED CORE version of Solr Proxy!!!!");
    	try
        {
            // create solrCoreObject and coreContainerObj
            Object solrCoreObj = null;
            Class<?> coreContainerClass = Class.forName("org.apache.solr.core.CoreContainer");
            Object coreContainerObj = null;

            File multicoreConfigFile = new File(solrCoreDir + "/solr.xml");
            if (multicoreConfigFile.exists())
            {
                // multicore Solr 1.3 installation 
                logger.info("Using the multicore schema file at : " + multicoreConfigFile.getAbsolutePath());
                logger.info("Using the " + solrCoreName + " core");

                if (solrDataDir == null) 
                {
                	solrDataDir = solrCoreDir + "/" + solrCoreName;
                }
                System.setProperty("solr.data.dir", solrDataDir);
                logger.info("Using the data directory of: " + solrDataDir);
    
                // instantiate CoreContainer object with constructor CoreContainer(solrCoreDir, multicoreConfigFile);
                Constructor<?> coreContainerConstructor = coreContainerClass.getConstructor(String.class, File.class);
                coreContainerObj = coreContainerConstructor.newInstance(solrCoreDir, multicoreConfigFile);
                
                // instantiate SolrCore object  via  CoreContainer.getCore(solrCoreName)
                Method getCoreMethod = coreContainerClass.getMethod("getCore", String.class);
                solrCoreObj = getCoreMethod.invoke(coreContainerObj, solrCoreName);
                
                }
            else  // non-multicore Solr 1.3 installation 
            {
                if (solrDataDir == null) 
                {
                    solrDataDir = solrCoreDir + "/" + "data";
                }
                System.setProperty("solr.data.dir", solrDataDir);
                
                // instantiate SolrConfig object with constructor SolrConfig(solrCoreDir, "solrconfig.xml", null)
                Class<?> solrConfigClass = Class.forName("org.apache.solr.core.SolrConfig");
                Constructor<?> solrConfigConstructor = null;
                try {
                    solrConfigConstructor = solrConfigClass.getConstructor(String.class, String.class, InputStream.class);
                }
                catch (NoSuchMethodException e)
                {
                    solrConfigConstructor = solrConfigClass.getConstructor(String.class, String.class, InputSource.class);
                }
                Object solrConfig = solrConfigConstructor.newInstance(solrCoreDir, "solrconfig.xml", null);
                
                // instantiate IndexSchema object with constructor IndexSchema(solrConfigObj, "schema.xml" null)
                Class<?> indexSchemaClass = Class.forName("org.apache.solr.schema.IndexSchema");
                Constructor<?> IndexSchemaConstructor = null;
                try {
                    IndexSchemaConstructor = indexSchemaClass.getConstructor(solrConfigClass, String.class, InputStream.class);
                }
                catch (NoSuchMethodException e)
                {
                    IndexSchemaConstructor = indexSchemaClass.getConstructor(solrConfigClass, String.class, InputSource.class);
                }
                Object  solrSchema = IndexSchemaConstructor.newInstance(solrConfig, "schema.xml", null);
                
                // instantiate CoreContainer object via no arg constructor
                Constructor<?> coreContainerConstructor = coreContainerClass.getConstructor();
                coreContainerObj = coreContainerConstructor.newInstance();
                
                solrCoreName = "Solr";  // used to create solrCoreObj and solrServerObj below

                // instantiate CoreDescriptor object with constructor CoreDescriptor(coreContainerObj, "Solr" "solrCoreDir/conf")
                Class<?> coreDescClass = Class.forName("org.apache.solr.core.CoreDescriptor");
                Constructor<?> coreDescConstructor = coreDescClass.getConstructor(coreContainerClass, String.class, String.class);
                Object coreDescObj = coreDescConstructor.newInstance(coreContainerObj, solrCoreName, solrCoreDir+"/conf");

                // instantiate SolrCore object with constructor SolrCore(solrCoreName, solrDataDir, SolrConfigObj, IndexSchemaObj, CoreDescriptorObj);  
                Class<?> solrCoreClass = Class.forName("org.apache.solr.core.SolrCore");
                Constructor<?> solrCoreConstructor = solrCoreClass.getConstructor(String.class, String.class, solrConfigClass, indexSchemaClass, coreDescClass);
                solrCoreObj = solrCoreConstructor.newInstance(solrCoreName, solrDataDir, solrConfig, solrSchema, coreDescObj);

                // Register SolrCore descriptor in the container registry using the specified name
                coreContainerClass.getMethod("register", String.class, solrCoreClass, boolean.class).invoke(coreContainerObj, solrCoreName, solrCoreObj, false);

            } // end non-multicore Solr 1.3 installation 
                
            // create solrServerObj from solrCore and coreContainerObj 
            Object solrServerObj = null;
                if (useBinaryRequestHandler)
                { 
                    Class<?> embeddedSolrServerClass = Class.forName("org.solrmarc.solr.embedded.SolrServerEmbeddedImpl");
                    Constructor<?> embeddedSolrServerConstructor = embeddedSolrServerClass.getConstructor(Object.class, Object.class);
                    solrServerObj = embeddedSolrServerConstructor.newInstance(solrCoreObj, coreContainerObj);
                }
                else
                {
            	    // go for ancient solrj version
                    try {
                        Class<?> embeddedSolrServerClass = Class.forName("org.apache.solr.client.solrj.embedded.EmbeddedSolrServer");
                        Constructor<?> embeddedSolrServerConstructor = embeddedSolrServerClass.getConstructor(coreContainerClass, String.class);
                        solrServerObj = embeddedSolrServerConstructor.newInstance(coreContainerObj, solrCoreName);
                    }
                    catch (Exception e)
                    {
                        if (e instanceof ClassNotFoundException || (e instanceof InvocationTargetException && 
                            e.getCause() instanceof java.lang.NoClassDefFoundError) )
                        {
                            logger.error("Error loading class:org.apache.solr.client.solrj.embedded.EmbeddedSolrServer : " + e.getCause());
                            Class<?> embeddedSolrServerClass = Class.forName("org.solrmarc.solr.embedded.SolrServerEmbeddedImpl");
                            Constructor<?> embeddedSolrServerConstructor = embeddedSolrServerClass.getConstructor(Object.class, Object.class);
                            solrServerObj = embeddedSolrServerConstructor.newInstance(solrCoreObj, coreContainerObj);
                        }
                        else
                        {
                            logger.error("Error loading class:org.apache.solr.client.solrj.embedded.EmbeddedSolrServer : " + e.getCause());
                            e.printStackTrace();
                        }
                    }
                }
                
            return(new SolrServerProxy((SolrServer) solrServerObj));
        }
        catch (Exception e)
        {
        	e.getCause().printStackTrace();
//            e.printStackTrace();
            System.err.println("Error: Problem instantiating SolrCore");               
            logger.error("Error: Problem instantiating SolrCore");
            System.exit(1);
        }

        return null;
    }
    
    public static SolrProxy loadRemoteSolrServer(String solrHostUpdateURL, boolean useBinaryRequestHandler, boolean useStreamingServer)
    {
        SolrProxy solrProxy = null;
        String urlString = solrHostUpdateURL.replaceAll("[/\\\\]update$", "");
/* 
// commented out for DEBUG       
        boolean supportsJavabin = false;
        String solrversion = "UNKNOWN";
        // request the solr/admin/registry info page and extract info about the version 
        // number of the solr server and whether it supports javabin
        try
        {
            URL url = new URL(urlString+"/admin/registry.jsp");            
System.err.println("DEBUG:    SolrCoreLoader.loadRemoteSolrServer about to open registry url");            
//            InputStream is = url.openStream();
            String registryInfo = Utils.readStreamIntoString(is);
            int solrversionStart = registryInfo.indexOf("<solr-spec-version>");
            int solrversionEnd = registryInfo.indexOf("</solr-spec-version>");
            if (solrversionStart != -1 && solrversionEnd != -1)
            {
                solrversion = registryInfo.substring(solrversionStart+("<solr-spec-version>").length(), solrversionEnd);
            }
            int javabinStart = registryInfo.indexOf("/update/javabin");
            int javabinEnd = registryInfo.indexOf("BinaryUpdateRequestHandler");
            supportsJavabin = (javabinStart != -1 && javabinEnd != -1);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        if (useBinaryRequestHandler && !supportsJavabin)
        {
            String errmsg = "Warning: Specifying binary request handling from a Solr Server that doesn't support it. Enable it in the solrconfig file for that server.";
            System.err.println(errmsg); 
            useBinaryRequestHandler = false;
        }
*/
        try
        {        	
            CommonsHttpSolrServer httpsolrserver;
            if (useStreamingServer)
                httpsolrserver = new StreamingUpdateSolrServer(urlString, 100, 2); 
            else
                httpsolrserver = new CommonsHttpSolrServer(urlString);
            
// DEBUG:  commented out b/c no longer care??            
//            if (solrversion.equals("UNKNOWN") || !useBinaryRequestHandler)
            {
//                System.err.println("Solrversion "+solrversion+" using xml response writer");
                httpsolrserver.setRequestWriter(new RequestWriter());
                httpsolrserver.setParser(new XMLResponseParser());
            }
// DEBUG: commented out b/c no longer care??            
//            else if (solrversion.startsWith("1.4"))
//            {
//                System.err.println("Solrversion "+solrversion+" using v1 binary response writer");
//                httpsolrserver.setRequestWriter(new BinaryRequestWriterV1());
//                httpsolrserver.setParser(new BinaryResponseParserV1());
//            }
//            else
//            {
//                System.err.println("Solrversion "+solrversion+" using v2 binary response writer");
//                httpsolrserver.setRequestWriter(new BinaryRequestWriter());
//                httpsolrserver.setParser(new BinaryResponseParser());
//            }
            
            if (useBinaryRequestHandler)
            {
            	httpsolrserver.setRequestWriter(new BinaryRequestWriter());
            	httpsolrserver.setParser(new BinaryResponseParser());
            }
            solrProxy = new SolrServerProxy(httpsolrserver); 
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        return(solrProxy);
    }

}
