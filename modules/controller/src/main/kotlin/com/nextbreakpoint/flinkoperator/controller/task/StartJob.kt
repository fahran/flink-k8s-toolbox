package com.nextbreakpoint.flinkoperator.controller.task

import com.nextbreakpoint.flinkoperator.common.model.Result
import com.nextbreakpoint.flinkoperator.common.model.ResultStatus
import com.nextbreakpoint.flinkoperator.controller.OperatorTaskHandler
import com.nextbreakpoint.flinkoperator.controller.OperatorContext
import com.nextbreakpoint.flinkoperator.controller.OperatorTimeouts

class StartJob : OperatorTaskHandler {
    override fun onExecuting(context: OperatorContext): Result<String> {
        if (context.flinkCluster.spec?.flinkJob == null) {
            return Result(
                ResultStatus.FAILED,
                "Cluster ${context.flinkCluster.metadata.name} doesn't have a job"
            )
        }

        val elapsedTime = System.currentTimeMillis() - context.lastUpdated

        if (elapsedTime > OperatorTimeouts.STARTING_JOBS_TIMEOUT) {
            return Result(
                ResultStatus.FAILED,
                "Failed to start job of cluster ${context.flinkCluster.metadata.name} after ${elapsedTime / 1000} seconds"
            )
        }

        val response = context.controller.isJobStarted(context.clusterId)

        if (response.status == ResultStatus.SUCCESS) {
            return Result(
                ResultStatus.SUCCESS,
                "Job of cluster ${context.flinkCluster.metadata.name} already started"
            )
        }

        val runJarResponse = context.controller.runJar(context.clusterId, context.flinkCluster)

        if (runJarResponse.status == ResultStatus.SUCCESS) {
            return Result(
                ResultStatus.SUCCESS,
                "Stating job of cluster ${context.flinkCluster.metadata.name}..."
            )
        }

        return Result(
            ResultStatus.AWAIT,
            "Retry starting job of cluster ${context.flinkCluster.metadata.name}..."
        )
    }

    override fun onAwaiting(context: OperatorContext): Result<String> {
        val elapsedTime = System.currentTimeMillis() - context.lastUpdated

        if (elapsedTime > OperatorTimeouts.STARTING_JOBS_TIMEOUT) {
            return Result(
                ResultStatus.FAILED,
                "Failed to start job of cluster ${context.flinkCluster.metadata.name} after ${elapsedTime / 1000} seconds"
            )
        }

        val response = context.controller.isJobStarted(context.clusterId)

        if (response.status == ResultStatus.SUCCESS) {
            return Result(
                ResultStatus.SUCCESS,
                "Job of cluster ${context.flinkCluster.metadata.name} started in ${elapsedTime / 1000} seconds"
            )
        }

        return Result(
            ResultStatus.AWAIT,
            "Wait for creation of job of cluster ${context.flinkCluster.metadata.name}..."
        )
    }

    override fun onIdle(context: OperatorContext): Result<String> {
        return Result(
            ResultStatus.AWAIT,
            ""
        )
    }

    override fun onFailed(context: OperatorContext): Result<String> {
        return Result(
            ResultStatus.AWAIT,
            ""
        )
    }
}