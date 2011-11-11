package org.solrmarc.marc;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.*;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.marc4j.ErrorHandler;
import org.marc4j.marc.Record;
import org.solrmarc.solr.*;
import org.solrmarc.tools.*;

/**
 * @author Wayne Graham (wsgrah@wm.edu)
 * @version $Id: MarcImporter.java 1397 2011-01-18 20:15:34Z rh9ec@virginia.edu
 *          $
 * 
 */
public class MarcImporter extends MarcHandler
{
	/** needs to be visible to StanfordItemMarcImporter ... */
	protected SolrProxy solrProxy;
	protected boolean solrProxyIsRemote;

	protected String solrCoreDir;
	protected String solrDataDir;
	protected String solrCoreName;
	private String deleteRecordListFilename;
	private String deleteRecordIDMapper = null;
	private String solrHostURL;
	private String solrHostUpdateURL;
	protected boolean commitAtEnd = true;
	protected boolean optimizeAtEnd = false;
	protected boolean shuttingDown = false;
	protected boolean isShutDown = false;
	protected boolean justIndexDontAdd = false;
	private int recsReadCounter = 0;
	private int recsIndexedCounter = 0;
	private int idsToDeleteCounter = 0;
	private int recsDeletedCounter = 0;
	private boolean useBinaryRequestHandler = false;
	// Initialize logging category
	protected static Logger logger = Logger.getLogger(MarcImporter.class.getName());

	/**
	 * Constructs an instance with properties files
	 */
	public MarcImporter()
	{
		showConfig = true;
		showInputFile = true;
	}

	/**
	 * Load the properties file
	 * 
	 * @param properties
	 */
	@Override
	protected void loadLocalProperties()
	{
		// The solr.home directory
		solrCoreDir = PropertiesUtils.getProperty(configProps, "solr.path");

		// The solr data diretory to use
		solrDataDir = PropertiesUtils.getProperty(configProps, "solr.data.dir");

		// The name of the solr core to use, in a solr multicore environment
		solrCoreName = PropertiesUtils.getProperty(configProps, "solr.core.name");

		// Ths URL of the currently running Solr server
		solrHostURL = PropertiesUtils.getProperty(configProps, "solr.hosturl");

		// Ths URL of the currently running Solr server
		solrHostUpdateURL = PropertiesUtils.getProperty(configProps, "solr.updateurl");
		if (solrHostUpdateURL == null && solrHostURL != null && solrHostURL.length() > 0)
		{
			if (solrHostURL.endsWith("/update"))
				solrHostUpdateURL = solrHostURL;
			else
				solrHostUpdateURL = solrHostURL + "/update";
		}

		// set solrLogLevel  default is WARNING
		String solrLogLevel = PropertiesUtils.getProperty(configProps, "solr.log.level");
		setSolrLogLevel(solrLogLevel);

		// Set solrmarc log level  default is solrLogLevel or WARNING if null
		String solrmarcLogLevel = PropertiesUtils.getProperty(configProps, "solrmarc.log.level");
		setSolrMarcLogLevel(solrmarcLogLevel, solrLogLevel);
		
		// Specification of how to modify the entries in the delete record file
		// before passing the id onto Solr. Based on syntax of String.replaceAll
		// To prepend a 'u' specify the following: "(.*)->u$1"
		deleteRecordIDMapper = PropertiesUtils.getProperty(configProps, "marc.delete_record_id_mapper");
		if (deleteRecordIDMapper != null)
		{
			String parts[] = deleteRecordIDMapper.split("->");
			if (parts.length == 2)
			{
				String mapPattern = parts[0];
				String mapReplace = parts[1];
				try
				{
					String testID = "12345";
					String tested = testID.replaceFirst(mapPattern, mapReplace);
					logger.info("Valid Regex pattern specified in property: marc.delete_record_id_mapper");
				} 
				catch (PatternSyntaxException pse)
				{
					deleteRecordIDMapper = null;
					logger.warn("Invalid Regex pattern specified in property: marc.delete_record_id_mapper");
				}
			}
			else
			{
				deleteRecordIDMapper = null;
				logger.warn("Invalid Regex pattern specified in property: marc.delete_record_id_mapper");
			}
		}

		justIndexDontAdd = Boolean.parseBoolean(PropertiesUtils.getProperty(configProps, "marc.just_index_dont_add"));
		if (justIndexDontAdd)
		{
			Utils.setLog4jLogLevel(org.apache.log4j.Level.WARN);
			optimizeAtEnd = false;
			commitAtEnd = false;
		}
		else
		{
			optimizeAtEnd = Boolean.parseBoolean(PropertiesUtils.getProperty(configProps, "solr.optimize_at_end"));
			if (optimizeAtEnd)
				commitAtEnd = true;
			String val = PropertiesUtils.getProperty(configProps, "solr.commit_at_end");
			if (val != null)
				commitAtEnd = Boolean.parseBoolean(val);
		}
		deleteRecordListFilename = PropertiesUtils.getProperty(configProps, "marc.ids_to_delete");

		// Set up Solr core
		boolean useEmbeddedSolrServerProxy = Boolean.parseBoolean(PropertiesUtils.getProperty(configProps, "solrmarc.use_solr_server_proxy"));
		useBinaryRequestHandler = Boolean.parseBoolean(PropertiesUtils.getProperty(configProps, "solrmarc.use_binary_request_handler"));
		solrProxy = getSolrProxy(useEmbeddedSolrServerProxy);

		return;
	}

