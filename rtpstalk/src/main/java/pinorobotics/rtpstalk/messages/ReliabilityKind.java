package pinorobotics.rtpstalk.messages;

public enum ReliabilityKind {
    BEST_EFFORT(1),
    RELIABLE(2);

    private int value;

    private ReliabilityKind(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
