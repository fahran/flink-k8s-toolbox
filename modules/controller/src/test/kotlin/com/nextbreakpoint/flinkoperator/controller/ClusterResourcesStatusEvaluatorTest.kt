package com.nextbreakpoint.flinkoperator.controller

import com.nextbreakpoint.flinkoperator.common.crd.V1FlinkCluster
import com.nextbreakpoint.flinkoperator.common.model.ClusterId
import com.nextbreakpoint.flinkoperator.common.model.ResourceStatus
import com.nextbreakpoint.flinkoperator.controller.resources.ClusterResources
import com.nextbreakpoint.flinkoperator.controller.resources.ClusterResourcesBuilder
import com.nextbreakpoint.flinkoperator.controller.resources.ClusterResourcesStatus
import com.nextbreakpoint.flinkoperator.controller.resources.ClusterResourcesStatusEvaluator
import com.nextbreakpoint.flinkoperator.controller.resources.DefaultClusterResourcesFactory
import com.nextbreakpoint.flinkoperator.controller.testing.TestFactory
import io.kubernetes.client.custom.Quantity
import io.kubernetes.client.models.V1EnvVar
import io.kubernetes.client.models.V1ObjectMeta
import io.kubernetes.client.models.V1Service
import io.kubernetes.client.models.V1ServiceSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(JUnitPlatform::class)
class ClusterResourcesStatusEvaluatorTest {
    private val statusEvaluator =
        ClusterResourcesStatusEvaluator()

    private val clusterId = UUID.randomUUID().toString()

    private val cluster = TestFactory.aCluster()

    private val identity = ClusterId(
        namespace = "test",
        name = cluster.metadata.name,
        uuid = clusterId
    )

