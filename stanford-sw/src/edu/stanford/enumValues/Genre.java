package edu.stanford.enumValues;

/**
 * genre facet values for Stanford University SearchWorks
 * @author - Naomi Dushay
 */
public enum Genre {
	CONFERENCE_PROCEEDINGS,
	THESIS;

	/**
	 * need to override for text of multiple words
	 */
	@Override
	public String toString() {
		switch (this) {
			case CONFERENCE_PROCEEDINGS:
				return "Conference proceedings";
			case THESIS:
				return "Thesis/Dissertation";
			default:
				String lc = super.toString().toLowerCase();
				String firstchar = lc.substring(0, 1).toUpperCase();
				return lc.replaceFirst(".{1}", firstchar);
		}
	}
}