	/**
	 * Delete records from the index
	 * 
	 * @return Number of records deleted
	 */
	public int deleteRecords()
	{
		idsToDeleteCounter = 0;
		recsDeletedCounter = 0;

		if (deleteRecordListFilename == null || deleteRecordListFilename.length() == 0)
			return recsDeletedCounter;

		String mapPattern = null;
		String mapReplace = null;
		if (deleteRecordIDMapper != null)
		{
			String parts[] = deleteRecordIDMapper.split("->");
			if (parts.length == 2)
			{
				mapPattern = parts[0];
				mapReplace = parts[1];
			}
		}
		BufferedReader is = null;
		File delFile = null;
		try
		{
			if (deleteRecordListFilename.equals("stdin"))
				is = new BufferedReader(new InputStreamReader(System.in));
			else
			{
				delFile = new File(deleteRecordListFilename);
				is = new BufferedReader(new FileReader(delFile));
			}
			String line;
			boolean fromCommitted = true;
			boolean fromPending = true;
			while ((line = is.readLine()) != null)
			{
				if (shuttingDown)
					break;
				line = line.trim();
				if (line.startsWith("#"))
					continue;
				if (deleteRecordIDMapper != null)
					line = line.replaceFirst(mapPattern, mapReplace);

				String id = line;
				idsToDeleteCounter++;
				if (verbose)
				{
					System.out.println("Deleting record with id :" + id);
					logger.info("Deleting record with id :" + id);
				}
				solrProxy.delete(id, fromCommitted, fromPending);
				recsDeletedCounter++;
			}
		} 
		catch (FileNotFoundException fnfe)
		{
			logger.error("Error: unable to find and open delete-record-id-list: " + deleteRecordListFilename, fnfe);
		} 
		catch (IOException ioe)
		{
			logger.error("Error: reading from delete-record-id-list: " + deleteRecordListFilename, ioe);
		}
		return recsDeletedCounter;
	}

	
	/**
	 * Iterate over the marc records in the file and add them to the index
	 * 
	 * @return Number of records indexed
	 */
	public int importRecords()
	{
		// keep track of record counts
		recsReadCounter = 0;
		recsIndexedCounter = 0;

		while (reader != null && reader.hasNext())
		{
			if (shuttingDown)
				break;

			// read next record
			Record record = null;
			try
			{
				record = reader.next();
				recsReadCounter++;
			} 
			catch (Exception e)
			{
				String recCntlNum = null;
				try
				{
					recCntlNum = record.getControlNumber();
				} 
				catch (NullPointerException npe) { /* ignore */	}

				if (e instanceof SolrMarcRuntimeException)
				{
					// stop reading
					String errmsg = "Unable to read record " + (recCntlNum != null ? recCntlNum : "") + " (record count " + recsReadCounter + ") -- " + e.getMessage();
					logger.fatal(errmsg);
					logger.fatal("******** Halting indexing! ********");
					throw (SolrMarcRuntimeException) e;
				}
				else
				{
					// keep reading
					logger.error("Error reading record: " + (recCntlNum != null ? recCntlNum : "") + " (record count " + recsReadCounter + ") -- " + e.getMessage(), e);
					continue;
				}
			}

			String recCntlNum = null;
			try
			{
				recCntlNum = record.getControlNumber();
			} 
			catch (NullPointerException npe) { /* ignore */	}

			// index the record
			try
			{
				boolean added = addToIndex(record);
				if (added)
				{
					recsIndexedCounter++;
					logger.info("Added record " + recsReadCounter + " read from file: " + recCntlNum);
				}
				else
					logger.info("Deleted record " + recsReadCounter	+ " read from file: " + recCntlNum);
			} catch (Exception e)
			{
				Throwable cause = null;
				if (e instanceof SolrRuntimeException)
					cause = e.getCause();
				if (cause != null && cause instanceof InvocationTargetException)
					cause = ((InvocationTargetException) cause).getTargetException();

				if (cause instanceof Exception && solrProxy.isSolrException((Exception) cause))
				{
					logger.error("Unable to index record " + (recCntlNum != null ? recCntlNum : "") + " (record count " + recsReadCounter + ") -- "	+ cause.getMessage());

					if (cause.getMessage().contains("missing required fields")
							|| cause.getMessage().contains("multiple values encountered for non multiValued field")
							|| cause.getMessage().contains("unknown field"))
					{
						// skip record, but keep indexing
					}
					else if (e instanceof SolrRuntimeException)
					{
						// stop indexing
						logger.fatal("******** Halting indexing! ********");
						throw (new SolrRuntimeException(cause.getMessage(),	(Exception) cause));
					}
				}
				else if (e instanceof SolrMarcIndexerException)
				{
					SolrMarcIndexerException smie = (SolrMarcIndexerException) e;
					if (smie.getLevel() == SolrMarcIndexerException.IGNORE)
						// skip record, but keep indexing
						logger.info("Ignored record " + (recCntlNum != null ? recCntlNum : "") + " (record count " + recsReadCounter + ")");
					else if (smie.getLevel() == SolrMarcIndexerException.DELETE)
						// skip record, but keep indexing
						logger.info("Deleted record " + (recCntlNum != null ? recCntlNum : "") + " (record count " + recsReadCounter + ")");
					else if (smie.getLevel() == SolrMarcIndexerException.EXIT)
					{
						// stop indexing
						logger.fatal("Serious Error flagged in record " + (recCntlNum != null ? recCntlNum : "") + " (record count " + recsReadCounter + ")");
						logger.fatal("******** Halting indexing! ********");
						throw (smie);
					}
				}
				else
				{
					// stop indexing
					logger.error("Unable to index record " + (recCntlNum != null ? recCntlNum : "") + " (record count " + recsReadCounter + ") -- " + e.getMessage(), e);
					// this error should (might?) only be thrown if we can't
					// write to the index
					// therefore, continuing to index would be pointless.
					if (e instanceof SolrRuntimeException)
					{
						logger.fatal("******** Halting indexing! ********");
						throw (SolrRuntimeException) e;
					}
				}
			} // catch basic reader exception
		} // while reader

		return recsIndexedCounter;
	}

