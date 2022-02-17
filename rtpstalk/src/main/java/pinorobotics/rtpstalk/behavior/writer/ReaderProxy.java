package pinorobotics.rtpstalk.behavior.writer;

import java.util.List;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;

public class ReaderProxy {

    private Guid remoteReaderGuid;
    private List<Locator> unicastLocatorList;

    public ReaderProxy(Guid remoteReaderGuid, List<Locator> unicastLocatorList) {
        this.remoteReaderGuid = remoteReaderGuid;
        this.unicastLocatorList = unicastLocatorList;
    }

    /**
     * Identifies the remote matched RTPS Reader that is represented by the
     * ReaderProxy.
     */
    public Guid getRemoteReaderGuid() {
        return remoteReaderGuid;
    }

    /**
     * List of unicast (address, port) combinations that can be used to send
     * messages to the matched Writer or Writers. The list may be empty.
     */
    public List<Locator> getUnicastLocatorList() {
        return unicastLocatorList;
    }
}
