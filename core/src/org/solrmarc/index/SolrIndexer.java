package org.solrmarc.index;

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
import java.lang.reflect.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.Logger;
import org.marc4j.*;
import org.marc4j.marc.*;
import org.solrmarc.marc.MarcImporter;
import org.solrmarc.tools.*;

/**
 *
 * @author Robert Haschart, revised by Naomi Dushay
 *
 */
/**
 * @author rh9ec
 *
 */
public class SolrIndexer
{
    /** map: keys are solr field names, values inform how to get solr field values */
    private Map<String, String[]> fieldMap = null;

    /** map of translation maps.  keys are names of translation maps;
     *  values are the translation maps (hence, it's a map of maps) */
    private Map<String, Map<String, String>> transMapMap = null;

    /** map of custom methods.  keys are names of custom methods;
     *  values are the translation maps (hence, it's a map of maps) */
    private Map<String, Method> customMethodMap = null;

    /** map of custom mixin classes that contain additional indexing functions
     *  values are the translation maps (hence, it's a map of maps) */
    private Map<String, SolrIndexerMixin> customMixinMap = null;

    /** current datestamp for indexed solr document */
    private Date indexDate = null;

    /** list of path to look for property files in */
    protected String propertyFilePaths[];

    /** Error Handler used for reporting errors */
    protected ErrorHandler errors;

    // Initialize logging category
    protected static Logger logger = Logger.getLogger(MarcImporter.class.getName());

    /**
     * private constructor; initializes fieldMap, transMapMap and indexDate to
     * empty objects
     */
    private SolrIndexer()
    {
        fieldMap = new HashMap<String, String[]>();
        transMapMap = new HashMap<String, Map<String, String>>();
        customMethodMap = new HashMap<String, Method>();
        customMixinMap = new HashMap<String, SolrIndexerMixin>();
        indexDate = new Date();
    }

    /**
     * Constructor
     * @param indexingPropsFile the x_index.properties file mapping solr
     *  field names to values in the marc records
     * @param propertyDirs - array of directories holding properties files
     */
    public SolrIndexer(String indexingPropsFile, String propertyDirs[])
    {
        this();
        propertyFilePaths = propertyDirs;
        if (indexingPropsFile != null)
        {
            String indexingPropsFiles[] = indexingPropsFile.split("[;,]");
            for (String indexProps : indexingPropsFiles)
            {
                indexProps = indexProps.trim();
                Properties indexingProps = PropertiesUtils.loadProperties(propertyFilePaths, indexProps);
                fillMapFromProperties(indexingProps);
            }
        }
    }

    /* A constructor that takes an INDEXER Properties object, and a search
     * path (possibly empty). This is used by SolrMarc tests, may not
     * work as you might expect right in actual program use, not sure.
     *
     *  You can initialize an 'empty' indexex for unit testing like so:
     *  SolrIndexer.indexerFromProperties( new Properties(),
     *
     * @param indexingProperties a Properties mapping solr
     *  field names to values in the marc records
     * @param propertyDirs - array of directories holding properties files
     * UNTESTED SO COMMENTED OUT FOR THE FUTURE
     */
     public static SolrIndexer indexerFromProperties(Properties indexingProperties, String searchPath[])
     {
        SolrIndexer indexer = new SolrIndexer();
        indexer.propertyFilePaths = searchPath;
        indexer.fillMapFromProperties(indexingProperties);

        return indexer;
     }

     /* Attempt to reinitialize an indexer given an INDEXER Properties object,
      * and a search path (possibly empty). This is used by SolrMarc tests, may not
      * work as you might expect right in actual program use, not sure.
      *
      *  You can re-init an indexer for unit testing like so:
      *  indexer.reinitFromProperties(new Properties())
      *
      * @param indexingProperties a Properties mapping solr
      *  field names to values in the marc records
      * UNTESTED SO COMMENTED OUT FOR THE FUTURE
      */
      public void reinitFromProperties(Properties indexingProperties)
      {
         this.fieldMap.clear();
         this.transMapMap.clear();
         this.customMethodMap.clear();
         this.customMixinMap.clear();

         this.fillMapFromProperties(indexingProperties);
      }

