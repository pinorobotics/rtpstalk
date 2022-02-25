package pinorobotics.rtpstalk.messages;

import id.xfunction.XJsonStringBuilder;

/**
 * If the RELIABILITY kind is set to RELIABLE, the write operation may block if
 * the modification would cause data to be lost or else cause one of the limits
 * specified in the RESOURCE_LIMITS to be exceeded. Under these circumstances,
 * the RELIABILITY max_blocking_time configures the maximum time the write
 * operation may block waiting for space to become available. If
 * max_blocking_time elapses before the DataWriter is able to store the
 * modification without exceeding the limits, the write operation will fail and
 * return TIMEOUT (2.2.2.4.2.11 write)
 */
public class ReliabilityQosPolicy {

    public int kind;

    public Duration maxBlockingTime;

    public ReliabilityQosPolicy() {

    }

    public ReliabilityQosPolicy(ReliabilityKind kind, Duration maxBlockingTime) {
        this.kind = kind.getValue();
        this.maxBlockingTime = maxBlockingTime;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("kind", kind);
        builder.append("maxBlockingTime", maxBlockingTime);
        return builder.toString();
    }
}
