package com.nextbreakpoint.flinkoperator.controller.command

import com.google.gson.Gson
import com.nextbreakpoint.flinkoperator.common.utils.FlinkServerUtils
import com.nextbreakpoint.flinkoperator.common.model.ClusterId
import com.nextbreakpoint.flinkoperator.common.model.FlinkOptions
import com.nextbreakpoint.flinkoperator.common.model.Result
import com.nextbreakpoint.flinkoperator.common.model.ResultStatus
import com.nextbreakpoint.flinkclient.model.ClusterOverviewWithVersion
import com.nextbreakpoint.flinkoperator.controller.OperatorCommand
import org.apache.log4j.Logger

class ClusterIsRunning(flinkOptions : FlinkOptions) : OperatorCommand<Void?, Boolean>(flinkOptions) {
    companion object {
        private val logger = Logger.getLogger(ClusterIsRunning::class.simpleName)
    }

    override fun execute(clusterId: ClusterId, params: Void?): Result<Boolean> {
        try {
            val flinkApi = FlinkServerUtils.find(flinkOptions, clusterId.namespace, clusterId.name)

            val response = flinkApi.getOverviewCall(null, null).execute()

            if (!response.isSuccessful) {
                return Result(
                    ResultStatus.FAILED,
                    false
                )
            }

            response.body().use {
                val overview = Gson().fromJson(it.source().readUtf8Line(), ClusterOverviewWithVersion::class.java)

                if (overview.slotsTotal > 0 && overview.taskmanagers > 0 && overview.jobsRunning >= 1) {
                    return Result(
                        ResultStatus.SUCCESS,
                        false
                    )
                } else if (overview.slotsTotal > 0 && overview.taskmanagers > 0 && overview.jobsFinished >= 1) {
                    return Result(
                        ResultStatus.SUCCESS,
                        true
                    )
                } else {
                    return Result(
                        ResultStatus.AWAIT,
                        false
                    )
                }
            }
        } catch (e : Exception) {
            logger.warn("Can't get overview of cluster ${clusterId.name}")

            return Result(
                ResultStatus.FAILED,
                false
            )
        }
    }
}