    /**
     * Parse the properties file and load parameters into fieldMap. Also
     * populate transMapMap and indexDate
     * @param props _index.properties as Properties object
     */
    protected void fillMapFromProperties(Properties props)
    {
        Enumeration<?> en = props.propertyNames();

        while (en.hasMoreElements())
        {
            String propName = (String) en.nextElement();

            // ignore map, pattern_map; they are handled separately
            if (!propName.startsWith("map") &&
                !propName.startsWith("pattern_map"))
            {
                String propValue = props.getProperty(propName);
                String fieldDef[] = new String[4];
                fieldDef[0] = propName;
                fieldDef[3] = null;
                if (propValue.startsWith("\""))
                {
                    // value is a constant if it starts with a quote
                    fieldDef[1] = "constant";
                    fieldDef[2] = propValue.trim().replaceAll("\"", "");
                }
                else
                // not a constant
                {
                    // split it into two pieces at first comma or space
                    String values[] = propValue.split("[, ]+", 2);
                    if (values[0].startsWith("custom") || values[0].equals("customDeleteRecordIfFieldEmpty") ||
                        values[0].startsWith("script"))
                    {
                        fieldDef[1] = values[0];

                        // parse sections of custom value assignment line in
                        // _index.properties file
                        String lastValues[];
                        // get rid of empty parens
                        if (values[1].indexOf("()") != -1)
                            values[1] = values[1].replace("()", "");

                        // index of first open paren after custom method name
                        int parenIx = values[1].indexOf('(');

                        // index of first unescaped comma after method name
                        int commaIx = Utils.getIxUnescapedComma(values[1]);

                        if (parenIx != -1 && commaIx != -1 && parenIx < commaIx) {
                            // remainder should be split after close paren
                            // followed by comma (optional spaces in between)
                            lastValues = values[1].trim().split("\\) *,", 2);

                            // Reattach the closing parenthesis:
                            if (lastValues.length == 2)  lastValues[0] += ")";
                        }
                        else
                            // no parens - split comma preceded by optional spaces
                            lastValues = values[1].trim().split(" *,", 2);

                        fieldDef[2] = lastValues[0].trim();

                        fieldDef[3] = lastValues.length > 1 ? lastValues[1].trim() : null;
                        // is this a translation map?
                        if (fieldDef[3] != null && fieldDef[3].contains("map"))
                        {
                            try
                            {
                                fieldDef[3] = loadTranslationMap(props, fieldDef[3]);
                            }
                            catch (IllegalArgumentException e)
                            {
                                logger.error("Unable to find file containing specified translation map (" + fieldDef[3] + ")");
                                throw new IllegalArgumentException("Error: Problems reading specified translation map (" + fieldDef[3] + ")");
                            }
                        }
                    } // end custom
                    else if (values[0].equals("xml") ||
                             values[0].equals("raw") ||
                             values[0].equals("date") ||
                             values[0].equals("json") ||
                             values[0].equals("json2") ||
                             values[0].equals("index_date") ||
                             values[0].equals("era"))
                    {
                        fieldDef[1] = "std";
                        fieldDef[2] = values[0];
                        fieldDef[3] = values.length > 1 ? values[1].trim() : null;
                        // NOTE: assuming no translation map here
                        if (fieldDef[2].equals("era") && fieldDef[3] != null)
                        {
                            try
                            {
                                fieldDef[3] = loadTranslationMap(props, fieldDef[3]);
                            }
                            catch (IllegalArgumentException e)
                            {
                                logger.error("Unable to find file containing specified translation map (" + fieldDef[3] + ")");
                                throw new IllegalArgumentException("Error: Problems reading specified translation map (" + fieldDef[3] + ")");
                            }
                        }
                    }
                    else if (values[0].equalsIgnoreCase("FullRecordAsXML") ||
                             values[0].equalsIgnoreCase("FullRecordAsMARC") ||
                             values[0].equalsIgnoreCase("FullRecordAsJson") ||
                             values[0].equalsIgnoreCase("FullRecordAsJson2") ||
                             values[0].equalsIgnoreCase("FullRecordAsText") ||
                             values[0].equalsIgnoreCase("DateOfPublication") ||
                             values[0].equalsIgnoreCase("DateRecordIndexed"))
                    {
                        fieldDef[1] = "std";
                        fieldDef[2] = values[0];
                        fieldDef[3] = values.length > 1 ? values[1].trim() : null;
                        // NOTE: assuming no translation map here
                    }
                    else if (values.length == 1)
                    {
                        fieldDef[1] = "all";
                        fieldDef[2] = values[0];
                        fieldDef[3] = null;
                    }
                    else
                    // other cases of field definitions
                    {
                        String values2[] = values[1].trim().split("[ ]*,[ ]*", 2);
                        fieldDef[1] = "all";
                        if (values2[0].equals("first") ||
                                (values2.length > 1 && values2[1].equals("first")))
                            fieldDef[1] = "first";

                        if (values2[0].startsWith("join"))
                            fieldDef[1] = values2[0];

                        if ((values2.length > 1 && values2[1].startsWith("join")))
                            fieldDef[1] = values2[1];

                        if (values2[0].equalsIgnoreCase("DeleteRecordIfFieldEmpty") ||
                            (values2.length > 1 && values2[1].equalsIgnoreCase("DeleteRecordIfFieldEmpty")))
                            fieldDef[1] = "DeleteRecordIfFieldEmpty";

                        fieldDef[2] = values[0];
                        fieldDef[3] = null;

                        // might we have a translation map?
                        if (!values2[0].equals("all") &&
                            !values2[0].equals("first") &&
                            !values2[0].startsWith("join") &&
                            !values2[0].equalsIgnoreCase("DeleteRecordIfFieldEmpty"))
                        {
                            fieldDef[3] = values2[0].trim();
                            if (fieldDef[3] != null)
                            {
                                try
                                {
                                    fieldDef[3] = loadTranslationMap(props, fieldDef[3]);
                                }
                                catch (IllegalArgumentException e)
                                {
                                    logger.error("Unable to find file containing specified translation map (" + fieldDef[3] + ")");
                                    throw new IllegalArgumentException("Error: Problems reading specified translation map (" + fieldDef[3] + ")");
                                }
                            }
                        }
                    } // other cases of field definitions

                } // not a constant

                fieldMap.put(propName, fieldDef);

            } // if not map or pattern_map

        } // while enumerating through property names

        // verify that fieldMap is valid
        verifyCustomMethodsAndTransMaps();
    }

    /**
     * Verify that custom methods are available and translation maps are in
     * transMapMap
     */
    private void verifyCustomMethodsAndTransMaps()
    {
        for (String key : fieldMap.keySet())
        {
            String fieldMapVal[] = fieldMap.get(key);
            String indexType = fieldMapVal[1];
            String indexParm = fieldMapVal[2];
            String mapName = fieldMapVal[3];

            if (indexType.startsWith("custom"))
                verifyCustomMethodExists(indexType, indexParm);

            // check that translation maps are present in transMapMap
            if (mapName != null && findTranslationMap(mapName) == null)
            {
//                System.err.println("Error: Specified translation map (" + mapName + ") not found in properties file");
                logger.error("Specified translation map (" + mapName + ") not found in properties file");
                throw new IllegalArgumentException("Specified translation map (" + mapName + ") not found in properties file");
            }
        }
    }

