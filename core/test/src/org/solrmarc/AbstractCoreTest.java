package org.solrmarc;

import java.io.*;

/**
 * Methods and variables used to test core code
 * @author Naomi Dushay
 */
public abstract class AbstractCoreTest extends AbstractTest
{
	
// FIXME:  ensure log4j.properties is in bin

	protected String coreTestDir = "core" + File.separator + "test";

	// set up required properties when tests not invoked via Ant
	// hardcodings below are only used when the tests are invoked without the
	//  properties set (e.g. from eclipse)
	{
        String configPropFile = System.getProperty("test.config.file");
		if (configPropFile == null)
            System.setProperty("test.config.file", coreTestDir + File.separator + "test_core_config.properties");

        // used to find site translation_maps
		if (System.getProperty("solrmarc.site.path") == null)
            System.setProperty("solrmarc.site.path", coreTestDir);

		// used to find test data files
		testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
        {
            testDataParentPath = System.getProperty("test.data.parent.path");
            if (testDataParentPath == null)
                testDataParentPath = coreTestDir + File.separator + "data";
            System.setProperty("test.data.path", testDataParentPath);
        }
	}

}