	/**
	 * Add a record to the index
	 * 
	 * @param record  marc record to add
	 */
	private boolean addToIndex(Record record) throws IOException
	{
		try
		{
			Map<String, Object> fieldsMap = indexer.map(record, errors);
			String docStr = addToIndex(fieldsMap);

			if (verbose || justIndexDontAdd)
			{
				if (verbose)
				{
					System.out.println(record.toString());
					logger.info(record.toString());
				}
				System.out.println(docStr);
				logger.info(docStr);
			}
			return (true);
		} catch (SolrMarcIndexerException e)
		{
			if (e.getLevel() == SolrMarcIndexerException.IGNORE)
			{
				throw (e);
			}
			else if (e.getLevel() == SolrMarcIndexerException.DELETE)
			{
				String id = record.getControlNumber();
				if (id != null)
				{
					solrProxy.delete(id, true, true);
				}
				throw (e);
			}
			else if (e.getLevel() == SolrMarcIndexerException.EXIT)
			{
				throw (e);
			}
		}
		return (true);
	}

	/**
	 * Add a document to the index according to the fields map
	 * 
	 * @param record
	 *            marc record to add
	 * @return the document added, as a String
	 */
	protected String addToIndex(Map<String, Object> fieldsMap)
			throws IOException
	{
		if (fieldsMap.size() == 0)
			return null;
		if (errors != null && includeErrors)
		{
			if (errors.hasErrors())
			{
				addErrorsToMap(fieldsMap, errors);
			}
		}

		// NOTE: exceptions are dealt with by calling class
		return solrProxy.addDoc(fieldsMap, verbose, !justIndexDontAdd);
	}