    /**
     * verify that custom methods defined in the _index properties file are
     * present and accounted for
     * @param indexParm - name of custom function plus args
     */
    private void verifyCustomMethodExists(String indexType, String indexParm)
    {
        String className = null;
        Class<?> classToLookIn = this.getClass();
        try
        {
//            if (!indexType.equals("custom"))
//            {
//                className = null;
//            }
            if (indexType.matches("custom[(][a-zA-Z0-9.]+[)]"))
            {
                className = indexType.substring(7, indexType.length()-1).trim();
                if (customMixinMap.containsKey(className))
                    classToLookIn = customMixinMap.get(className).getClass();
                else
                {
                    classToLookIn = Class.forName(className);
                    if (SolrIndexerMixin.class.isAssignableFrom(classToLookIn))
                    {
                       Constructor<?> classConstructor = classToLookIn.getConstructor();
                       SolrIndexerMixin instance = (SolrIndexerMixin)classConstructor.newInstance((Object[])null);
                       instance.setMainIndexer(this);
                       customMixinMap.put(className, instance);
                    }
                }
            }
        }
        catch (ClassNotFoundException e)
        {
            logger.error("Unable to find indexer mixin class "  + className + " which should defing the custom indexing function " + indexParm);
            logger.debug(e.getCause());
            throw new IllegalArgumentException("Unable to find indexer mixin class "  + className + " which should defing the custom indexing function " + indexParm);
        }
        catch (SecurityException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            logger.error("Unable to find no argument constructor in indexer mixin class "  + className);
            logger.debug(e.getCause());
            throw new IllegalArgumentException("Unable to find no argument constructor in indexer mixin class "  + className);
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try
        {

            Method method = null;
            int parenIx = indexParm.indexOf("(");
            if (parenIx != -1)
            {
                String functionName = indexParm.substring(0, parenIx);
                String parmStr = indexParm.substring(parenIx + 1, indexParm.lastIndexOf(')'));
                // parameters are separated by unescaped commas
                String parms[] = parmStr.trim().split("[^\\\\],");
                int numparms = parms.length;
                Class<?> parmClasses[] = new Class<?>[numparms + 1];
                parmClasses[0] = Record.class;
                for (int i = 0; i < numparms; i++)
                {
                    parmClasses[i + 1] = String.class;
                }
                method = classToLookIn.getMethod(functionName, parmClasses);
                if (customMethodMap.containsKey(functionName))
                    customMethodMap.put(functionName, null);
                else
                    customMethodMap.put(functionName, method);
            }
            else
            {
                method = classToLookIn.getMethod(indexParm, new Class[] { Record.class });
                if (customMethodMap.containsKey(indexParm))
                    customMethodMap.put(indexParm, null);
                else
                    customMethodMap.put(indexParm, method);
            }
            Class<?> retval = method.getReturnType();
            // if (!method.isAccessible())
            // {
            //   System.err.println("Error: Unable to invoke custom indexing function "+indexParm);
            // valid = false;
            // }
            if (!(Set.class.isAssignableFrom(retval) || String.class.isAssignableFrom(retval) ||
                    Map.class.isAssignableFrom(retval)) )
            {
                logger.error("Error: Return type of custom indexing function " + indexParm + " must be String or Set<String> or Map<String, String>");
                throw new IllegalArgumentException("Error: Return type of custom indexing function " + indexParm + " must be String or Set<String> or Map<String, String>");
            }
        }
        catch (SecurityException e)
        {
            logger.error("Unable to invoke custom indexing function " + indexParm);
            logger.debug(e.getCause(), e);
            throw new IllegalArgumentException("Unable to invoke custom indexing function " + indexParm);
        }
        catch (NoSuchMethodException e)
        {
            logger.error("Unable to find custom indexing function " + indexParm);
            logger.debug(e.getCause());
            throw new IllegalArgumentException("Unable to find custom indexing function " + indexParm);
        }
        catch (IllegalArgumentException e)
        {
            logger.error("Unable to find custom indexing function " + indexParm);
            logger.debug(e.getCause());
            throw new IllegalArgumentException("Unable to find custom indexing function " + indexParm);
        }
    }

    /**
     * public interface callable from custom indexing scripts to
     * load the translation map into transMapMap
     * @param translationMapSpec the specification of a translation map -
     *   could be name of a _map.properties file, or some subset of entries in a
     *   _map.properties file
     * @return the name of the translation map to be used in a subsequent call to FindMap
     */
    public String loadTranslationMap(String translationMapSpec)
    {
        return(this.loadTranslationMap(null, translationMapSpec));
    }

    /**
     * load the translation map into transMapMap
     * @param indexProps _index.properties as Properties object
     * @param translationMapSpec the specification of a translation map -
     *   could be name of a _map.properties file, or something in _index
     *   properties ...
     * @return the name of the translation map
     */
    protected String loadTranslationMap(Properties indexProps, String translationMapSpec)
    {
        if (translationMapSpec.length() == 0)
            return null;

        String mapName = null;
        String mapKeyPrefix = null;
        if (translationMapSpec.startsWith("(")
                && translationMapSpec.endsWith(")"))
        {
            // translation map entries are in passed Properties object
            mapName = translationMapSpec.replaceAll("[\\(\\)]", "");
            mapKeyPrefix = mapName;
            loadTranslationMapValues(indexProps, mapName, mapKeyPrefix);
        }
        else
        {
            // translation map is a separate file
            String transMapFname = null;
            if (translationMapSpec.contains("(")
                    && translationMapSpec.endsWith(")"))
            {
                String mapSpec[] = translationMapSpec.split("(//s|[()])+");
                transMapFname = mapSpec[0];
                mapName = mapSpec[1];
                mapKeyPrefix = mapName;
            }
            else
            {
                transMapFname = translationMapSpec;
                mapName = translationMapSpec.replaceAll(".properties", "");
                mapKeyPrefix = "";
            }

            if (findTranslationMap(mapName) == null)
                loadTranslationMapValues(transMapFname, mapName, mapKeyPrefix);
        }

        return mapName;
    }

    /**
     * Load translation map into transMapMap.  Look for translation map in
     * site specific directory first; if not found, look in solrmarc top
     * directory
     * @param transMapName name of translation map file to load
     * @param mapName - the name of the Map to go in transMapMap (the key in transMapMap)
     * @param mapKeyPrefix - any prefix on individual Map keys (entries in the
     *   value in transMapMap)
     */
    private void loadTranslationMapValues(String transMapName, String mapName, String mapKeyPrefix)
    {
        Properties props = null;
        props = PropertiesUtils.loadProperties(propertyFilePaths, transMapName);
        logger.debug("Loading Custom Map: " + transMapName);
        loadTranslationMapValues(props, mapName, mapKeyPrefix);
    }

    /**
     * populate transMapMap
     * @param transProps - the translation map as a Properties object
     * @param mapName - the name of the Map to go in transMapMap (the key in transMapMap)
     * @param mapKeyPrefix - any prefix on individual Map keys (entries in the
     *   value in transMapMap)
     */
    private void loadTranslationMapValues(Properties transProps, String mapName, String mapKeyPrefix)
    {
        Enumeration<?> en = transProps.propertyNames();
        while (en.hasMoreElements())
        {
            String property = (String) en.nextElement();
            if (mapKeyPrefix.length() == 0 || property.startsWith(mapKeyPrefix))
            {
                String mapKey = property.substring(mapKeyPrefix.length());
                if (mapKey.startsWith("."))
                    mapKey = mapKey.substring(1);
                String value = transProps.getProperty(property);
                value = value.trim();
                if (value.equals("null"))
                    value = null;

                Map<String, String> valueMap;
                if (transMapMap.containsKey(mapName))
                    valueMap = transMapMap.get(mapName);
                else
                {
                    valueMap = new LinkedHashMap<String, String>();
                    transMapMap.put(mapName, valueMap);
                }

                valueMap.put(mapKey, value);
            }
        }
    }

    /**
     * Given a record, return a Map of solr fields (keys are field names, values
     * are an Object containing the values (a Set or a String)
     */
    public Map<String, Object> createFldNames2ValsMap(Record record)
    {
        return (createFldNames2ValsMap(record, null));
    }

    /**
     * Given a record, return a Map of solr fields (keys are field names, values
     * are an Object containing the values (a Set or a String))
     */
    public Map<String, Object> createFldNames2ValsMap(Record record, ErrorHandler errors)
    {
        this.errors = errors;
        perRecordInitMaster(record);
        Map<String, Object> fldNames2ValsMap = new HashMap<String, Object>();

        for (String key : fieldMap.keySet())
        {
            String fieldVal[] = fieldMap.get(key);
            String indexField = fieldVal[0];
            String indexType = fieldVal[1];
            String indexParm = fieldVal[2];
            String mapName = fieldVal[3];

            if (indexType.equals("constant"))
            {
                if (indexParm.contains("|"))
                {
                    String parts[] = indexParm.split("[|]");
                    Set<String> result = new LinkedHashSet<String>();
                    result.addAll(Arrays.asList(parts));
                    // if a zero length string appears, remove it
                    result.remove("");
                    addFieldsToMap(fldNames2ValsMap, indexField, null, result);
                }
                else
                    addFieldToMap(fldNames2ValsMap, indexField, indexParm);
            }
            else if (indexType.equals("first"))
                addFieldToMap(fldNames2ValsMap, indexField, getFirstFieldVal(record, mapName, indexParm));
            else if (indexType.equals("all"))
                addFieldsToMap(fldNames2ValsMap, indexField, mapName, MarcUtils.getFieldList(record, indexParm));
            else if (indexType.equals("DeleteRecordIfFieldEmpty"))
            {
                Set<String> fields = MarcUtils.getFieldList(record, indexParm);
                if (mapName != null && findTranslationMap(mapName) != null)
                    fields = Utils.remap(fields, findTranslationMap(mapName), true);

                if (fields.size() != 0)
                    addFieldsToMap(fldNames2ValsMap, indexField, null, fields);
                else  // no entries produced for field => generate no record in Solr
                    throw new SolrMarcIndexerException(SolrMarcIndexerException.DELETE,
                                                    "Index specification: "+ indexField +" says this record should be deleted.");
            }
            else if (indexType.startsWith("join"))
            {
                String joinChar = " ";
                if (indexType.contains("(") && indexType.endsWith(")"))
                    joinChar = indexType.replace("join(", "").replace(")", "");
                addFieldToMap(fldNames2ValsMap, indexField, MarcUtils.getFieldVals(record, indexParm, joinChar));
            }
            else if (indexType.equals("std"))
            {
                if (indexParm.equals("era"))
                    addFieldsToMap(fldNames2ValsMap, indexField, mapName, MarcUtils.getEra(record));
                else
                    addFieldToMap(fldNames2ValsMap, indexField, getStd(record, indexParm));
            }
            else if (indexType.startsWith("custom"))
            {
                try {
                    handleCustom(fldNames2ValsMap, indexType, indexField, mapName, record, indexParm);
                }
                catch(SolrMarcIndexerException e)
                {
                    String recCntlNum = null;
                    try {
                        recCntlNum = record.getControlNumber();
                    }
                    catch (NullPointerException npe) { /* ignore */ }

                    if (e.getLevel() == SolrMarcIndexerException.DELETE)
                    {
                        throw new SolrMarcIndexerException(SolrMarcIndexerException.DELETE,
                                "Record " + (recCntlNum != null ? recCntlNum : "") + " purposely not indexed because " + key + " field is empty");
//                      logger.error("Record " + (recCntlNum != null ? recCntlNum : "") + " not indexed because " + key + " field is empty -- " + e.getMessage(), e);
                    }
                    else
                    {
                        logger.error("Unable to index record " + (recCntlNum != null ? recCntlNum : "") + " due to field " + key + " -- " + e.getMessage(), e);
                        throw(e);
                    }
                }
            }
        }
        this.errors = null;
        return fldNames2ValsMap;
    }

    /**
     * This routine CANNOT be overridden in a sub-class.  Its is called to perform some processing that needs
     * to be done once for each record, and which may be needed by several indexing specifications.  Basically all
     * this method does is call the override-able method perRecordInit for the SolrIndexer class, and the perRecordInit
     * methods of any SolrIndexerMixin that are in use.
     *
     * @param record -  The MARC record that is being indexed.
     */
    private final void perRecordInitMaster(Record record)
    {
        perRecordInit(record);
        for (String key : customMixinMap.keySet())
        {
            SolrIndexerMixin mixin = customMixinMap.get(key);
            mixin.perRecordInit(record);
        }
    }

    /**
     * This routine can be overridden in a sub-class to perform some processing that needs to be done once
     * for each record, and which may be needed by several indexing specifications, especially custom methods.
     * The default version does nothing.
     *
     * @param record -  The MARC record that is being indexed.
     */
    protected void perRecordInit(Record record)
    {
    }

    /**
     * Calling a custom method defined in a user-supplied custom subclass of SolrIndexer,
     * do the processing indicated by a custom function, putting the solr field
     * name and value into the indexMap parameter
     *
     * @param indexMap - The map contain the solr index record that is being constructed for this MARC record.
     * @param indexType - if set to "customDeleteRecordIfFieldEmpty", then the
     *                    solr record should be deleted if no value
     *                    is generated by this custom indexing method.
     * @param indexField - The name of the field to be added to the solr index record.  Note that
     *                     in that case of a custom index method that returns a Map, the keys of the map
     *                     define the names of the fields to be added, and this value is then simply a dummy.
     * @param mapName - The name (or file and name) of a translation map to use to convert
     *                  the data in the specified fields of the MARC record to the desired values
     *                  to be included in the Solr index record.  (If mapName is null, the values
     *                  in the record will be returned as-is.)
     * @param record -  The MARC record that is being indexed.
     * @param indexParm - contains the name of the custom method to invoke, as well as the
     *                    additional parameters to pass to that method.
     */
    private void handleCustom(Map<String, Object> indexMap, String indexType, String indexField,
    							String mapName, Record record, String indexParm)
    		throws SolrMarcIndexerException
    {
        Object retval = null;
        Class<?> returnType = null;

        // grab the record id in case we want to use it in an exception message
        String recCntlNum = null;
        try {
        	recCntlNum = record.getControlNumber();
        }
        catch (NullPointerException npe) { /* ignore as this is for error msgs only*/ }

        String className = null;
        Class<?> classThatContainsMethod = this.getClass();
        Object objectThatContainsMethod = this;
        try
        {
            if (indexType.matches("custom[(][a-zA-Z0-9.]+[)]"))
            {
                className = indexType.substring(7, indexType.length()-1).trim();
                if (customMixinMap.containsKey(className))
                {
                    objectThatContainsMethod = customMixinMap.get(className);
                    classThatContainsMethod = objectThatContainsMethod.getClass();
                }
            }

            Method method;
            if (indexParm.indexOf("(") != -1)
            {
                String functionName = indexParm.substring(0, indexParm.indexOf('('));
                String parmStr = indexParm.substring(indexParm.indexOf('(')+1, indexParm.lastIndexOf(')'));
                // parameters are separated by unescaped commas
                String parms[] = parmStr.trim().split("(?<=[^\\\\]),");
                int numparms = parms.length;
                Class parmClasses[] = new Class[numparms + 1];
                parmClasses[0] = Record.class;
                Object objParms[] = new Object[numparms + 1];
                objParms[0] = record;
                for (int i = 0; i < numparms; i++)
                {
                    parmClasses[i + 1] = String.class;
                    objParms[i + 1] = dequote(parms[i].trim());
                }
             // NRD 2010-06-03  can't do this because have functionName with two signatures!  (getAllAlphaSubfields)
//              method = customMethodMap.get(functionName);
                method = getClass().getMethod(functionName, parmClasses);
                if (method == null)
                    method = classThatContainsMethod.getMethod(functionName, parmClasses);
                returnType = method.getReturnType();
                retval = method.invoke(objectThatContainsMethod, objParms);
            }
            else
            {
                method = customMethodMap.get(indexParm);
                if (method == null)
                    method = classThatContainsMethod.getMethod(indexParm, new Class[]{Record.class});
                returnType = method.getReturnType();
                retval = method.invoke(objectThatContainsMethod, new Object[] { record });
            }
        }
        catch (SecurityException e)
        {
            // e.printStackTrace();
            logger.error("Error while indexing " + indexField + " for record " + (recCntlNum != null ? recCntlNum : "") + " -- " + e.getCause());
        }
        catch (NoSuchMethodException e)
        {
            // e.printStackTrace();
            logger.error("Error while indexing " + indexField + " for record " + (recCntlNum != null ? recCntlNum : "") + " -- " + e.getCause());
        }
        catch (IllegalArgumentException e)
        {
            // e.printStackTrace();
            logger.error("Error while indexing " + indexField + " for record " + (recCntlNum != null ? recCntlNum : "") + " -- " + e.getCause());
        }
        catch (IllegalAccessException e)
        {
            // e.printStackTrace();
            logger.error("Error while indexing " + indexField + " for record " + (recCntlNum != null ? recCntlNum : "") + " -- " + e.getCause());
        }
        catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof SolrMarcIndexerException)
                throw((SolrMarcIndexerException)e.getTargetException());

            e.printStackTrace();   // DEBUG
            logger.error("Error while indexing " + indexField + " for record " + (recCntlNum != null ? recCntlNum : "") + " -- " + e.getCause());
        }

