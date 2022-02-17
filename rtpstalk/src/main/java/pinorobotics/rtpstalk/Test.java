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
import pinorobotics.rtpstalk.messages.KeyHash;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;
import pinorobotics.rtpstalk.transport.RtpsMessageSender;

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
        var printer = new XSubscriber<CacheChange<ParameterList>>() {
            @Override
            public void onNext(CacheChange<ParameterList> item) {
                var topic = "rt/chatter";
                var pl = item.getDataValue();
                if (Objects.equals(pl.params.get(ParameterId.PID_TOPIC_NAME), topic)) {
                    System.out.println(pl);
                    try {
                        var sender = new RtpsMessageSender(channelFactory
                                .connect(sedp.getSubscriptionsReader().matchedWriters().get(0)
                                        .getUnicastLocatorList().get(0)),
                                "sender");
                        sedp.getSubscriptionsWriter().subscribe(sender);
                        sedp.getSubscriptionsWriter().newChange(
                                createSubscriptionData("rt/chatter", "std_msgs::msg::dds_::String_"));
                        var receiver = new RtpsMessageReceiver(topic);
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
                subscription.request(1);
            }
        };

//        sedp.getPublicationsWriter().newChange(createNew);
        sedp.getPublicationsReader().subscribe(printer);
        System.in.read();
    }

    private static ParameterList createSubscriptionData(String topicName, String typeName) {
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
        return new ParameterList(params);
    }
}