	private void addErrorsToMap(Map<String, Object> map, ErrorHandler errors2)
	{
		map.put("marc_error", errors.getErrors());
	}

	/**
     * 
     */
	public void finish()
	{
		if (commitAtEnd)
		{
			try
			{
				// System.out.println("Calling commit");
				logger.info("Calling commit");
				solrProxy.commit(shuttingDown ? false : optimizeAtEnd);
				// System.out.println("Done with commit, closing Solr");
				logger.info("Done with the commit, closing Solr");
			} 
			catch (IOException ioe)
			{
				// System.err.println("Final commit and optimization failed");
				logger.error("Final commit and optimization failed: " + ioe.getMessage());
				logger.debug(ioe);
				// e.printStackTrace();
			}
		}

		solrProxy.close();
		solrProxy = null;
		logger.info("Setting Solr closed flag");
		isShutDown = true;
	}

	/**
	 * If there is a running Solr server instance looking at the same index that
	 * is being updated by this process, this function can be used to signal
	 * that server that the indexes have changed, so that it will find the new
	 * data with out having to be restarted.
	 * 
	 * uses member variable solrHostUpdateURL which contains the URL of the Solr
	 * server for example: http://localhost:8983/solr/update This value is taken
	 * from the solr.hosturl entry in the properties file.
	 */

	protected void signalServer()
	{
		if (shuttingDown || !commitAtEnd)
			return;
		// if solrCoreDir == null and solrHostUpdateURL != null then we are
		// talking to a remote
		// solr server during the main program, so there is no need to
		// separately contact
		// server to tell it to commit, therefore merely return.
		if ((solrCoreDir == null || solrCoreDir.length() == 0 || 
				solrCoreDir.equalsIgnoreCase("REMOTE")) && solrHostUpdateURL != null)
			return;
		if (solrHostUpdateURL == null || solrHostUpdateURL.length() == 0)
			return;
		try
		{
			logger.info("Connecting to solr server at URL: " + solrHostUpdateURL);
			SolrUpdate.signalServer(solrHostUpdateURL);
		} catch (MalformedURLException me)
		{
			// System.err.println("MalformedURLException: " + me);
			logger.error("Specified URL is malformed: " + solrHostUpdateURL);
		} catch (IOException ioe)
		{
			// System.err.println("IOException: " + ioe.getMessage());
			logger.warn("Unable to establish connection to solr server at URL: " + solrHostUpdateURL);
		}
	}

	/**
	 * Set the shutdown flag
	 */
	public void shutDown()
	{
		shuttingDown = true;
	}

	class MyShutdownThread extends Thread
	{
		MarcImporter importer;

		public MyShutdownThread(MarcImporter im)
		{
			importer = im;
		}

