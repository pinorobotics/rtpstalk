package pinorobotics.rtpstalk.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class Duration {

	public static enum Predefined {
		ZERO(new Duration(0, 0)),
		INFINITE(new Duration(0x7fffffff, 0xffffffff));

		private Duration value;

		Predefined(Duration value) {
			this.value = value;
		}
	}
	
	@Streamed
	public int seconds;
	
	/**
	 * Time in sec/2^32
	 */
	@Streamed
	public int fraction;

	public Duration() {
		// TODO Auto-generated constructor stub
	}
	
	public Duration(int seconds, int fraction) {
		this.seconds = seconds;
		this.fraction = fraction;
	}
	
	static Map<Duration, Predefined> map = new HashMap<>();
	static {
		for (var t: Predefined.values()) map.put(t.value, t);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(fraction, seconds);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Duration other = (Duration) obj;
		return fraction == other.fraction && seconds == other.seconds;
	}

	@Override
	public String toString() {
		var predefined = map.get(this);
		if (predefined != null) {
			return predefined.name();
		}
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("seconds", seconds);
		builder.append("fraction", fraction);
		return builder.toString();
	}
}
