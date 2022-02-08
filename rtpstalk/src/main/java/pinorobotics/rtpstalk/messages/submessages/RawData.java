package pinorobotics.rtpstalk.messages.submessages;

public class RawData implements Payload {

    public byte[] data;

    public RawData() {
    }

    public RawData(byte[] data) {
        this.data = data;
    }

}
