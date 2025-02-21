package com.nextbreakpoint.flinkoperator.controller

import com.nextbreakpoint.flinkoperator.common.utils.KubernetesUtils
import com.nextbreakpoint.flinkoperator.common.model.ClusterId
import com.nextbreakpoint.flinkoperator.common.model.FlinkOptions
import com.nextbreakpoint.flinkoperator.common.model.OperatorTask
import com.nextbreakpoint.flinkoperator.common.model.Result
import com.nextbreakpoint.flinkoperator.common.model.SavepointOptions
import com.nextbreakpoint.flinkoperator.common.model.SavepointRequest
import com.nextbreakpoint.flinkoperator.common.model.StartOptions
import com.nextbreakpoint.flinkoperator.common.model.StopOptions
import com.nextbreakpoint.flinkoperator.common.crd.V1FlinkCluster
import com.nextbreakpoint.flinkoperator.controller.command.ClusterCreateResources
import com.nextbreakpoint.flinkoperator.controller.command.ClusterDeleteResources
import com.nextbreakpoint.flinkoperator.controller.command.ClusterGetStatus
import com.nextbreakpoint.flinkoperator.controller.command.ClusterIsReady
import com.nextbreakpoint.flinkoperator.controller.command.ClusterIsRunning
import com.nextbreakpoint.flinkoperator.controller.command.ClusterIsSuspended
import com.nextbreakpoint.flinkoperator.controller.command.ClusterIsTerminated
import com.nextbreakpoint.flinkoperator.controller.command.ClusterStart
import com.nextbreakpoint.flinkoperator.controller.command.ClusterStop
import com.nextbreakpoint.flinkoperator.controller.command.SavepointCreate
import com.nextbreakpoint.flinkoperator.controller.command.ClusterUpdateSavepoint
import com.nextbreakpoint.flinkoperator.controller.command.ClusterUpdateStatus
import com.nextbreakpoint.flinkoperator.controller.command.FlinkClusterCreate
import com.nextbreakpoint.flinkoperator.controller.command.FlinkClusterDelete
import com.nextbreakpoint.flinkoperator.controller.command.JarIsReady
import com.nextbreakpoint.flinkoperator.controller.command.JarRemove
import com.nextbreakpoint.flinkoperator.controller.command.JarRun
import com.nextbreakpoint.flinkoperator.controller.command.JarUpload
import com.nextbreakpoint.flinkoperator.controller.command.JobCancel
import com.nextbreakpoint.flinkoperator.controller.command.JobHasStarted
import com.nextbreakpoint.flinkoperator.controller.command.JobHasStopped
import com.nextbreakpoint.flinkoperator.controller.command.JobStop
import com.nextbreakpoint.flinkoperator.controller.command.PodsAreTerminated
import com.nextbreakpoint.flinkoperator.controller.command.PodsRestart
import com.nextbreakpoint.flinkoperator.controller.command.PodsTerminate
import com.nextbreakpoint.flinkoperator.controller.command.SavepointGetStatus
import com.nextbreakpoint.flinkoperator.controller.command.SavepointTrigger
import com.nextbreakpoint.flinkoperator.controller.command.UploadJobDeleteResource
import com.nextbreakpoint.flinkoperator.controller.resources.ClusterResources

class OperatorController(val flinkOptions: FlinkOptions) {
    fun startCluster(clusterId: ClusterId, options: StartOptions, cache: OperatorCache) : Result<List<OperatorTask>> =
        ClusterStart(flinkOptions, cache).execute(clusterId, options)

    fun stopCluster(clusterId: ClusterId, options: StopOptions, cache: OperatorCache) : Result<List<OperatorTask>> =
        ClusterStop(flinkOptions, cache).execute(clusterId, options)

    fun createSavepoint(clusterId: ClusterId, cache: OperatorCache) : Result<List<OperatorTask>> =
        SavepointCreate(flinkOptions, cache).execute(clusterId, null)

    fun getClusterStatus(clusterId: ClusterId, cache: OperatorCache) : Result<Map<String, String>> =
        ClusterGetStatus(flinkOptions, cache).execute(clusterId, null)

