package org.solrmarc.testUtils;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import java.util.*;
import static org.junit.Assert.fail;

/**
 * used to test what is written to log.  Shamelessly cribbed from
 *   http://giordano.scalzo.biz/2009/10/21/no-more-excuses-junit-testing-log-messages/
 *   http://stackoverflow.com/questions/1827677/how-to-do-a-junit-assert-on-a-message-in-a-logger
 * @author Naomi Dushay
 */
public class LoggerAppender4Testing extends AppenderSkeleton {
    private final List<LoggingEvent> logEventList = new ArrayList<LoggingEvent>();

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    protected void append(final LoggingEvent loggingEvent) {
        logEventList.add(loggingEvent);
    }

    @Override
    public void close() {
    }

    public List<LoggingEvent> getLog() {
        return new ArrayList<LoggingEvent>(logEventList);
    }

    /**
     * look through all the log messages for the expected string.
     * @param expected  we are looking for a log message that contains this string
     */
    public void assertLogContains(String expected)
    {
    	for (LoggingEvent logEvent : logEventList)
    	{
    		String actual = logEvent.getRenderedMessage();
    		if (actual.contains(expected))
    			return;
    	}
    	fail("Log did not contain expected string: " + expected);
    }

    /**
     * look through all the log messages for the expected string.
     * @param expLevel the expected level of the log message containing the string
     * @param expStr  we are looking for a log message that contains this string
     */
    public void assertLogContains(Level expLevel, String expStr)
    {
    	for (LoggingEvent logEvent : logEventList)
    	{
    		String actual = logEvent.getRenderedMessage();
    		if (actual.contains(expStr) && logEvent.level.equals(expLevel))
    			return;
    	}
    	fail("Log did not contain a " + expLevel.toString() + " message with expected string: " + expStr);
    }
}
