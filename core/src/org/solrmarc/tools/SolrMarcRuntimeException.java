package org.solrmarc.tools;

/**
 * Allows SolrMarc to throw its own RuntimeException to stop execution
 * @author Naomi Dushay
 */
public class SolrMarcRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = 7494518253370528093L;

	/**
	 * SolrMarc needs to stop
	 */
	public SolrMarcRuntimeException()
	{
		super();
	}

	/**
	 * SolrMarc needs to stop
	 * @param message
	 * @param cause
	 */
	public SolrMarcRuntimeException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * SolrMarc needs to stop
	 * @param message
	 */
	public SolrMarcRuntimeException(String message)
	{
		super(message);
	}

	/**
	 * SolrMarc needs to stop
	 * @param cause
	 */
	public SolrMarcRuntimeException(Throwable cause)
	{
		super(cause);
	}

	
}
