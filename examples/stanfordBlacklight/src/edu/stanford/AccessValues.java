package edu.stanford;

/** 
 * access facet values for Stanford University
 * @author - Naomi Dushay
 */
public enum AccessValues {
	ONLINE,
	AT_LIBRARY;

	/**
	 * need to override for text of multiple words
	 */
	@Override
	public String toString() {
		switch (this) {
		case AT_LIBRARY:
			return "At the Library";
		case ONLINE:
			return "Online";
		}
		String lc = super.toString().toLowerCase();
		String firstchar = lc.substring(0, 1).toUpperCase();
		return lc.replaceFirst(".{1}", firstchar);
	}
}

