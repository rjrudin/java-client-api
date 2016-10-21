/*
 * Copyright 2015 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.client.test.datamovement.javadocExamples;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.DeleteQueryDefinition;
import com.marklogic.client.query.MatchDocumentSummary;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.StructuredQueryBuilder;

import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.JobTicket;
import com.marklogic.client.datamovement.QueryHostBatcher;
import com.marklogic.client.datamovement.WriteHostBatcher;
import com.marklogic.client.test.datamovement.Common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;

public class PackageExamples {
  private static Logger logger = LoggerFactory.getLogger(PackageExamples.class);
  private static DataMovementManager dataMovementManager = DataMovementManager.newInstance();
  private static DatabaseClient client = Common.connect();
  private static String collection = "PackageExamples_" +
    new Random().nextInt(10000);
  private static DocumentMetadataHandle meta = new DocumentMetadataHandle().withCollections(collection);
  private static QueryDefinition collectionQuery = new StructuredQueryBuilder().collection(collection);

  @BeforeClass
  public static void beforeClass() {
    dataMovementManager.withClient(client);
    //System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "debug");
  }

  @AfterClass
  public static void afterClass() {
    DeleteQueryDefinition deleteQuery = client.newQueryManager().newDeleteDefinition();
    deleteQuery.setCollections(collection);
    client.newQueryManager().delete(deleteQuery);
    client.newDocumentManager().delete("doc1.txt");
    client.newDocumentManager().delete("doc2.txt");
  }

  @Test
  public void testQueryHostBatcher() {
    client.newDocumentManager().writeAs(collection + "/test1.json", meta, "[true]");
    client.newDocumentManager().writeAs(collection + "/test1.xml",  meta, "<xml/>");
    client.newDocumentManager().writeAs(collection + "/test1.txt",  meta, "text");
    assertEquals(3, client.newQueryManager().search(collectionQuery, new SearchHandle()).getTotalResults());

    QueryDefinition query = collectionQuery;

    // begin copy from "Using QueryHostBatcher" in src/main/java/com/marklogic/datamovement/package-info.java
    QueryHostBatcher qhb = dataMovementManager.newQueryHostBatcher(query)
        .withBatchSize(1000)
        .withThreadCount(20)
        .withConsistentSnapshot()
        .onUrisReady((client, batch) -> {
            for ( String uri : batch.getItems() ) {
                if ( uri.endsWith(".txt") ) {
                    client.newDocumentManager().delete(uri);
                }
            }
        })
        .onQueryFailure((client, queryHostException) -> queryHostException.printStackTrace());
    JobTicket ticket = dataMovementManager.startJob(qhb);
    qhb.awaitCompletion();
    dataMovementManager.stopJob(ticket);
    // end copy from "Using QueryHostBatcher" in src/main/java/com/marklogic/datamovement/package-info.java

    SearchHandle results = client.newQueryManager().search(collectionQuery, new SearchHandle());
    assertEquals(2, results.getTotalResults());
    for ( MatchDocumentSummary match : results.getMatchResults() ) {
      assertTrue(match.getUri().matches(".*/test1.(json|xml)"));
    }
  }

  @Test
  public void testWriteHostBatcher() {
    assertEquals(null, client.newDocumentManager().exists("doc1.txt"));
    assertEquals(null, client.newDocumentManager().exists("doc2.txt"));

    // begin copy from "Using WriteHostBatcher" in src/main/java/com/marklogic/datamovement/package-info.java
    WriteHostBatcher whb = dataMovementManager.newWriteHostBatcher()
        .withBatchSize(100)
        .withThreadCount(20)
        .onBatchSuccess((client,batch) -> {
            logger.debug("batch # {}, so far: {}", batch.getJobBatchNumber(), batch.getJobWritesSoFar());
        })
        .onBatchFailure((client,batch,throwable) -> throwable.printStackTrace() );
    JobTicket ticket = dataMovementManager.startJob(whb);
    // the add or addAs methods could be called in separate threads on the
    // single whb instance
    whb.add  ("doc1.txt", new StringHandle("doc1 contents"));
    whb.addAs("doc2.txt", "doc2 contents");

    whb.flushAndWait(); // send the two docs even though they're not a full batch
    dataMovementManager.stopJob(ticket);
    // end copy from "Using WriteHostBatcher" in src/main/java/com/marklogic/datamovement/package-info.java

    assertTrue(null != client.newDocumentManager().exists("doc1.txt"));
    assertTrue(null != client.newDocumentManager().exists("doc2.txt"));
  }
}
