package pinorobotics.rtpstalk.behavior.reader;

public record WriterInfo(
        WriterProxy proxy,
        WriterHeartbeatProcessor heartbeatProcessor) {
}
