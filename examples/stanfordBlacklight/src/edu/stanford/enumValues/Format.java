package edu.stanford.enumValues;

/** 
 * format facet values for Stanford University
 * @author - Naomi Dushay
 */
public enum Format {
	BOOK,
	COMPUTER_FILE,
	CONFERENCE_PROCEEDINGS,
	IMAGE,
	JOURNAL_PERIODICAL,
	MANUSCRIPT_ARCHIVE,
	MAP_GLOBE,
	MICROFORMAT,
	MUSIC_RECORDING,
	MUSIC_SCORE,
	NEWSPAPER,
	SOUND_RECORDING,
	THESIS,
	VIDEO,
	OTHER;

	/**
	 * need to override for text of multiple words
	 */
	@Override
	public String toString() {
		switch (this) {
		case COMPUTER_FILE:
			return "Computer File";
		case CONFERENCE_PROCEEDINGS:
			return "Conference Proceedings";
		case JOURNAL_PERIODICAL:
			return "Journal/Periodical";
		case MANUSCRIPT_ARCHIVE:
			return "Manuscript/Archive";
		case MAP_GLOBE:
			return "Map/Globe";
		case MUSIC_RECORDING:
			return "Music - Recording";
		case MUSIC_SCORE:
			return "Music - Score";
		case SOUND_RECORDING:
			return "Sound Recording";
		}
		String lc = super.toString().toLowerCase();
		String firstchar = lc.substring(0, 1).toUpperCase();
		return lc.replaceFirst(".{1}", firstchar);
	}
}
