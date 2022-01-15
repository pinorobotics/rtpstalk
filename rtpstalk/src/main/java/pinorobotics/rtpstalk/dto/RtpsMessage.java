package pinorobotics.rtpstalk.dto;

import java.util.Arrays;
import java.util.Objects;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.dto.submessages.Submessage;

public class RtpsMessage {
	
	public Header header;
	public Submessage<?>[] submessages;

	public RtpsMessage() {
		
	}
	
	public RtpsMessage(Header header, Submessage<?>[] submessages) {
		this.header = header;
		this.submessages = submessages;
	}

	public Submessage<?>[] getSubmessages() {
		return submessages;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(submessages);
		result = prime * result + Objects.hash(header);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RtpsMessage other = (RtpsMessage) obj;
		return Objects.equals(header, other.header) && Arrays.equals(submessages, other.submessages);
	}

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("header", header);
		builder.append("submessages", submessages);
		return builder.toString();
	}
}
