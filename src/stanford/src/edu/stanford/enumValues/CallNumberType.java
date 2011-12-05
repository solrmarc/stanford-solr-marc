package edu.stanford.enumValues;

/** 
 * call number types for Stanford University
 * @author - Naomi Dushay
 */
public enum CallNumberType {
	DEWEY,  // TODO:  do cubberley TX dewey call numbers need a separate type?
	HARVYENCH,  // Harvard Yenching
	LC,
	SUDOC,
	THESIS,
	XX,
	OTHER;
	
	public String getPrefix() {
		switch(this) {
			case DEWEY:
				return "dewey ";
			case HARVYENCH:
				return "harvyench ";
			case LC:
				return "lc ";
			case SUDOC:
				return "sudoc ";
			case THESIS:
				return "thesis ";
//			case XX:
//				return "xxx ";
			case OTHER:
				return "other ";
			default:
				return "other ";
		}
	}

}
