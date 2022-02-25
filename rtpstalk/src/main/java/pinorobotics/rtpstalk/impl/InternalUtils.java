package pinorobotics.rtpstalk.impl;

import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;

public class InternalUtils {

    private static final InternalUtils INSTANCE = new InternalUtils();

    public static InternalUtils getInstance() {
        return INSTANCE;
    }

    public XLogger getLogger(Class<?> clazz, String contextName) {
        return XLogger.getLogger(clazz.getName() + "#" + contextName);
    }

    public XLogger getLogger(Class<?> clazz, EntityId contextEntityId) {
        return XLogger.getLogger(clazz.getName() + "#" + contextEntityId.toString());
    }

}
