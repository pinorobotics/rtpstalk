package pinorobotics.rtpstalk;

import id.xfunction.concurrent.flow.XSubscriber;
import id.xfunction.logging.XLogger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import pinorobotics.rtpstalk.discovery.sedp.SedpService;
import pinorobotics.rtpstalk.discovery.spdp.SpdpService;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.KeyHash;
import pinorobotics.rtpstalk.messages.ProtocolId;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.messages.submessages.RepresentationIdentifier;
import pinorobotics.rtpstalk.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.messages.submessages.SerializedPayloadHeader;
import pinorobotics.rtpstalk.messages.submessages.Submessage;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;

public class Test {

    private static RtpsTalkConfiguration config;

    public static void main(String[] args) throws Exception {
        XLogger.load("rtpstalk-debug.properties");
        config = RtpsTalkConfiguration.DEFAULT;
        var channelFactory = new DataChannelFactory(config);
        var spdp = new SpdpService(config, channelFactory);
        var sedp = new SedpService(config, channelFactory);
        sedp.start(spdp.getReader());
        spdp.start();
        var printer = new XSubscriber<CacheChange>() {
            @Override
            public void onNext(CacheChange item) {
                if (item.getDataValue() instanceof ParameterList pl) {
                    var topic = "rt/chatter";
                    if (Objects.equals(pl.params.get(ParameterId.PID_TOPIC_NAME), topic)) {
                        System.out.println(pl);
                        try {
                            var receiver = new RtpsMessageReceiver(topic);
                            // announce to writer about new subscription
                            channelFactory
                                    .connect(sedp.getSubscriptionsReader().matchedWriters().get(0)
                                            .getUnicastLocatorList().get(0))
                                    .send(createSubscriptionMessage("rt/chatter", "std_msgs::msg::dds_::String_"));
                            receiver.start(channelFactory.bind(config.getDefaultUnicastLocator()));
                            System.out.println("receiver is started");
                            receiver.subscribe(new XSubscriber<RtpsMessage>() {
                                @Override
                                public void onNext(RtpsMessage item) {
                                    System.out.println(item);
                                    subscription.request(1);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                subscription.request(1);
            }
        };
        sedp.getPublicationsReader().subscribe(printer);
        System.in.read();
    }

    private static RtpsMessage createSubscriptionMessage(String topicName, String typeName) {
        var params = List.<Entry<ParameterId, Object>>of(
                Map.entry(ParameterId.PID_UNICAST_LOCATOR, config.getDefaultUnicastLocator()),
                Map.entry(ParameterId.PID_PARTICIPANT_GUID, new Guid(
                        config.getGuidPrefix(), EntityId.Predefined.ENTITYID_PARTICIPANT.getValue())),
                Map.entry(ParameterId.PID_TOPIC_NAME, topicName),
                Map.entry(ParameterId.PID_TYPE_NAME, typeName),
                Map.entry(ParameterId.PID_KEY_HASH, new KeyHash(
                        0x01, 0x0f, 0xeb, 0x7d, 0x5f, 0xfa, 0x9f, 0xe4, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x12,
                        0x04)),
                Map.entry(ParameterId.PID_ENDPOINT_GUID,
                        new Guid(config.getGuidPrefix(), new EntityId(new byte[] { 00, 00, 0x12 }, 04))),
                Map.entry(ParameterId.PID_PROTOCOL_VERSION, ProtocolVersion.Predefined.Version_2_3.getValue()),
                Map.entry(ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue()));
        var submessages = new Submessage[] { InfoTimestamp.now(),
                new Data(0b100 | RtpsTalkConfiguration.ENDIANESS_BIT, 0,
                        EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR.getValue(),
                        EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER.getValue(),
                        new SequenceNumber(1),
                        new SerializedPayload(new SerializedPayloadHeader(
                                RepresentationIdentifier.Predefined.PL_CDR_LE.getValue()),
                                new ParameterList(params))) };
        Header header = new Header(
                ProtocolId.Predefined.RTPS.getValue(),
                ProtocolVersion.Predefined.Version_2_3.getValue(),
                VendorId.Predefined.RTPSTALK.getValue(),
                config.getGuidPrefix());
        return new RtpsMessage(header, submessages);
    }
}
