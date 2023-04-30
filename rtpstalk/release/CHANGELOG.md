# Version 4

- Updating tests for Windows
- Print topic name in cyclonedds publisher example
- Closing DataWriters before disposing SEDP endpoints
- Enabling support for RTPS 2.1 and adding support for CycloneDDS
- Improve logging around mismatch message types in the Reader
- When subscriptions publisher is disposed and PID_KEY_HASH is absent fallback to `PID_ENDPOINT_GUID`
- Fixing race condition between TopicPublicationsManager, TopicSubscriptionsManager and SPDP
- Support for HistoryQosPolicy
- Rely on `PID_PARTICIPANT_GUID` when participant is disposed and `PID_KEY_HASH` is absent
- Extracting HelloWorldExample interface from FastRtpsExamples
- Moving all fastdds tests to separate package
- Log warnings when keyed participants are discovered
- Renaming SedpService and SpdpService
- Run SpdpBuiltinParticipantReader on metadata unicast locator as well according to RTPS spec
- Adding ParticipantsRegistry
- Including INFO_DST to HEARTBEAT and DATA
- Adding DataRepresentationQosPolicy
- Supporting domain id in SPDP discovery and changing Duration::seconds to UnsignedInt
- Moving SEDP configuration to separate class
- Extracting MetatrafficMulticastReceiver and MetatrafficUnicastReceiver to separate classes
- Implementing metrics
- Updating Gradle to 8.0.2

[rtpstalk-v4.0.zip](https://github.com/pinorobotics/rtpstalk/raw/main/rtpstalk/release/rtpstalk-v4.0.zip)

# Previous versions

Changelog for previous versions were published in github Releases but then migrated here.
