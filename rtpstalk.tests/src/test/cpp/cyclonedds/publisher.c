#include "dds/dds.h"
#include "HelloWorldData.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void publish(dds_entity_t writer, int id, const char* data) {
  HelloWorld msg;
  /* Create a message to write. */
  msg.index = id;
  msg.message = data;

  printf ("=== [Publisher]  Writing : \n");
  printf ("Message (%"PRId32", %s)\n", msg.index, msg.message);
  fflush (stdout);
  int rc = dds_write (writer, &msg);
  if (rc != DDS_RETCODE_OK)
    DDS_FATAL("dds_write: %s\n", dds_strretcode(-rc));
}

int main (int argc, char ** argv)
{
  dds_entity_t participant;
  dds_entity_t topic;
  dds_entity_t writer;
  dds_return_t rc;
  uint32_t status = 0;
  (void)argc;
  (void)argv;
  dds_set_log_mask(DDS_TRACE_MASK);
  /* Create a Participant. */
  participant = dds_create_participant (DDS_DOMAIN_DEFAULT, NULL, NULL);
  if (participant < 0)
    DDS_FATAL("dds_create_participant: %s\n", dds_strretcode(-participant));
  int numOfSamples = 1;
  if (argc > 1)
    numOfSamples = atoi(argv[1]);
  printf("Num of samples: %d\n", numOfSamples);

  dds_qos_t *qos = dds_create_qos ();
  char* cstr = getenv("RTPS_ReliabilityQosPolicyKind");
  if (cstr != NULL) {
    if (strcmp(cstr, "BEST_EFFORT_RELIABILITY") == 0) 
      dds_qset_reliability (qos, DDS_RELIABILITY_BEST_EFFORT, DDS_SECS(10));
    else
      dds_qset_reliability (qos, DDS_RELIABILITY_RELIABLE, DDS_SECS(10));
  } else {
    dds_qset_reliability (qos, DDS_RELIABILITY_RELIABLE, DDS_SECS(10));
  }
  dds_qset_durability(qos, DDS_DURABILITY_TRANSIENT_LOCAL);
  cstr = getenv("RTPS_TopicName");

  /* Create a Topic. */
  char* topicName = cstr != NULL? cstr: "HelloWorldTopic";
  printf("Topic name: %s\n", topicName);
  topic = dds_create_topic (
    participant, &HelloWorld_desc, topicName, qos, NULL);
  if (topic < 0)
    DDS_FATAL("dds_create_topic: %s\n", dds_strretcode(-topic));

  /* Create a Writer. */
  writer = dds_create_writer (participant, topic, qos, NULL);
  if (writer < 0)
    DDS_FATAL("dds_create_writer: %s\n", dds_strretcode(-writer));

  printf("=== [Publisher]  Waiting for a reader to be discovered ...\n");
  fflush (stdout);

  rc = dds_set_status_mask(writer, DDS_PUBLICATION_MATCHED_STATUS);
  if (rc != DDS_RETCODE_OK)
    DDS_FATAL("dds_set_status_mask: %s\n", dds_strretcode(-rc));
  uint32_t oldStatus = status;
  while(!(status & DDS_PUBLICATION_MATCHED_STATUS))
  {
    rc = dds_get_status_changes (writer, &status);
    if (rc != DDS_RETCODE_OK)
      DDS_FATAL("dds_get_status_changes: %s\n", dds_strretcode(-rc));
    if (oldStatus != status) {
      oldStatus = status;
    }
    
    /* Polling sleep. */
    dds_sleepfor (DDS_MSECS (20));
  }

  if (numOfSamples == -1) {
    int i = 0;
    char*line = NULL;
    int len = 0;
    while ((len = getline(&line, &len, stdin)) != -1) {
      line[len - 1] = 0;
      publish(writer, i++, line);
      free(line);
      line = NULL;
      len = 0;
    }
  } else {
    for (uint32_t i = 0; i < numOfSamples; ++i) publish(writer, i + 1, "HelloWorld");
  }
  
  /* Deleting the participant will delete all its children recursively as well. */
  rc = dds_delete (participant);
  if (rc != DDS_RETCODE_OK)
    DDS_FATAL("dds_delete: %s\n", dds_strretcode(-rc));

  return EXIT_SUCCESS;
}