    fun createFlinkCluster(clusterId: ClusterId, flinkCluster: V1FlinkCluster) : Result<Void?> =
        FlinkClusterCreate(flinkOptions).execute(clusterId, flinkCluster)

    fun deleteFlinkCluster(clusterId: ClusterId) : Result<Void?> =
        FlinkClusterDelete(flinkOptions).execute(clusterId, null)

    fun updateClusterStatus(clusterId: ClusterId, flinkCluster: V1FlinkCluster, resources: OperatorResources) : Result<Void?> =
        ClusterUpdateStatus(this, resources).execute(clusterId, flinkCluster)

    fun createClusterResources(clusterId: ClusterId, clusterResources: ClusterResources) : Result<Void?> =
        ClusterCreateResources(flinkOptions).execute(clusterId, clusterResources)

    fun removeJar(clusterId: ClusterId) : Result<Void?> =
        JarRemove(flinkOptions).execute(clusterId, null)

    fun isJarReady(clusterId: ClusterId) : Result<Void?> =
        JarIsReady(flinkOptions).execute(clusterId, null)

    fun triggerSavepoint(clusterId: ClusterId, options: SavepointOptions) : Result<SavepointRequest?> =
        SavepointTrigger(flinkOptions).execute(clusterId, options)

    fun getSavepointStatus(clusterId: ClusterId, savepointRequest: SavepointRequest) : Result<String> =
        SavepointGetStatus(flinkOptions).execute(clusterId, savepointRequest)

    fun deleteClusterResources(clusterId: ClusterId) : Result<Void?> =
        ClusterDeleteResources(flinkOptions).execute(clusterId, null)

    fun deleteUploadJobResource(clusterId: ClusterId) : Result<Void?> =
        UploadJobDeleteResource(flinkOptions).execute(clusterId, null)

    fun terminatePods(clusterId: ClusterId) : Result<Void?> =
        PodsTerminate(flinkOptions).execute(clusterId, null)

    fun restartPods(clusterId: ClusterId, clusterResources: ClusterResources): Result<Void?> =
        PodsRestart(flinkOptions).execute(clusterId, clusterResources)

    fun arePodsTerminated(clusterId: ClusterId): Result<Void?> =
        PodsAreTerminated(flinkOptions).execute(clusterId, null)

    fun runJar(clusterId: ClusterId, cluster: V1FlinkCluster) : Result<Void?> =
        JarRun(flinkOptions).execute(clusterId, cluster)

    fun cancelJob(clusterId: ClusterId, options: SavepointOptions): Result<SavepointRequest?> =
        JobCancel(flinkOptions).execute(clusterId, options)

    fun stopJob(clusterId: ClusterId): Result<Map<String, String>> =
        JobStop(flinkOptions).execute(clusterId, null)

    fun isClusterReady(clusterId: ClusterId): Result<Void?> =
        ClusterIsReady(flinkOptions).execute(clusterId, null)

    fun isClusterRunning(clusterId: ClusterId): Result<Boolean> =
        ClusterIsRunning(flinkOptions).execute(clusterId, null)

    fun isClusterSuspended(clusterId: ClusterId): Result<Void?> =
        ClusterIsSuspended(flinkOptions).execute(clusterId, null)

    fun isClusterTerminated(clusterId: ClusterId): Result<Void?> =
        ClusterIsTerminated(flinkOptions).execute(clusterId, null)

    fun isJobStarted(clusterId: ClusterId): Result<Void?> =
        JobHasStarted(flinkOptions).execute(clusterId, null)

    fun isJobStopped(clusterId: ClusterId): Result<Void?> =
        JobHasStopped(flinkOptions).execute(clusterId, null)

    fun uploadJar(clusterId: ClusterId, clusterResources: ClusterResources): Result<Void?> =
        JarUpload(flinkOptions).execute(clusterId, clusterResources)

    fun updateSavepoint(clusterId: ClusterId, savepointPath: String): Result<Void?> =
        ClusterUpdateSavepoint(flinkOptions).execute(clusterId, savepointPath)

    fun updateAnnotations(clusterId: ClusterId, annotations: Map<String, String>) {
        KubernetesUtils.updateAnnotations(clusterId, annotations)
    }
}