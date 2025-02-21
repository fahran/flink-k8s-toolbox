package com.nextbreakpoint.flinkoperator.common.crd;

import com.google.gson.annotations.SerializedName;
import io.kubernetes.client.models.V1EnvFromSource;
import io.kubernetes.client.models.V1EnvVar;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1Volume;
import io.kubernetes.client.models.V1VolumeMount;

import java.util.List;
import java.util.Objects;

public class V1JobManagerSpec {
    @SerializedName("requiredCPUs")
    private Float requiredCPUs;
    @SerializedName("requiredMemory")
    private Integer requiredMemory;
    @SerializedName("environment")
    private List<V1EnvVar> environment;
    @SerializedName("environmentFrom")
    private List<V1EnvFromSource> environmentFrom;
    @SerializedName("serviceMode")
    private String serviceMode;
    @SerializedName("serviceAccount")
    private String serviceAccount;
    @SerializedName("volumes")
    private List<V1Volume> volumes;
    @SerializedName("volumeMounts")
    private List<V1VolumeMount> volumeMounts;
    @SerializedName("persistentVolumeClaimsTemplates")
    private List<V1PersistentVolumeClaim> persistentVolumeClaimsTemplates;

    public Float getRequiredCPUs() {
        return requiredCPUs;
    }

    public V1JobManagerSpec setRequiredCPUs(Float requiredCPUs) {
        this.requiredCPUs = requiredCPUs;
        return this;
    }

    public Integer getRequiredMemory() {
        return requiredMemory;
    }

    public V1JobManagerSpec setRequiredMemory(Integer requiredMemory) {
        this.requiredMemory = requiredMemory;
        return this;
    }

    public List<V1EnvVar> getEnvironment() {
        return environment;
    }

    public V1JobManagerSpec setEnvironment(List<V1EnvVar> environment) {
        this.environment = environment;
        return this;
    }

    public List<V1EnvFromSource> getEnvironmentFrom() {
        return environmentFrom;
    }

    public void setEnvironmentFrom(List<V1EnvFromSource> environmentFrom) {
        this.environmentFrom = environmentFrom;
    }

    public String getServiceMode() {
        return serviceMode;
    }

    public V1JobManagerSpec setServiceMode(String serviceMode) {
        this.serviceMode = serviceMode;
        return this;
    }

    public String getServiceAccount() {
        return serviceAccount;
    }

    public V1JobManagerSpec setServiceAccount(String serviceAccount) {
        this.serviceAccount = serviceAccount;
        return this;
    }

    public List<V1Volume> getVolumes() {
        return volumes;
    }

    public V1JobManagerSpec setVolumes(List<V1Volume> volumes) {
        this.volumes = volumes;
        return this;
    }

    public List<V1VolumeMount> getVolumeMounts() {
        return volumeMounts;
    }

    public V1JobManagerSpec setVolumeMounts(List<V1VolumeMount> volumeMounts) {
        this.volumeMounts = volumeMounts;
        return this;
    }

    public List<V1PersistentVolumeClaim> getPersistentVolumeClaimsTemplates() {
        return persistentVolumeClaimsTemplates;
    }

    public V1JobManagerSpec setPersistentVolumeClaimsTemplates(List<V1PersistentVolumeClaim> persistentVolumeClaimsTemplates) {
        this.persistentVolumeClaimsTemplates = persistentVolumeClaimsTemplates;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V1JobManagerSpec that = (V1JobManagerSpec) o;
        return Objects.equals(getRequiredCPUs(), that.getRequiredCPUs()) &&
                Objects.equals(getRequiredMemory(), that.getRequiredMemory()) &&
                Objects.equals(getEnvironment(), that.getEnvironment()) &&
                Objects.equals(getEnvironmentFrom(), that.getEnvironmentFrom()) &&
                Objects.equals(getServiceMode(), that.getServiceMode()) &&
                Objects.equals(getServiceAccount(), that.getServiceAccount()) &&
                Objects.equals(getVolumes(), that.getVolumes()) &&
                Objects.equals(getVolumeMounts(), that.getVolumeMounts()) &&
                Objects.equals(getPersistentVolumeClaimsTemplates(), that.getPersistentVolumeClaimsTemplates());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRequiredCPUs(), getRequiredMemory(), getEnvironment(), getEnvironmentFrom(), getServiceMode(), getServiceAccount(), getVolumes(), getVolumeMounts(), getPersistentVolumeClaimsTemplates());
    }

    @Override
    public String toString() {
        return "V1JobManagerSpec{" +
                "requiredCPUs=" + requiredCPUs +
                ", requiredMemory=" + requiredMemory +
                ", environment=" + environment +
                ", environmentFrom=" + environmentFrom +
                ", serviceMode='" + serviceMode + '\'' +
                ", serviceAccount='" + serviceAccount + '\'' +
                ", volumes='" + volumes + '\'' +
                ", volumeMounts='" + volumeMounts + '\'' +
                ", persistentVolumeClaimsTemplates='" + persistentVolumeClaimsTemplates + '\'' +
                '}';
    }
}
