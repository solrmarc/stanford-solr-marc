package edu.stanford.enumValues;

/**
 * physical format facet values for Stanford University SearchWorks
 * @author - Naomi Dushay
 */
public enum FormatPhysical
{
	// recordings
	CD,
	VINYL,
	VINYL_45,
	SHELLAC_78,
	CYLINDER,
	INSTANTANEOUS_DISC,
	CASSETTE,
	CARTRIDGE_8_TRACK,
	DAT,
	REEL_TO_REEL,
	OTHER_RECORDING,

	// images
	SLIDE,
	PHOTO,
	REMOTE_SENSING_IMAGE,
	OTHER_IMAGE,

	// videos
	FILM,
	DVD,
	BLURAY,
	VHS,
	BETA,
	BETA_SP,
	MP4,
	OTHER_VIDEO,

	// maps
	ATLAS,
	GLOBE,
	OTHER_MAPS,

	// microformats
	MICROFILM,
	MICROFICHE,

	OTHER;


	/**
	 * need to override for text of multiple words
	 */
	@Override
	public String toString() {
		switch (this) {
			// recordings
			case CD:
				return "CD";
			case VINYL:
				return "Vinyl disc";
			case VINYL_45:
				return "45 rpm disc";
			case SHELLAC_78:
				return "78 rpm (shellac)";
			case CYLINDER:
				return "Cylinder (Wax)";
			case INSTANTANEOUS_DISC:
				return "Instantaneous disc";
			case CASSETTE:
				return "Audio cassette";
			case CARTRIDGE_8_TRACK:
				return "8-track cartridge";
			case DAT:
				return "DAT";
			case REEL_TO_REEL:
				return "Reel-toreel tape";
			case OTHER_RECORDING:
				return "Other recording";

			// images
			case SLIDE:
				return "Slide";
			case PHOTO:
				return "Photo";
			case REMOTE_SENSING_IMAGE:
				return "Remote-sensing image";
			case OTHER_IMAGE:
				return "Other image";

			// videos
			case FILM:
				return "Film";
			case DVD:
				return "DVD";
			case BLURAY:
				return "Blu-ray";
			case VHS:
				return "Videocassette (VHS)";
			case BETA:
				return "Videocassette (Beta)";
			case BETA_SP:
				return "Videocassette (Beta SP)";
			case MP4:
				return "MP4";
			case OTHER_VIDEO:
				return "Other video";

			// maps
			case ATLAS:
				return "Atlas";
			case GLOBE:
				return "Globe";
			case OTHER_MAPS:
				return "Other Maps";

			// microformats
			case MICROFILM:
				return "Microfilm";
			case MICROFICHE:
				return "Microfiche";

			case OTHER:
				return "Other";
			default:
				String lc = super.toString().toLowerCase();
				String firstchar = lc.substring(0, 1).toUpperCase();
				return lc.replaceFirst(".{1}", firstchar);
		}

	}

}
