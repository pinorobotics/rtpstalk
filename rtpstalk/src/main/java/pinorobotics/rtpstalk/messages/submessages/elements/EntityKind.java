package pinorobotics.rtpstalk.messages.submessages.elements;

public enum EntityKind {

    BUILTIN_UNKNOWN(0xc0),
    BUILTIN_PARTICIPANT(0xc1),
    BUILTIN_WRITER(0xc2),
    BUILTIN_WRITER_NO_KEY(0xc3),
    BUILTIN_READER(0xc7),
    BUILTIN_READER_NO_KEY(0xc4),
    BUILTIN_WRITER_GROUP(0xc8),
    BUILTIN_READER_GROUP(0xc9),

    UNKNOWN(0x00),
    WRITER(0x02),
    WRITER_NO_KEY(0x03),
    READER_NO_KEY(0x04),
    READER(0x07),
    WRITER_GROUP(0x08),
    READER_GROUP(0x09);

    private int value;

    private EntityKind(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
