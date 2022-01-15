package pinorobotics.rtpstalk.dto.submessages;

import pinorobotics.rtpstalk.dto.submessages.elements.GuidPrefix;

public class InfoDestination extends Submessage {
	
	/**
	 * Provides the GuidPrefix that should be used to reconstruct the
     * GUIDs of all the RTPS Reader entities whose EntityIds appears
     * in the Submessages that follow
	 */
	public GuidPrefix guidPrefix;
	
	public InfoDestination() {

	}

	@Override
	protected Object[] getAdditionalFields() {
		return new Object[] {"guidPrefix", guidPrefix};
	}
}
