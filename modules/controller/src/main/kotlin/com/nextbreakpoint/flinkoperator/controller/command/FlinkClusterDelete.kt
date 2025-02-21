package com.nextbreakpoint.flinkoperator.controller.command

import com.nextbreakpoint.flinkoperator.common.utils.KubernetesUtils
import com.nextbreakpoint.flinkoperator.common.model.ClusterId
import com.nextbreakpoint.flinkoperator.common.model.FlinkOptions
import com.nextbreakpoint.flinkoperator.common.model.Result
import com.nextbreakpoint.flinkoperator.common.model.ResultStatus
import com.nextbreakpoint.flinkoperator.controller.OperatorCommand
import io.kubernetes.client.models.V1DeleteOptions
import org.apache.log4j.Logger

class FlinkClusterDelete(flinkOptions: FlinkOptions) : OperatorCommand<Void?, Void?>(flinkOptions) {
    companion object {
        private val logger = Logger.getLogger(FlinkClusterDelete::class.simpleName)

        private val deleteOptions = V1DeleteOptions().propagationPolicy("Background")
    }

    override fun execute(clusterId: ClusterId, params: Void?): Result<Void?> {
        try {
            val response = KubernetesUtils.objectApi.deleteNamespacedCustomObjectWithHttpInfo(
                "nextbreakpoint.com",
                "v1",
                clusterId.namespace,
                "flinkclusters",
                clusterId.name,
                deleteOptions,
                null,
                null,
                null
            )

            if (response.statusCode == 200) {
                logger.info("Custom object deleted ${clusterId.name}")

                return Result(
                    ResultStatus.SUCCESS,
                    null
                )
            } else {
                logger.error("Can't delete custom object ${clusterId.name}")

                return Result(
                    ResultStatus.FAILED,
                    null
                )
            }
        } catch (e : Exception) {
            logger.error("Can't delete cluster resource ${clusterId.name}", e)

            return Result(
                ResultStatus.FAILED,
                null
            )
        }
    }
}