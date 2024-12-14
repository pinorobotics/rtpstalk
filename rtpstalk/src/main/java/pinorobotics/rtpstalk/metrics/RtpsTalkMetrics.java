/*
 * Copyright 2023 pinorobotics
 * 
 * Website: https://github.com/pinorobotics/rtpstalk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pinorobotics.rtpstalk.metrics;

/**
 * Complete list of metrics emitted by <b>rtpstalk</b>
 *
 * <p><b>rtpstalk</b> metrics are integrated with <a
 * href="https://opentelemetry.io/">OpenTelemetry</a>.
 *
 * <p>To receive OpenTelemetry metrics users suppose to configure OpenTelemetry exporter. List of
 * Java exporters can be found in <a
 * href="https://opentelemetry.io/ecosystem/registry/?language=java&component=exporter">OpenTelemetry
 * registry</a> or in <a
 * href="https://github.com/lambdaprime/opentelemetry-exporters-pack">opentelemetry-exporters-pack</a>
 *
 * <p>Example of Elasticsearch dashboard (with exporter from opentelemetry-exporters-pack):
 *
 * <p><img alt="" src="doc-files/elasticsearch.png"/>
 *
 * @author lambdaprime intid@protonmail.com
 */
public interface RtpsTalkMetrics {

    String PROCESS_TIME_METRIC = "process_time_ms";
    String PROCESS_TIME_METRIC_DESCRIPTION = "RTPS Reader message processing time in millis";

    String DATA_COUNT_METRIC = "data_messages_received_total";
    String DATA_COUNT_METRIC_DESCRIPTION = "Number of data messages received";

    String JOIN_TIME_METRIC = "join_time_ms";
    String JOIN_TIME_METRIC_DESCRIPTION = "DATAFRAG join time in millis";

    String LOST_CHANGES_COUNT_METRIC = "lost_messages_total";
    String LOST_CHANGES_COUNT_METRIC_DESCRIPTION =
            "Total number of messages which were lost by the local reader (it means such messages"
                    + " are not available on the writer anymore)";

    String IRRELEVANT_CHANGES_COUNT_METRIC = "irrelevant_messages_total";
    String IRRELEVANT_CHANGES_COUNT_METRIC_DESCRIPTION =
            "Total number of messages which remote writer marked as irrelevant";

    String SUBMITTED_CHANGES_COUNT_METRIC = "submitted_changes_total";
    String SUBMITTED_CHANGES_COUNT_METRIC_DESCRIPTION =
            "Number of changes submitted to the writer by local publishers";

    String HEARTBEATS_COUNT_METRIC = "heartbeat_total";
    String HEARTBEATS_COUNT_METRIC_DESCRIPTION = "RTPS Writer heartbeat";

    String PARTICIPANTS_COUNT_METRIC = "participants_total";
    String PARTICIPANTS_COUNT_METRIC_DESCRIPTION = "Total number of discovered participants";

    String ANNOUNCEMENTS_COUNT_METRIC = "announcements_total";
    String ANNOUNCEMENTS_COUNT_METRIC_DESCRIPTION = "Number of SPDP announcements";

    String SEND_TIME_METRIC = "send_time_ms";
    String SEND_TIME_METRIC_DESCRIPTION = "RTPS message send time in millis";

    String RECEIVE_TIME_METRIC = "receive_time_ms";
    String RECEIVE_TIME_METRIC_DESCRIPTION = "RTPS message receive time in millis";

    String SERIALIZATION_TIME_METRIC = "serialization_time_ms";
    String READ_TIME_METRIC_DESCRIPTION = "RTPS message serialization time in millis";

    String DESERIALIZATION_TIME_METRIC = "deserialization_time_ms";
    String WRITE_TIME_METRIC_DESCRIPTION = "RTPS message deserialization time in millis";

    String RTPS_READER_COUNT_METRIC = "rtps_reader_total";
    String RTPS_READER_COUNT_METRIC_DESCRIPTION = "Total number of local RTPS readers";

    String FRAGMENTED_MESSAGES_READ_COUNT_METRIC = "fragmented_messages_read_total";
    String FRAGMENTED_MESSAGES_READ_COUNT_METRIC_DESCRIPTION =
            "Number of fragmented data messages which were joined back to original data message";
}
