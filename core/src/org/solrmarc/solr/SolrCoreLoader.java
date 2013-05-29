package org.solrmarc.solr;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.*;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.xml.sax.InputSource;

public class SolrCoreLoader
{

	/**
	 * @deprecated
	 * @param solrCoreDir
	 * @param solrDataDir
	 * @param solrCoreName
	 * @param useBinaryRequestHandler
	 * @param logger
	 */
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
        HttpSolrServer httpSolrServer;
// possibly replaced by ConcurrentUpdateSolrServer;  possibly same bug of swallowing errors
//            if (useStreamingServer)
//                httpSolrServer = new StreamingUpdateSolrServer(urlString, 100, 2);
//            else
            httpSolrServer = new HttpSolrServer(urlString);

        if (!useBinaryRequestHandler)
        {
        	httpSolrServer.setRequestWriter(new RequestWriter());
        	httpSolrServer.setParser(new XMLResponseParser());
        }

        solrProxy = new SolrServerProxy(httpSolrServer);
        return(solrProxy);
    }

}
