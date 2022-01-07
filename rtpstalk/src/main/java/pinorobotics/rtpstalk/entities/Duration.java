package pinorobotics.rtpstalk.entities;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import id.xfunction.XJsonStringBuilder;

public class Duration {

	public static enum Predefined {
		ZERO(new Duration(0, 0)),
		INFINITE(new Duration(0x7fffffff, 0xffffffff));

		static final Map<Duration, Predefined> MAP = Arrays.stream(Predefined.values())
				.collect(Collectors.toMap(k -> k.value, v -> v));
		private Duration value;

		Predefined(Duration value) {
			this.value = value;
		}
	}
	
	public int seconds;
	
	/**
	 * Time in sec/2^32
	 */
	public int fraction;

	public Duration() {

	}

	public Duration(int seconds, int fraction) {
		this.seconds = seconds;
		this.fraction = fraction;
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
		var predefined = Predefined.MAP.get(this);
		if (predefined != null) {
			return predefined.name();
		}
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("seconds", seconds);
		builder.append("fraction", fraction);
		return builder.toString();
	}
}
