package com.marklogic.client.test.dataservices;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.Forest;
import com.marklogic.client.datamovement.QueryBatcher;
import com.marklogic.client.dataservices.OutputEndpoint;
import com.marklogic.client.impl.NodeConverter;
import com.marklogic.client.io.StringHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RunStepTest {

	private final static Logger logger = LoggerFactory.getLogger(RunStepTest.class);

	private final static String JSON = "{\n" +
		"  \"endpoint\": \"/dataservices/runStep.sjs\",\n" +
		"  \"params\": [ {\n" +
		"    \"name\":     \"endpointState\",\n" +
		"    \"datatype\": \"jsonDocument\",\n" +
		"    \"multiple\": false,\n" +
		"    \"nullable\": true\n" +
		"    }, {\n" +
		"    \"name\":     \"workUnit\",\n" +
		"    \"datatype\": \"jsonDocument\",\n" +
		"    \"multiple\": false,\n" +
		"    \"nullable\": false\n" +
		"    } ],\n" +
		"  \"return\": {\n" +
		"    \"datatype\": \"jsonDocument\",\n" +
		"    \"multiple\": true,\n" +
		"    \"nullable\": true\n" +
		"  }\n" +
		"}\n";

	public static void main(String[] args) {
		final DatabaseClientFactory.SecurityContext authContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");
		final String host = "localhost";
		DatabaseClient stagingClient = DatabaseClientFactory.newClient(host, 8010, authContext);
		DatabaseClient finalClient = DatabaseClientFactory.newClient(host, 8011, authContext);

		DataMovementManager dmm = stagingClient.newDataMovementManager();
		final List<String> stagingForestIds = new ArrayList<>();
		for (Forest f : dmm.readForestConfig().listForests()) {
			stagingForestIds.add(f.getForestId());
		}

		List<String> finalForestIds = new ArrayList<>();
		for (Forest f : finalClient.newDataMovementManager().readForestConfig().listForests()) {
			finalForestIds.add(f.getForestId());
		}

		List<String> stepBatchResults = new ArrayList<>();

		final String jobId = "abc123";
		final String flowName = "ingestion_mapping_mastering-flow";
		final List<String> stepNumbers = Arrays.asList("2", "3");
		final String firstStepNumber = stepNumbers.get(0);
		final String lastStepNumber = stepNumbers.get(stepNumbers.size() - 1);

		JobService jobService = JobService.on(stagingClient);
		logger.info("Starting job");
		jobService.startJob(jobId, flowName, stepNumbers.get(0));

		for (String stepNumber : stepNumbers) {
			List<String> forestIds = stepNumber.equals("2") ? stagingForestIds : finalForestIds;
			QueryBatcher queryBatcher = dmm.newQueryBatcher(forestIds.iterator())
				.withBatchSize(1)
				.withThreadCount(3)
				.onUrisReady(batch -> {
					OutputEndpoint.BulkOutputCaller bulkCaller = OutputEndpoint.on(stagingClient, new StringHandle(JSON)).bulkCaller();

					ObjectNode state = IOTestUtil.mapper.createObjectNode();
					state.put("jobId", jobId);

					ObjectNode work = IOTestUtil.mapper.createObjectNode();
					work.put("flowName", flowName);
					ArrayNode steps = work.putArray("steps");
					steps.add(stepNumber);
					ArrayNode forestIdArray = work.putArray("forestIds");
					for (String item : batch.getItems()) {
						forestIdArray.add(item);
					}
					ObjectNode options = work.putObject("options");
					options.put("batchSize", 3);

					bulkCaller.setEndpointState(new ByteArrayInputStream(state.toString().getBytes()));
					bulkCaller.setWorkUnit(new ByteArrayInputStream(work.toString().getBytes()));
					bulkCaller.setOutputListener(value -> {
						logger.info("Adding output");
						stepBatchResults.add(NodeConverter.InputStreamToString(value));
					});
					logger.info("Awaiting bulkCaller completion");
					bulkCaller.awaitCompletion();
				});

			if (!stepNumber.equals(firstStepNumber)) {
				jobService.startStep(jobId, stepNumber);
			}

			dmm.startJob(queryBatcher);
			queryBatcher.awaitCompletion();
			dmm.stopJob(queryBatcher);

			logger.info("RESULTS: " + stepBatchResults);

			if (stepNumber.equals(lastStepNumber)) {
				logger.info("Finishing job");
				jobService.finishJob(jobId, stepNumber);
			} else {
				logger.info("Finishing step");
				jobService.finishStep(jobId, stepNumber);
			}
		}
	}
}
