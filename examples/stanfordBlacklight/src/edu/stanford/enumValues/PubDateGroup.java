package edu.stanford.enumValues;

/** 
 * pub date grouping facet values for Stanford University 
 * @author - Naomi Dushay
 */
public enum PubDateGroup {
	THIS_YEAR,
	LAST_3_YEARS,
	LAST_10_YEARS,
	LAST_50_YEARS,
	MORE_THAN_50_YEARS_AGO,
	;

	/**
	 * need to override for text of multiple words
	 */
	@Override
	public String toString() {
		switch (this) {
		case THIS_YEAR:
			return "This year";
		case LAST_3_YEARS:
			return "Last 3 years";
		case LAST_10_YEARS:
			return "Last 10 years";
		case LAST_50_YEARS:
			return "Last 50 years";
		case MORE_THAN_50_YEARS_AGO:
			return "More than 50 years ago";
		}
		String lc = super.toString().toLowerCase();
		String firstchar = lc.substring(0, 1).toUpperCase();
		return lc.replaceFirst(".{1}", firstchar);
	}

}