    @Test
    fun `should return all valid resources when creating resources from base configuration`() {
        val expectedResources = createTestClusterResources(cluster)

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jarUploadJob.first).isEqualTo(ResourceStatus.VALID)
        assertThat(actualStatus.jobmanagerService.first).isEqualTo(ResourceStatus.VALID)
        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.VALID)
        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.VALID)
    }

    @Test
    fun `should return missing resource when the jobmanager service is not present`() {
        val expectedResources = createTestClusterResources(cluster).withJobManagerService(null)

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerService.first).isEqualTo(ResourceStatus.MISSING)
    }

    @Test
    fun `should return missing resource when the upload job is not present`() {
        val expectedResources = createTestClusterResources(cluster).withJarUploadJob(null)

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jarUploadJob.first).isEqualTo(ResourceStatus.MISSING)
    }

    @Test
    fun `should return missing resource when the jobmanager statefulset is not present`() {
        val expectedResources = createTestClusterResources(cluster).withJobManagerStatefulSet(null)

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.MISSING)
    }

    @Test
    fun `should return missing resource when the taskmanager statefulset is not present`() {
        val expectedResources = createTestClusterResources(cluster).withTaskManagerStatefulSet(null)

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.MISSING)
    }

    @Test
    fun `should return divergent resource when the jobmanager service does not have the expected labels`() {
        val jobmanagerService = V1Service()

        jobmanagerService.metadata = V1ObjectMeta()
        jobmanagerService.metadata.name = "flink-jobmanager-test"
        jobmanagerService.metadata.namespace = "flink"
        jobmanagerService.metadata.labels = mapOf()

        jobmanagerService.spec = V1ServiceSpec()
        jobmanagerService.spec.type = "ClusterIP"

        val expectedResources = createTestClusterResources(cluster).withJobManagerService(jobmanagerService)

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerService.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerService.second).hasSize(4)
    }

    @Test
    fun `should return divergent resource when the jobmanager service does not have the expected service mode`() {
        val jobmanagerService = V1Service()

        val labels = createLabels("flink-operator", "jobmanager", clusterId, cluster.metadata.name)

        jobmanagerService.metadata = V1ObjectMeta()
        jobmanagerService.metadata.name = "flink-jobmanager-test"
        jobmanagerService.metadata.namespace = "flink"
        jobmanagerService.metadata.labels = labels

        jobmanagerService.spec = V1ServiceSpec()
        jobmanagerService.spec.type = "NodePort"

        val expectedResources = createTestClusterResources(cluster).withJobManagerService(jobmanagerService)

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerService.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerService.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the jobmanager statefulset does not have the expected labels`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jobmanagerStatefulSet?.metadata?.labels = mapOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerStatefulSet.second).hasSize(4)
    }

    @Test
    fun `should return divergent resource when the jobmanager statefulset does not have the expected service account`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jobmanagerStatefulSet?.spec?.template?.spec?.serviceAccountName = "xxx"

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the jobmanager statefulset does not have the expected pull secrets`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jobmanagerStatefulSet?.spec?.template?.spec?.imagePullSecrets = listOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the jobmanager statefulset does not have the expected pull secrets name`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jobmanagerStatefulSet?.spec?.template?.spec?.imagePullSecrets?.get(0)?.name = "xxx"

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the jobmanager statefulset does not have the expected volume claims`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jobmanagerStatefulSet?.spec?.volumeClaimTemplates = listOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the jobmanager statefulset does not have containers`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jobmanagerStatefulSet?.spec?.template?.spec?.containers = listOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the jobmanager statefulset does not have the expected container image`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jobmanagerStatefulSet?.spec?.template?.spec?.containers?.get(0)?.image = "xxx"

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the jobmanager statefulset does not have the expected container pull policy`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jobmanagerStatefulSet?.spec?.template?.spec?.containers?.get(0)?.imagePullPolicy = "xxx"

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the jobmanager statefulset does not have the expected container cpu limits`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jobmanagerStatefulSet?.spec?.template?.spec?.containers?.get(0)?.resources?.limits?.set("cpu", Quantity("2.0"))

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the jobmanager statefulset does not have the expected container memory limits`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jobmanagerStatefulSet?.spec?.template?.spec?.containers?.get(0)?.resources?.requests?.set("memory", Quantity("100Mi"))

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the jobmanager statefulset does not have the expected container environment variables`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jobmanagerStatefulSet?.spec?.template?.spec?.containers?.get(0)?.env = listOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerStatefulSet.second).hasSize(5)
    }

    @Test
    fun `should return divergent resource when the jobmanager statefulset does not have the internal expected container environment variables`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jobmanagerStatefulSet?.spec?.template?.spec?.containers?.get(0)?.env = listOf(V1EnvVar().name("key").value("value"))

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jobmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jobmanagerStatefulSet.second).hasSize(5)
    }

    @Test
    fun `should return divergent resource when the taskmanager statefulset does not have the expected labels`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.taskmanagerStatefulSet?.metadata?.labels = mapOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.taskmanagerStatefulSet.second).hasSize(4)
    }

    @Test
    fun `should return divergent resource when the taskmanager statefulset does not have the expected service account`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.taskmanagerStatefulSet?.spec?.template?.spec?.serviceAccountName = "xxx"

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.taskmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the taskmanager statefulset does not have the expected pull secrets`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.taskmanagerStatefulSet?.spec?.template?.spec?.imagePullSecrets = listOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.taskmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the taskmanager statefulset does not have the expected pull secrets name`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.taskmanagerStatefulSet?.spec?.template?.spec?.imagePullSecrets?.get(0)?.name = "xxx"

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.taskmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the taskmanager statefulset does not have the expected volume claims`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.taskmanagerStatefulSet?.spec?.volumeClaimTemplates = listOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.taskmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the taskmanager statefulset does not have containers`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.taskmanagerStatefulSet?.spec?.template?.spec?.containers = listOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.taskmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the taskmanager statefulset does not have the expected container image`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.taskmanagerStatefulSet?.spec?.template?.spec?.containers?.get(0)?.image = "xxx"

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.taskmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the taskmanager statefulset does not have the expected container pull policy`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.taskmanagerStatefulSet?.spec?.template?.spec?.containers?.get(0)?.imagePullPolicy = "xxx"

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.taskmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the taskmanager statefulset does not have the expected container cpu limits`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.taskmanagerStatefulSet?.spec?.template?.spec?.containers?.get(0)?.resources?.limits?.set("cpu", Quantity("2.0"))

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.taskmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the taskmanager statefulset does not have the expected container memory limits`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.taskmanagerStatefulSet?.spec?.template?.spec?.containers?.get(0)?.resources?.requests?.set("memory", Quantity("100Mi"))

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.taskmanagerStatefulSet.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the taskmanager statefulset does not have the expected container environment variables`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.taskmanagerStatefulSet?.spec?.template?.spec?.containers?.get(0)?.env = listOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.taskmanagerStatefulSet.second).hasSize(6)
    }

    @Test
    fun `should return divergent resource when the taskmanager statefulset does not have the internal expected container environment variables`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.taskmanagerStatefulSet?.spec?.template?.spec?.containers?.get(0)?.env = listOf(V1EnvVar().name("key").value("value"))

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.taskmanagerStatefulSet.second).hasSize(6)
    }

    @Test
    fun `should return divergent resource when the taskmanager statefulset does not have the expected number of replicas`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.taskmanagerStatefulSet?.spec?.replicas = 4

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.taskmanagerStatefulSet.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.taskmanagerStatefulSet.second).hasSize(1)
    }

    private fun printStatus(clusterResourcesStatus: ClusterResourcesStatus) {
        clusterResourcesStatus.jarUploadJob.second.forEach { println("uploadJob job: ${it}") }

        clusterResourcesStatus.jobmanagerService.second.forEach { println("jobmanager service: ${it}") }

        clusterResourcesStatus.jobmanagerStatefulSet.second.forEach { println("jobmanager statefulset: ${it}") }

        clusterResourcesStatus.taskmanagerStatefulSet.second.forEach { println("taskmanager statefulset: ${it}") }
    }

    @Test
    fun `should return divergent resource when the upload job does not have the expected labels`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jarUploadJob?.metadata?.labels = mapOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jarUploadJob.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jarUploadJob.second).hasSize(3)
    }

    @Test
    fun `should return divergent resource when the upload job does not have the expected service account`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jarUploadJob?.spec?.template?.spec?.serviceAccountName = "xxx"

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jarUploadJob.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jarUploadJob.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the upload job does not have the expected pull secrets`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jarUploadJob?.spec?.template?.spec?.imagePullSecrets = listOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jarUploadJob.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jarUploadJob.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the upload job does not have the expected pull secrets name`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jarUploadJob?.spec?.template?.spec?.imagePullSecrets?.get(0)?.name = "xxx"

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jarUploadJob.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jarUploadJob.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the upload job does not have containers`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jarUploadJob?.spec?.template?.spec?.containers = listOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jarUploadJob.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jarUploadJob.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the upload job does not have the expected container image`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jarUploadJob?.spec?.template?.spec?.containers?.get(0)?.image = "xxx"

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jarUploadJob.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jarUploadJob.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the upload job does not have the expected container pull policy`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jarUploadJob?.spec?.template?.spec?.containers?.get(0)?.imagePullPolicy = "xxx"

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jarUploadJob.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jarUploadJob.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the upload job does not have the expected container arguments`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jarUploadJob?.spec?.template?.spec?.containers?.get(0)?.args = listOf()

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jarUploadJob.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jarUploadJob.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the upload job does not have the expected job argument`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jarUploadJob?.spec?.template?.spec?.containers?.get(0)?.args = listOf("xxx", "jar")

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jarUploadJob.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jarUploadJob.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the upload job does not have the expected job arguments`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jarUploadJob?.spec?.template?.spec?.containers?.get(0)?.args = listOf("upload", "xxx")

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jarUploadJob.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jarUploadJob.second).hasSize(1)
    }

    @Test
    fun `should return divergent resource when the upload job does not have the expected job jar arguments`() {
        val expectedResources = createTestClusterResources(cluster)

        expectedResources.jarUploadJob?.spec?.template?.spec?.containers?.get(0)?.args = listOf("upload", "jar")

        val actualStatus = statusEvaluator.evaluate(identity, cluster, expectedResources)

        printStatus(actualStatus)

        assertThat(actualStatus.jarUploadJob.first).isEqualTo(ResourceStatus.DIVERGENT)
        assertThat(actualStatus.jarUploadJob.second).hasSize(3)
    }

    private fun createLabels(
        clusterOwner: String,
        role: String,
        clusterId: String,
        clusterName: String
    ): Map<String, String> {
        val componentLabel = Pair("component", "flink")

        val clusterLabel = Pair("name", clusterName)

        val clusterIdLabel = Pair("uid", clusterId)

        val ownerLabel = Pair("owner", clusterOwner)

        val roleLabel = Pair("role", role)

        return mapOf(ownerLabel, clusterLabel, clusterIdLabel, componentLabel, roleLabel)
    }

    private fun createTestClusterResources(cluster: V1FlinkCluster): ClusterResources {
        val targetResources = ClusterResourcesBuilder(
            DefaultClusterResourcesFactory,
            "test",
            clusterId,
            "flink-operator",
            cluster
        ).build()

        return ClusterResources(
            jarUploadJob = targetResources.jarUploadJob,
            jobmanagerService = targetResources.jobmanagerService,
            jobmanagerStatefulSet = targetResources.jobmanagerStatefulSet,
            taskmanagerStatefulSet = targetResources.taskmanagerStatefulSet
        )
    }
}