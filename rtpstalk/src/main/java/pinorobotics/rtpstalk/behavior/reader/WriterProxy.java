package pinorobotics.rtpstalk.behavior.reader;

import java.util.List;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;

public record WriterProxy(

        /**
         * Identifies the reader to which this Writer belongs
         */
        Guid readerGuid,

        /**
         * Identifies the matched Writer. Configured by discovery
         */
        Guid remoteWriterGuid,

        /**
         * List of unicast (address, port) combinations that can be used to send
         * messages to the matched Writer or Writers. The list may be empty.
         */
        List<Locator> unicastLocatorList) {

}
