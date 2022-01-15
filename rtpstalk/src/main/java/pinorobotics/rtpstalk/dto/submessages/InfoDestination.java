package pinorobotics.rtpstalk.dto.submessages;

import java.util.List;

import pinorobotics.rtpstalk.dto.submessages.elements.GuidPrefix;

public class InfoDestination extends Submessage<GuidPrefix> {
	
	/**
	 * Provides the GuidPrefix that should be used to reconstruct the
     * GUIDs of all the RTPS Reader entities whose EntityIds appears
     * in the Submessages that follow
	 */
	public GuidPrefix guidPrefix;
	
	public InfoDestination() {

	}

	@Override
	public List<GuidPrefix> getSubmessageElements() {
		return List.of(guidPrefix);
	}

}
