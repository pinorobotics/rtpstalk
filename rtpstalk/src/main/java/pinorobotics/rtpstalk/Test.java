package pinorobotics.rtpstalk;

import id.xfunction.logging.XLogger;
import java.util.Objects;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import pinorobotics.rtpstalk.discovery.sedp.SedpService;
import pinorobotics.rtpstalk.discovery.spdp.SpdpService;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.transport.DataChannelFactory;

public class Test {

    public static void main(String[] args) throws Exception {
        XLogger.load("rtpstalk-debug.properties");
        var config = RtpsTalkConfiguration.DEFAULT;
        var channelFactory = new DataChannelFactory(config);
        var spdp = new SpdpService(config, channelFactory);
        var sedp = new SedpService(config, channelFactory);
        sedp.start(spdp.getReader().getCache());
        spdp.start();
        var printer = new Subscriber<CacheChange>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(CacheChange item) {
                if (item.getDataValue().getSerializedPayload().payload instanceof ParameterList pl) {
                    System.out.println(pl);
                }
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {
                // TODO Auto-generated method stub

            }
        };
        sedp.getPublicationsReader().getCache().subscribe(printer);
//        sedp.getSubscriptionsReader().getCache().subscribe(printer);
        System.in.read();
    }
}