        boolean deleteIfEmpty = false;
        if (indexType.equals("customDeleteRecordIfFieldEmpty"))
            deleteIfEmpty = true;

        boolean stopOrDelete = finishCustomMethod(indexMap, indexField, mapName, returnType, retval, deleteIfEmpty);

        if (stopOrDelete == true)
        	throw new SolrMarcIndexerException(SolrMarcIndexerException.DELETE);
    }

    /**
     * Finish up the processing for a custom indexing function
     * @param indexMap - The map contain the solr index record that is being constructed for this MARC record.
     * @param indexField - The name of the field to be added to the solr index record.  Note that
     *                     in that case of a custom index method that returns a Map, the keys of the map
     *                     define the names of the fields to be added, and this value is then simply a dummy.
     * @param mapName - The name (or file and name) of a translation map to use to convert
     *                  the data in the specified fields of the MARC record to the desired values
     *                  to be included in the Solr index record.  (If mapName is null, the values
     *                  in the record will be returned as-is.)
     * @param returnType - The Class of the return type of the custom indexing function or the
     *                     custom BeanShell script method, the valid expected types are String, Set<String>, or Map<String, Object>
     * @param retval - The value that was returned from the custom indexing function or the
     *                 custom BeanShell script method
     * @param deleteIfEmpty - Indicates whether the the solr record should be deleted if no value
     *                        was generated.
     * @return returns true if the indexing process should stop and the solr record should be deleted.
     */
    private boolean finishCustomMethod(Map<String, Object> indexMap, String indexField, String mapName,
                                         Class<?> returnType, Object retval, boolean deleteIfEmpty)
    {
        if (returnType == null || retval == null)
            return(deleteIfEmpty);
        else if (returnType.isAssignableFrom(Map.class))
        {
            if (deleteIfEmpty && ((Map<String, String>) retval).size() == 0)
            	return (true);
            indexMap.putAll((Map<String, String>) retval);
        }
        else if (returnType.isAssignableFrom(Set.class))
        {
            Set<String> fields = (Set<String>) retval;
            if (mapName != null && findTranslationMap(mapName) != null)
                fields = Utils.remap(fields, findTranslationMap(mapName), true);
            if (deleteIfEmpty && fields.size()== 0)
            	return (true);
            addFieldsToMap(indexMap, indexField, null, fields);
        }
        else if (returnType.isAssignableFrom(String.class))
        {
            String field = (String) retval;
            if (mapName != null && findTranslationMap(mapName) != null)
                field = Utils.remap(field, findTranslationMap(mapName), true);
            addFieldToMap(indexMap, indexField, null, field);
        }
        return false;
    }

    /**
     * if the first and last characters of the string are quote marks ("), then
     * delete them.
     */
    private String dequote(String str)
    {
        if (str.length() >= 2 && str.charAt(0) == '"' && str.charAt(str.length()-1) == '"')
            return str.substring(1, str.length() - 1);

        return str;
    }

    /**
     * get values that don't require parsing specified record fields:
     *   raw, xml, date, index_date ...
     * @param indexParm - what type of value to return
     */
    private String getStd(Record record, String indexParm)
    {
        if (indexParm.equals("raw")
                || indexParm.equalsIgnoreCase("FullRecordAsMARC"))
            return MarcUtils.getRecordAsBinaryStr(record);
        else if (indexParm.equals("xml")
                || indexParm.equalsIgnoreCase("FullRecordAsXML"))
            return MarcUtils.getRecordAsMarcXmlStr(record);
        else if (indexParm.equals("json")
                || indexParm.equalsIgnoreCase("FullRecordAsJSON"))
            return MarcUtils.getRecordAsJsonStr(record, true);
        else if (indexParm.equals("json2")
                || indexParm.equalsIgnoreCase("FullRecordAsJSON2"))
            return MarcUtils.getRecordAsJsonStr(record, false);
        else if (indexParm.equals("xml")
                || indexParm.equalsIgnoreCase("FullRecordAsText"))
            return (record.toString().replaceAll("\n", "<br/>"));
        else if (indexParm.equals("date")
                || indexParm.equalsIgnoreCase("DateOfPublication"))
            return MarcUtils.getDate(record);
        else if (indexParm.equals("index_date")
                || indexParm.equalsIgnoreCase("DateRecordIndexed"))
            return getCurrentDate();
        return null;
    }

    /**
     * Add a field-value pair to the fldNames2ValsMap representation of a solr doc.
     *  The value will be "translated" per the translation map indicated.
     * @param fldNames2ValsMap - the mapping of solr doc field names to values
     * @param ixFldName - the name of the field to add to the solr doc
     * @param transMapName - the name of a translation map for the field value, or null
     * @param fieldVal - the (untranslated) field value to add to the solr doc field
     */
    protected void addFieldToMap(Map<String, Object> fldNames2ValsMap, String ixFldName, String transMapName, String fieldVal)
    {
        if (transMapName != null && findTranslationMap(transMapName) != null)
            fieldVal = Utils.remap(fieldVal, findTranslationMap(transMapName), true);

        if (fieldVal != null && fieldVal.length() > 0)
            fldNames2ValsMap.put(ixFldName, fieldVal);
    }

    /**
     * Add a field-value pair to the fldNames2ValsMap representation of a solr doc.
     * @param fldNames2ValsMap - the mapping of solr doc field names to values
     * @param ixFldName - the name of the field to add to the solr doc
     * @param fieldVal - the (untranslated) field value to add to the solr doc field
     */
    protected void addFieldToMap(Map<String, Object> fldNames2ValsMap, String ixFldName, String fieldVal)
    {
        addFieldToMap(fldNames2ValsMap, ixFldName, null, fieldVal);
    }

    /**
     * Add a field-value pair to the fldNames2ValsMap representation of a solr doc for
     *  each value present in the "fieldVals" parameter.
     *  The values will be "translated" per the translation map indicated.
     * @param fldNames2ValsMap - the mapping of solr doc field names to values
     * @param ixFldName - the name of the field to add to the solr doc
     * @param transMapName - the name of a translation map for the field value, or null
     * @param fieldVals - a set of (untranslated) field values to be assigned to the solr doc field
     */
    protected void addFieldsToMap(Map<String, Object> fldNames2ValsMap, String ixFldName, String transMapName, Set<String> fieldVals)
    {
        if (transMapName != null && findTranslationMap(transMapName) != null)
            fieldVals = Utils.remap(fieldVals, findTranslationMap(transMapName), true);

        if (!fieldVals.isEmpty())
        {
            if (fieldVals.size() == 1)
            {
                String value = fieldVals.iterator().next();
                fldNames2ValsMap.put(ixFldName, value);
            }
            else
                fldNames2ValsMap.put(ixFldName, fieldVals);
        }
    }

    /****
     * Intended to be called as a custom method from an indexing properties
     * file: some_field = custom, getWithOptions( marcFieldSpec, options)
     *
     *  marcFieldSpec is the ordinary field spec for SolrIndexer, eg
     *  "245a:500az" etc.
     *
     *  Options is a colon-seperated list of one or more of the following:
     *
     *  first  => take first value found only
     *  includeLinkedFields => include all 880 linked fields for spec, with getLinkedField
     *  removeTrailingPunct => Remove trailing punctuation from all values, using Util.cleanData.
     * map=mapName  =>   A map to send all values through, using the same semantics as the other SolrIndexer mapping functions. Eg, "map=somefile.properties" or "map=map_defined_in_index_properties". Utils.remap is used to do the mapping.
     * "default=Default Value" => A default value to use whenever there are otherwise no values found for this index entry. Default value can contain spaces, but not colons (since we parse colon-seperated). If a map is used, and there are marc fields matching the spec,  this default value will be used ONLY if the map returns _no_ values (map is not set to pass through, map does not have default value). If there are no marc fields matching the spec, the default value will always be used.
     * combineSubfields=joinStr => combine all subfields in the same marc field in one Solr field, joined by the joinStr. Will use getAllSubfields to fetch, instead of getFieldList. joinStr can include spaces but not colons. If you want it to end in a space, and it's the last option in an options list, just add a terminal colon.   "combineSubfields= :"
     *
     * eg:  some_field = custom, getWithOptions(245a:500a, includeLinkedFields:removeTrailingPunct:default=Unknown)
     *
     */
    public Set<String> getWithOptions(Record record, String tagStr, String optionStr)
    {
      //default options
      boolean first = false;
      boolean includeLinked = false;
      boolean removeTrailingPunct = false;
      String strDefault = null;
      String mapName = null;
      String combineSubfieldsJoin = null;
      //specified options
      String[] options = optionStr.split(":");
      for (int i = 0; i < options.length; i++)
      {
          String option = options[i];
          if (option.equals("first"))
              first = true;
          else if (option.equals("includedLinkedFields"))
              includeLinked = true;
          else if (option.equals("removeTrailingPunct"))
              removeTrailingPunct = true;
          else if (option.length() > 4 && option.substring(0, 4).equals("map="))
              mapName = option.substring(4, option.length());
          else if (option.length() > 8 && option.substring(0, 8).equals("default="))
              strDefault = option.substring(8, option.length());
          else if (option.length() > 17 && option.substring(0, 17).equals("combineSubfields="))
          {
              combineSubfieldsJoin = option.substring(17, option.length());
              if (combineSubfieldsJoin.length() > 2 && combineSubfieldsJoin.startsWith("\"") && combineSubfieldsJoin.endsWith("\""))
                  combineSubfieldsJoin = combineSubfieldsJoin.substring(1, combineSubfieldsJoin.length() - 1);
          }
      }


       // While getFieldList and similar methods are
       // declared as returning only a Set, for
       // 'first' functionality to work, it better be a LinkedHashSet
       // with predictable order! That we can't really guarantee this
       // seems to be a flaw in the jdk library designs, there's no
       // interface for predictable-order-set. oh well.

       // We need to use two different methods depending on if we're doing
      // a combineSubfieldsJoin or not. TODO, would be nice to refactor
      // SolrIndexer to make this more straightforward.

      Set<String> results = null;
      if ( combineSubfieldsJoin == null )
        results = MarcUtils.getFieldList(record, tagStr);
      else
        //combine subfields!
        results = MarcUtils.getAllSubfields(record, tagStr, combineSubfieldsJoin);

      //linked fields?
      if (includeLinked)
    	  results.addAll(MarcUtils.getLinkedField(record, tagStr));

       //first only?
       if ( results.size() > 0 && first)
       {
         Set newResults = new LinkedHashSet<String>();
         newResults.add(results.iterator().next());
         results = newResults;
       }

       //Map?
       if (mapName != null )
       {
         //TODO: It's somewhat inefficient to call loadTranslationMap
         // when it may already have beenloaded. But it's the only
         // way to get the internal map name we need for later call
         // to findMap, to pass to remap. Code in rest of this class
         // should be refactored to make this a lot more reasonable.
         String internalMapName = loadTranslationMap(mapName);
         results = Utils.remap(results, findTranslationMap(internalMapName), true);
       }

       //removeTrailingPunct?
       if ( removeTrailingPunct )
       {
         Set newResults = new LinkedHashSet<String>();
         for (String s : results)
            newResults.add(Utils.cleanData(s));
         results = newResults;
       }

       //default value?
       if ( (results.size() == 0) && (strDefault != null) )
         results.add(strDefault);

       return results;
    }


    /**
     * Get the first field value, which is mapped to another value. If there is
     * no mapping for the value, use the mapping for the empty key, if it
     * exists, o.w., use the mapping for the __DEFAULT key, if it exists.
     * @param record - the marc record object
     * @param mapName - name of translation map to use to xform values
     * @param tagStr - which field(s)/subfield(s) to use
     * @return first value as a string
     */
    public String getFirstFieldVal(Record record, String mapName, String tagStr)
    {
        Set<String> result = MarcUtils.getFieldList(record, tagStr);
        if (mapName != null && findTranslationMap(mapName) != null)
        {
            result = Utils.remap(result, findTranslationMap(mapName), false);
            if (findTranslationMap(mapName).containsKey(""))
            {
                result.add(findTranslationMap(mapName).get(""));
            }
            if (findTranslationMap(mapName).containsKey("__DEFAULT"))
            {
                result.add(findTranslationMap(mapName).get("__DEFAULT"));
            }
        }
        Iterator<String> iter = result.iterator();
        if (iter.hasNext())
            return iter.next();
        else
            return null;
    }

    /**
     * Return the index datestamp as a string
     */
    public String getCurrentDate()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        return df.format(indexDate);
    }


    /**
     * Get the appropriate Map object from populated transMapMap
     * @param mapName the name of the translation map to find
     * @return populated Map object
     */
    public Map<String, String> findTranslationMap(String mapName)
    {
        if (mapName.startsWith("pattern_map:"))
            mapName = mapName.substring("pattern_map:".length());

        if (transMapMap.containsKey(mapName))
            return (transMapMap.get(mapName));

        return null;
    }


    /**
     * Custom routine to process a field specification very similar to the way
     *  that specifying "first" works  ie. create a list of responses based on
     *  the field spec, but only return one of them. (In this case, the longest of them)
     *  Additionally this function demonstrates how custom indexing functions can
     *  encounter errors, and continue indexing, but record the error in the generated index map.
     *
     * @param record -  marc record object
     * @param fieldSpec - the marc field(s)/subfield(s)
     * @param flagExtraEntries - boolean string - if "true" and more than one index entry
     *            would be returned, flag the condition as an error, and continue
     * @return a string containing longest entry matching the provided field spec.
     */
    public String getSingleIndexEntry(final Record record, String fieldSpec, String flagExtraEntries)
    {
        Set<String> set = MarcUtils.getFieldList(record, fieldSpec);
        if (set.size() == 0)
        {
            return (null);
        }
        else if (set.size() == 1)
        {
            return (set.toArray(new String[0])[0]);
        }
        else
        {
            String longest = "";
            for (String item : set)
            {
                if (item.length() > longest.length())
                {
                    longest = item;
                }
            }
            if (flagExtraEntries.equalsIgnoreCase("true") && errors != null)
            {
                for (String item : set)
                {
                    if (!item.equals(longest))
                    {
                        errors.addError(record.getControlNumber(), fieldSpec.substring(0,3), fieldSpec.substring(3),
                                        ErrorHandler.MINOR_ERROR, "Multiple fields found for Field that expects only one occurance");
                    }
                }
            }
            return (longest);
        }
    }


	/**
	 * get the era field values from 045a as a Set of Strings
	 */
	public Set<String> getEra(Record record)
	{
		return MarcUtils.getEra(record);
	}


	/**
	 * Get the 245a (and 245b, if it exists, concatenated with a space between
	 *  the two subfield values), with trailing punctuation removed.
	 *    See org.solrmarc.tools.Utils.cleanData() for details on the
	 *     punctuation removal
	 * @param record - the marc record object
	 * @return 245a, b, and k values concatenated in order found, with trailing punct removed. Returns empty string if no suitable title found.
	 */
	public static String getTitle(Record record)
	{
		return MarcUtils.getTitle(record);
	}


	/**
	 * Get the title (245ab) from a record, without non-filing chars as
	 * specified in 245 2nd indicator, and lowercased.
	 * @param record - the marc record object
	 * @return 245a and 245b values concatenated, with trailing punct removed,
	 *         and with non-filing characters omitted. Null returned if no
	 *         title can be found.
	 */
	public static String getSortableTitle(Record record)
	{
		return MarcUtils.getSortableTitle(record);
	}


	/**
	 * returns string for author sort:  a string containing
	 *  1. the main entry author, if there is one
	 *  2. the main entry uniform title (240), if there is one - not including
	 *    non-filing chars as noted in 2nd indicator
	 * followed by
	 *  3.  the 245 title, not including non-filing chars as noted in ind 2
	 */
	public static String getSortableAuthor(final Record record)
	{
		return MarcUtils.getSortableAuthor(record);
	}


	/**
	 * Return the date in 260c as a string
	 * @param record - the marc record object
	 * @return 260c, "cleaned" per org.solrmarc.tools.Utils.cleanDate()
	 */
	public static String getDate(Record record)
	{
		return MarcUtils.getDate(record);
	}


	/**
	 * Stub (to be overridden) default simply calls getDate()
	 * @param record - the marc record object
	 * @return 260c, "cleaned" per org.solrmarc.tools.Utils.cleanDate()
	 */
	public static String getPublicationDate(final Record record)
	{
		return MarcUtils.getPublicationDate(record);
	}


    /**
     * returns the URLs for the full text of a resource described by the record
     *
     * @param record
     * @return Set of Strings containing full text urls, or empty set if none
     */
    public Set<String> getFullTextUrls(final Record record)
    {
    	return MarcUtils.getFullTextUrls(record);
    }

	/**
	 * returns the URLs for supplementary information (rather than fulltext)
	 *
	 * @param record
	 * @return Set of Strings containing supplementary urls, or empty string if
	 *         none
	 */
	public static Set<String> getSupplUrls(final Record record)
	{
		return MarcUtils.getSupplUrls(record);
	}

	/**
	 * remove trailing punctuation (default trailing characters to be removed)
	 *    See org.solrmarc.tools.Utils.cleanData() for details on the
	 *     punctuation removal
	 * @param record marc record object
	 * @param fieldSpec - the field to have trailing punctuation removed
	 * @return Set of strings containing the field values with trailing
	 *         punctuation removed
	 */
	public static Set<String> removeTrailingPunct(Record record, String fieldSpec)
	{
		return MarcUtils.removeTrailingPunct(record, fieldSpec);
	}


	/**
	 * extract all the subfields requested in requested marc fields. Each
	 * instance of each marc field will be put in a separate result (but the
	 * subfields will be concatenated into a single value for each marc field)
	 *
	 * @param record - marc record object
	 * @param fieldSpec -
	 *            the desired marc fields and subfields as given in the
	 *            xxx_index.properties file
	 * @param separator -
	 *            the character to use between subfield values in the solr field
	 *            contents
	 * @return Set of values (as strings) for solr field
	 */
	public static Set<String> getAllSubfields(final Record record, String fieldSpec, String separator)
	{
		return MarcUtils.getAllSubfields(record, fieldSpec, separator);
	}


	/**
	 * For each occurrence of a marc field in the fieldSpec list, extract the
	 * contents of all alphabetical subfields, concatenate them with a space
	 * separator and add the string to the result set. Each instance of each
	 * marc field will be put in a separate result.
	 *
	 * @param record - the marc record
	 * @param fieldSpec -
	 *            the marc fields (e.g. 600:655) in which we will grab the
	 *            alphabetic subfield contents for the result set. The field may
	 *            not be a control field (must be 010 or greater)
	 * @return a set of strings, where each string is the concatenated values of
	 *         all the alphabetic subfields.
	 */
	public static Set<String> getAllAlphaSubfields(final Record record, String fieldSpec)
	{
		return MarcUtils.getAllAlphaSubfields(record, fieldSpec);
	}


	/**
	 * For each occurrence of a marc field in the fieldSpec list, extract the
	 * contents of all alphabetical subfields, concatenate them with a space
	 * separator and add the string to the result set, handling multiple
	 * occurrences as indicated
	 *
	 * @param record - the marc record
	 * @param fieldSpec -
	 *            the marc fields (e.g. 600:655) in which we will grab the
	 *            alphabetic subfield contents for the result set. The field may
	 *            not be a control field (must be 010 or greater)
	 * @param multOccurs -
	 *            "first", "join" or "all" indicating how to handle multiple
	 *            occurrences of field values
	 * @return a set of strings, where each string is the concatenated values of
	 *         all the alphabetic subfields.
	 */
	public static final Set<String> getAllAlphaSubfields(final Record record, String fieldSpec, String multOccurs)
	{
		return MarcUtils.getAllAlphaSubfields(record, fieldSpec, multOccurs);
	}


	/**
	 * For each occurrence of a marc field in the fieldSpec list, extract the
	 * contents of all subfields except the ones specified, concatenate the
	 * subfield contents with a space separator and add the string to the result
	 * set.
	 *
	 * @param record - the marc record
	 * @param fieldSpec -
	 *            the marc fields (e.g. 600:655) in which we will grab the
	 *            alphabetic subfield contents for the result set. The field may
	 *            not be a control field (must be 010 or greater)
	 * @return a set of strings, where each string is the concatenated values of
	 *         all the alphabetic subfields.
	 */
	public static Set<String> getAllAlphaExcept(final Record record, String fieldSpec)
	{
		return MarcUtils.getAllAlphaExcept(record, fieldSpec);
	}


	/**
	 * Given a fieldSpec, get any linked 880 fields and include the appropriate
	 * subfields as a String value in the result set.
	 *
	 * @param record - marc record object
	 * @param fieldSpec -
	 *            the marc field(s)/subfield(s) for which 880s are sought.
	 *            Separator of colon indicates a separate value, rather than
	 *            concatenation. 008[5-7] denotes bytes 5-7 of the linked 008
	 *            field (0 based counting) 100[a-cf-z] denotes the bracket
	 *            pattern is a regular expression indicating which subfields to
	 *            include from the linked 880. Note: if the characters in the
	 *            brackets are digits, it will be interpreted as particular
	 *            bytes, NOT a pattern 100abcd denotes subfields a, b, c, d are
	 *            desired from the linked 880.
	 *
	 * @return set of Strings containing the values of the designated 880
	 *         field(s)/subfield(s)
	 */
	public static Set<String> getLinkedField(final Record record, String fieldSpec)
	{
		// NOTE: not sure we need this here, or just in MarcUtils
		return MarcUtils.getLinkedField(record, fieldSpec);
	}


	/**
	 * Given a tag for a field, and a list (or regex) of one or more subfields
	 * get any linked 880 fields and include the appropriate subfields as a String value
	 * in the result set.
	 *
	 * @param record - marc record object
	 * @param tag -  the marc field for which 880s are sought.
	 * @param subfield -
	 *           The subfield(s) within the 880 linked field that should be returned
	 *            [a-cf-z] denotes the bracket pattern is a regular expression indicating
	 *            which subfields to include from the linked 880. Note: if the characters
	 *            in the brackets are digits, it will be interpreted as particular
	 *            bytes, NOT a pattern 100abcd denotes subfields a, b, c, d are
	 *            desired from the linked 880.
	 * @param separator - the separator string to insert between subfield items (if null, a " " will be used)
	 *
	 * @return set of Strings containing the values of the designated 880 field(s)/subfield(s)
	 */
	public static Set<String> getLinkedFieldValue(final Record record, String tag, String subfield, String separator)
	{
		// NOTE: not sure we need this here, or just in MarcUtils
		return MarcUtils.getLinkedFieldValue(record, tag, subfield, separator);
	}


	/**
	 * Given a fieldSpec, get the field(s)/subfield(s) values, PLUS any linked
	 * 880 fields and return these values as a set.
	 * @param record marc record object
	 * @param fieldSpec - the marc field(s)/subfield(s)
	 * @return set of Strings containing the values of the indicated field(s)/
	 *         subfields(s) plus linked 880 field(s)/subfield(s)
	 */
	public static Set<String> getLinkedFieldCombined(final Record record, String fieldSpec)
	{
		// NOTE: not sure we need this here, or just in MarcUtils
		return MarcUtils.getLinkedFieldCombined(record, fieldSpec);
	}


	/**
	 * Loops through all datafields and creates a field for "all fields"
	 * searching. Shameless stolen from Vufind Indexer Custom Code
	 *
	 * @param record - marc record object
	 * @param lowerBoundStr -
	 *            the "lowest" marc field to include (e.g. 100). defaults to 100
	 *            if value passed doesn't parse as an integer
	 * @param upperBoundStr -
	 *            one more than the "highest" marc field to include (e.g. 900
	 *            will include up to 899). Defaults to 900 if value passed
	 *            doesn't parse as an integer
	 * @return a string containing ALL subfields of ALL marc fields within the
	 *         range indicated by the bound string arguments.
	 */
	public static String getAllSearchableFields(final Record record, String lowerBoundStr, String upperBoundStr)
	{
		return MarcUtils.getAllSearchableFields(record, lowerBoundStr, upperBoundStr);
	}


    public ErrorHandler getErrorHandler()
    {
        return errors;
    }

}
