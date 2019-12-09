package com.marklogic.client.test.dataservices;

// IMPORTANT: Do not edit. This file is generated.

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.impl.BaseProxy;
import com.marklogic.client.io.Format;

/**
 * Provides a set of operations on the database server
 */
public interface JobService {
    /**
     * Creates a JobService object for executing operations on the database server.
     *
     * The DatabaseClientFactory class can create the DatabaseClient parameter. A single
     * client object can be used for any number of requests and in multiple threads.
     *
     * @param db	provides a client for communicating with the database server
     * @return	an object for session state
     */
    static JobService on(DatabaseClient db) {
        final class JobServiceImpl implements JobService {
            private BaseProxy baseProxy;

            private JobServiceImpl(DatabaseClient dbClient) {
                baseProxy = new BaseProxy(dbClient, "/dataservices/job/");
            }

            @Override
            public com.fasterxml.jackson.databind.JsonNode startStep(String jobId, String stepNumber) {
              return BaseProxy.JsonDocumentType.toJsonNode(
                baseProxy
                .request("startStep.sjs", BaseProxy.ParameterValuesKind.MULTIPLE_ATOMICS)
                .withSession()
                .withParams(
                    BaseProxy.atomicParam("jobId", false, BaseProxy.StringType.fromString(jobId)),
                    BaseProxy.atomicParam("stepNumber", false, BaseProxy.StringType.fromString(stepNumber)))
                .withMethod("POST")
                .responseSingle(false, Format.JSON)
                );
            }


            @Override
            public com.fasterxml.jackson.databind.JsonNode finishStep(String jobId, String stepNumber) {
              return BaseProxy.JsonDocumentType.toJsonNode(
                baseProxy
                .request("finishStep.sjs", BaseProxy.ParameterValuesKind.MULTIPLE_ATOMICS)
                .withSession()
                .withParams(
                    BaseProxy.atomicParam("jobId", false, BaseProxy.StringType.fromString(jobId)),
                    BaseProxy.atomicParam("stepNumber", false, BaseProxy.StringType.fromString(stepNumber)))
                .withMethod("POST")
                .responseSingle(false, Format.JSON)
                );
            }


            @Override
            public com.fasterxml.jackson.databind.JsonNode startJob(String jobId, String flowName, String stepNumber) {
              return BaseProxy.JsonDocumentType.toJsonNode(
                baseProxy
                .request("startJob.sjs", BaseProxy.ParameterValuesKind.MULTIPLE_ATOMICS)
                .withSession()
                .withParams(
                    BaseProxy.atomicParam("jobId", false, BaseProxy.StringType.fromString(jobId)),
                    BaseProxy.atomicParam("flowName", false, BaseProxy.StringType.fromString(flowName)),
                    BaseProxy.atomicParam("stepNumber", false, BaseProxy.StringType.fromString(stepNumber)))
                .withMethod("POST")
                .responseSingle(false, Format.JSON)
                );
            }


            @Override
            public com.fasterxml.jackson.databind.JsonNode finishJob(String jobId, String stepNumber) {
              return BaseProxy.JsonDocumentType.toJsonNode(
                baseProxy
                .request("finishJob.sjs", BaseProxy.ParameterValuesKind.MULTIPLE_ATOMICS)
                .withSession()
                .withParams(
                    BaseProxy.atomicParam("jobId", false, BaseProxy.StringType.fromString(jobId)),
                    BaseProxy.atomicParam("stepNumber", false, BaseProxy.StringType.fromString(stepNumber)))
                .withMethod("POST")
                .responseSingle(false, Format.JSON)
                );
            }

        }

        return new JobServiceImpl(db);
    }

  /**
   * Invokes the startStep operation on the database server
   *
   * @param jobId	provides input
   * @param stepNumber	provides input
   * @return	as output
   */
    com.fasterxml.jackson.databind.JsonNode startStep(String jobId, String stepNumber);

  /**
   * Invokes the finishStep operation on the database server
   *
   * @param jobId	provides input
   * @param stepNumber	provides input
   * @return	as output
   */
    com.fasterxml.jackson.databind.JsonNode finishStep(String jobId, String stepNumber);

  /**
   * Invokes the startJob operation on the database server
   *
   * @param jobId	provides input
   * @param flowName	provides input
   * @param stepNumber	provides input
   * @return	as output
   */
    com.fasterxml.jackson.databind.JsonNode startJob(String jobId, String flowName, String stepNumber);

  /**
   * Invokes the finishJob operation on the database server
   *
   * @param jobId	provides input
   * @param stepNumber	provides input
   * @return	as output
   */
    com.fasterxml.jackson.databind.JsonNode finishJob(String jobId, String stepNumber);

}
