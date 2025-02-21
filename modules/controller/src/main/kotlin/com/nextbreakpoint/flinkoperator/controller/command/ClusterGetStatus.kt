package com.nextbreakpoint.flinkoperator.controller.command

import com.nextbreakpoint.flinkoperator.common.model.ClusterId
import com.nextbreakpoint.flinkoperator.common.model.FlinkOptions
import com.nextbreakpoint.flinkoperator.common.model.Result
import com.nextbreakpoint.flinkoperator.common.model.ResultStatus
import com.nextbreakpoint.flinkoperator.controller.OperatorCache
import com.nextbreakpoint.flinkoperator.controller.OperatorCommand
import org.apache.log4j.Logger

class ClusterGetStatus(flinkOptions: FlinkOptions, val cache: OperatorCache) : OperatorCommand<Void?, Map<String, String>>(flinkOptions) {
    companion object {
        private val logger = Logger.getLogger(ClusterGetStatus::class.simpleName)
    }

    override fun execute(clusterId: ClusterId, params: Void?): Result<Map<String, String>> {
        try {
            val flinkCluster = cache.getFlinkCluster(clusterId)

            val result = flinkCluster.metadata.annotations?.filter { it.key.startsWith("nextbreakpoint.com/flink-operator-") }?.toMap() ?: mapOf()

            return Result(
                ResultStatus.SUCCESS,
                result
            )
        } catch (e : Exception) {
            logger.error("Can't get annotations of cluster ${clusterId.name}", e)

            return Result(
                ResultStatus.FAILED,
                mapOf()
            )
        }
    }
}