# Version 5

- Adding build support for Android
- Renaming write/read metrics
- Make sure that fragmentSize in fragmented messages which are resended, always consistent with the original, first message +update WriterProxy to be thread-safe
- Issue lambdaprime/jros2client#8 Allow users to change pushMode setting
- For TRANSIENT_LOCAL_DURABILITY_QOS mark missing messages starting from sequence number 1 and not from the one which is first received
- Adding latency test
- Do not exceed SequenceNumberSet.BITMAP_SIZE when building ACKNACK message
- Fix ConcurrentModificationException in removeAllBelow by making WriterChanges as thread-safe
- Extract RtpsMessage aggregation logic to RtpsMessageAggregator
- Run cache cleanup when new ACKNACK received
- Use SynchronousPublisher and TransformSubscriber (instead of TransformProcessor)
- Adding DurabilityQosPolicy support for StatefullReliableRtpsReader
- Do not add messages from non matched writers to the HistoryCache
- Adding comments and references for DDS spec
- Ensure message ordering for StatefullReliableRtpsReader and adding tests
- Use SortedMap inside the WriterChanges to make sure that all changes are ordered by sequence number even if they added out-of-order
- When Participant leaves remove all its non built-in matched readers and writers
- Renaming OperatingEntities
- Extract all test logs setup to LogExtension class
- Updating throughput tests to changes in pubsubtests module
- Issue #2 Do not show message mismatch warnings for ENTITYID_UNKNOWN readers (as it is expected) and log them insteadUpdating to kineticstreamer 6.0-SNAPSHOT
- Issue lambdaprime/jros2client#5 Use bulk operations to improve performance of RTPS message (de)serialization, update tests
- Add logging to DataFragmentSplitter
- Allow users to change socket sendBufferSize
- Fixing issue when Subscriber keeps reporting that message is absent if it was received out of order
- Including TracingToken to HistoryCache logging
- Use ByteBuffer to handle endianess instead of doing it manually
- Integrating xfunctiontests and removing XAssert
- Integrating with load tests from pubsubtests, adding fragmented_messages_read metric
- Add support for user inlineQos parameters in DataFrag submessages, add more tests for DataFragmentSplitter
- Do not use several namespaces for tests and consolidate them under non impl one
- Updates to new changes in pubsubtests
- Support joining fragments which come out of order
- Update metrics dashboard
- Updating xfunction to v24
- Updating gradle to 8.3
- Adding metrics dashboards
- Updating download links

[rtpstalk-v5.0.zip](https://github.com/pinorobotics/rtpstalk/raw/main/rtpstalk/release/rtpstalk-v5.0.zip)

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
