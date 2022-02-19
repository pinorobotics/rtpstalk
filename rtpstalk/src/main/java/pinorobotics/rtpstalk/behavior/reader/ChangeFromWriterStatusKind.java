package pinorobotics.rtpstalk.behavior.reader;

public enum ChangeFromWriterStatusKind {
    LOST,

    /**
     * The changes with status {@link #MISSING} represent the set of changes
     * available in the HistoryCache of the RTPS Writer that have not been received
     * by the RTPS Reader.
     */
    MISSING,

    RECEIVED,

    UNKNOWN
}
