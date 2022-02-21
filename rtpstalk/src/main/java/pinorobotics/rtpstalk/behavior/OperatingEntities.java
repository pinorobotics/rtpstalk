package pinorobotics.rtpstalk.behavior;

import id.xfunction.XAsserts;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import pinorobotics.rtpstalk.behavior.writer.StatefullRtpsWriter;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;

public class OperatingEntities {

    private static final OperatingEntities INSTANCE = new OperatingEntities();
    private Map<EntityId, StatefullRtpsWriter<?>> writers = new ConcurrentHashMap<>();

    public static OperatingEntities getInstance() {
        return INSTANCE;
    }

    public void add(EntityId entityId, StatefullRtpsWriter<?> writer) {
        XAsserts.assertTrue(!writers.containsKey(entityId), "Writer " + entityId + " already present");
        writers.put(entityId, writer);
    }

    public Optional<StatefullRtpsWriter<?>> findStatefullWriter(EntityId entityId) {
        return Optional.ofNullable(writers.get(entityId));
    }

    public void remove(EntityId entityId) {
        writers.remove(entityId);
    }
}
