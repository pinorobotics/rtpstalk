#include "dds/dds.h"
#include "HelloWorldData.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

int main (int argc, char ** argv)
{
  dds_entity_t participant;
  dds_entity_t topic;
  dds_entity_t reader;
  HelloWorld *msg;
  void **samples;
  dds_sample_info_t *infos;
  dds_return_t rc;
  dds_qos_t *qos;
  (void)argc;
  (void)argv;
  int numOfSamples = 1;
  if (argc >= 1)
    numOfSamples = atoi(argv[1]);
  printf("Num of samples: %d\n", numOfSamples);

  /* Create a Participant. */
  participant = dds_create_participant (DDS_DOMAIN_DEFAULT, NULL, NULL);
  if (participant < 0)
    DDS_FATAL("dds_create_participant: %s\n", dds_strretcode(-participant));

  char* cstr = getenv("RTPS_TopicName");

  /* Create a Topic. */
  topic = dds_create_topic (
    participant, &HelloWorld_desc, cstr != NULL? cstr: "HelloWorldTopic", NULL, NULL);
  if (topic < 0)
    DDS_FATAL("dds_create_topic: %s\n", dds_strretcode(-topic));

  /* Create a reliable Reader. */
  qos = dds_create_qos ();
  dds_qset_reliability (qos, DDS_RELIABILITY_RELIABLE, DDS_SECS (10));
  dds_qset_history (qos, DDS_HISTORY_KEEP_ALL, 1000);
  dds_qset_durability(qos, DDS_DURABILITY_TRANSIENT_LOCAL);
  reader = dds_create_reader (participant, topic, qos, NULL);
  if (reader < 0)
    DDS_FATAL("dds_create_reader: %s\n", dds_strretcode(-reader));
  dds_delete_qos(qos);

  printf ("\n=== [Subscriber] Waiting for a sample ...\n");
  fflush (stdout);

  samples = malloc(numOfSamples * sizeof(void*));
  /* Initialize sample buffer, by pointing the void pointer within
   * the buffer array to a valid sample memory location. */
  for (int i = 0; i < numOfSamples; i++) {
    samples[i] = HelloWorld__alloc ();
  }

  infos = malloc(numOfSamples * sizeof(dds_sample_info_t));
  
  /* Poll until data has been read. */
  while (true)
  {
    rc = dds_read (reader, samples, infos, numOfSamples, numOfSamples);
    for (int i = 0; i < rc; i++) {
      if (! infos[i].valid_data) continue;
      msg = (HelloWorld*) samples[i];
      printf ("=== [Subscriber] Received : ");
      printf ("Message (%"PRId32", %s)\n", msg->index, msg->message);
      fflush (stdout);
    }
    if (rc > 0) break;
    dds_sleepfor (DDS_MSECS (20));
    continue;
    /* Do the actual read.
     * The return value contains the number of read samples. */
    rc = dds_read (reader, samples, infos, numOfSamples, numOfSamples);
    if (rc < 0)
      DDS_FATAL("dds_read: %s\n", dds_strretcode(-rc));

    /* Check if we read some data and it is valid. */
    if ((rc > 0) && (infos[0].valid_data))
    {
      for (int i = 0; i < numOfSamples; i++) {
        /* Print Message. */
        msg = (HelloWorld*) samples[i];
        printf ("=== [Subscriber] Received : ");
        printf ("Message (%"PRId32", %s)\n", msg->index, msg->message);
        fflush (stdout);
      }
      break;
    }
    else
    {
      /* Polling sleep. */
      dds_sleepfor (DDS_MSECS (20));
    }
  }

  /* Free the data location. */
  HelloWorld_free (samples[0], DDS_FREE_ALL);

  /* Explicitly deleting subscriber for test purposes */
  rc = dds_delete (reader);
  if (rc != DDS_RETCODE_OK)
    DDS_FATAL("dds_delete: %s\n", dds_strretcode(-rc));

  /* Deleting the participant will delete all its children recursively as well. */
  rc = dds_delete (participant);
  if (rc != DDS_RETCODE_OK)
    DDS_FATAL("dds_delete: %s\n", dds_strretcode(-rc));

  return EXIT_SUCCESS;
}