		public void run()
		{
			// System.err.println("Starting Shutdown hook");
			logger.info("Starting Shutdown hook");

			if (!importer.isShutDown)
			{
				logger.info("Stopping main loop");
				importer.shutDown();
			}
			while (!importer.isShutDown)
			{
				try
				{
					sleep(2000);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			logger.info("Finished Shutdown hook");
			// System.err.println("Finished Shutdown hook");
		}
	}

	/**
	 * Main loop in the MarcImporter class the handles all of importing and
	 * deleting of records.
	 * 
	 * @param args
	 */
	@Override
	public int handleAll()
	{
		Runtime.getRuntime().addShutdownHook(new MyShutdownThread(this));

		Date start = new Date();

		int numImported = 0;
		int numDeleted = 0;
		try
		{
			numImported = importRecords();
			numDeleted = deleteRecords();
		} 
		catch (Exception e)
		{
			logger.info("Exception occurred while Indexing: " + e.getMessage());
			logger.info("Setting Solr closed flag");
			// commit what we've got so far
			finish();
			isShutDown = true;
		}

		logger.info(" Adding " + recsIndexedCounter + " of " + recsReadCounter	+ " documents to index");
		logger.info(" Deleting " + recsDeletedCounter + " documents from index");

		if (!isShutDown)
			finish();

		if (!justIndexDontAdd)
			signalServer();

		Date end = new Date();
		long totalTime = end.getTime() - start.getTime();

		logger.info("Finished indexing in " + DateUtils.calcTime(totalTime));
		// System.out.println("Finished in " + DateUtils.calcTime(totalTime) );

		// calculate the time taken
		float indexingRate = numImported * 1000 / totalTime;

		// System.out.println("Indexed " + numImported + " at a rate of about "
		// + indexingRate + "per sec");
		// System.out.println("Deleted " + numDeleted + " records");

		logger.info("Indexed " + numImported + " at a rate of about " + indexingRate + " per sec");
		logger.info("Deleted " + numDeleted + " records");

		return (shuttingDown ? 1 : 0);
	}

	/**
	 * set up Proxy for SolrCore, or return existing one
	 * 
	 * @param useEmbedded  true if using solr embedded server
	 * @return SolrProxy object, instantiated
	 */
	public SolrProxy getSolrProxy(boolean useEmbedded)
	{
		if (solrProxy == null)
		{
			solrProxyIsRemote = false;
			if (solrHostUpdateURL != null && solrHostUpdateURL.length() > 0)
			{
				if ((solrCoreDir == null || solrCoreDir.length() == 0 || solrCoreDir.equalsIgnoreCase("REMOTE")))
					solrProxyIsRemote = true;
				else
				{
					URL solrhostURL;
					try
					{
						solrhostURL = new URL(solrHostUpdateURL);
						java.net.InetAddress address = java.net.InetAddress	.getByName(solrhostURL.getHost());

						String urlCanonicalHostName = address.getCanonicalHostName();
						String localCanonicalHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
						if (!(address.isLoopbackAddress() || urlCanonicalHostName.equals(localCanonicalHostName)))
							solrProxyIsRemote = true;
					} 
					catch (MalformedURLException e)
					{
						// URL seems invalid, assume that we want local access
						// to the solr index and proceed
						solrProxyIsRemote = false;
					} 
					catch (UnknownHostException e)
					{
						// hostname in URL seems invalid, assume that we want
						// local access to the solr index and proceed
						solrProxyIsRemote = false;
					}
				}
			}
			if (solrProxyIsRemote)
			{
				logger.info(" Connecting to remote Solr server at URL "	+ solrHostUpdateURL);

				if (useEmbedded)
				{
					try
					{
						String URL = solrHostUpdateURL.replaceAll("[/\\\\]update$", "");
						CommonsHttpSolrServer httpsolrserver = new CommonsHttpSolrServer(URL);
						if (useBinaryRequestHandler)
							httpsolrserver.setRequestWriter(new BinaryRequestWriter());

						solrProxy = new SolrServerProxy(httpsolrserver);
					} 
					catch (MalformedURLException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
					solrProxy = new SolrRemoteProxy(solrHostUpdateURL, useBinaryRequestHandler);
			}
			else
			{
				if (solrCoreDir.equals("@SOLR_PATH@"))
				{
					String errmsg = "Error: Solr home directory not initialized, please run setsolrhome";
					System.err.println(errmsg);
					logger.fatal(errmsg);
					System.exit(1);
				}
				
				File solrcoretest = new File(solrCoreDir);
				if (!solrcoretest.isAbsolute())
					solrcoretest = new File(homeDir, solrCoreDir);
				try
				{
					solrCoreDir = solrcoretest.getCanonicalPath();
					System.setProperty("solr.solr.home", solrCoreDir);
				} 
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (!solrcoretest.exists() || !solrcoretest.isDirectory())
				{
					String errmsg = "Error: Supplied Solr home directory does not exist: " + solrCoreDir;
					System.err.println(errmsg);
					logger.fatal(errmsg);
					System.exit(1);
				}
				File solrcoretest1 = new File(solrCoreDir, "solr.xml");
				File solrcoretest2 = new File(solrCoreDir, "conf");
				if (!solrcoretest1.exists() && !solrcoretest2.exists())
				{
					String errmsg = "Error: Supplied Solr home directory does not contain proper solr configuration: " + solrCoreDir;
					System.err.println(errmsg);
					logger.fatal(errmsg);
					System.exit(1);
				}
				logger.info(" Updating to Solr index at " + solrCoreDir);
				
				// set solrDataDir
				if (!solrcoretest1.exists() && solrDataDir == null)
					solrDataDir = new File(solrcoretest, "data").getAbsolutePath();
				else if (solrDataDir != null && solrDataDir.contains("${solr.path}"))
				{
					String dataPathFrag = solrDataDir.replaceFirst("[$][{]solr[.]path[}][/\\\\]+", "");
					solrDataDir = new File(solrCoreDir, dataPathFrag).getAbsolutePath();
				}
				if (solrDataDir != null)
				{
					System.setProperty("solr.data.dir", solrDataDir);
					logger.info("     Using Solr data dir " + solrDataDir);
				}
				
				if (solrCoreName != null && solrCoreName.length() != 0)
					logger.info("     Using Solr core " + solrCoreName);

				if (useEmbedded)
					solrProxy = SolrCoreLoader.loadEmbeddedCore(solrCoreDir, solrDataDir, solrCoreName, useBinaryRequestHandler, logger);
				else
					solrProxy = SolrCoreLoader.loadCore(solrCoreDir, solrDataDir, solrCoreName, logger);
			}
		}
		return (solrProxy);
	}

	
	/**
	 * set log level for org.apache.solr logger
	 *   default is WARNING
	 * @param solrLogLevelStr
	 */
	private void setSolrLogLevel(String solrLogLevelStr)
	{
		java.util.logging.Level solrLevel = java.util.logging.Level.WARNING;
		if (solrLogLevelStr != null)
		{
			if (solrLogLevelStr.equals("OFF"))
				solrLevel = java.util.logging.Level.OFF;
			if (solrLogLevelStr.equals("SEVERE"))
				solrLevel = java.util.logging.Level.SEVERE;
//			if (solrLogLevel.equals("WARNING"))
//				solrLevel = java.util.logging.Level.WARNING;
			if (solrLogLevelStr.equals("INFO"))
				solrLevel = java.util.logging.Level.INFO;
			if (solrLogLevelStr.equals("FINE"))
				solrLevel = java.util.logging.Level.FINE;
			if (solrLogLevelStr.equals("FINER"))
				solrLevel = java.util.logging.Level.FINER;
			if (solrLogLevelStr.equals("FINEST"))
				solrLevel = java.util.logging.Level.FINEST;
			if (solrLogLevelStr.equals("ALL"))
				solrLevel = java.util.logging.Level.ALL;
		}
		java.util.logging.Logger.getLogger("org.apache.solr").setLevel(solrLevel);
	}

	/**
	 * set log level for this MarcImporter class
	 * @param solrmarcLogLevelStr
	 * @param solrLogLevelStr we check if this is set
	 */
	private void setSolrMarcLogLevel(String solrmarcLogLevelStr, String solrLogLevelStr)
	{
		Level solrmarcLevel = Level.WARN;
		if (solrmarcLogLevelStr == null && solrLogLevelStr != null)
		{
			if (solrLogLevelStr.equals("OFF"))
				solrmarcLevel = Level.OFF;
			if (solrLogLevelStr.equals("FATAL"))
				solrmarcLevel = Level.FATAL;
			if (solrLogLevelStr.equals("WARN"))
				solrmarcLevel = Level.WARN;
			if (solrLogLevelStr.equals("INFO"))
				solrmarcLevel = Level.INFO;
			if (solrLogLevelStr.equals("DEBUG"))
				solrmarcLevel = Level.DEBUG;
			if (solrLogLevelStr.equals("ALL"))
				solrmarcLevel = Level.ALL;
		}
		logger.setLevel(solrmarcLevel);
	}

	/**
	 * Main program instantiation for doing the indexing
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		logger.info("Starting SolrMarc indexing.");

		MarcImporter importer = null;
		try
		{
			importer = new MarcImporter();
			importer.init(args);
		} 
		catch (IllegalArgumentException e)
		{
			logger.fatal(e.getMessage());
			System.err.println(e.getMessage());
			// e.printStackTrace();
			System.exit(1);
		} 
		catch (FileNotFoundException e)
		{
			logger.fatal(e.getMessage());
			System.err.println(e.getMessage());
//			e.printStackTrace();
			System.exit(1);
		}

		int exitCode = importer.handleAll();
		// System.clearProperty("marc.path");
		// System.clearProperty("marc.source");
		System.exit(exitCode);
	}
}
