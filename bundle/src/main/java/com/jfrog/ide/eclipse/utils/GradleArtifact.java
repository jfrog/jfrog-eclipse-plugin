package com.jfrog.ide.eclipse.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GradleArtifact {

	@JsonProperty("groupId")
	public String groupId;

	@JsonProperty("artifactId")
	public String artifactId;

	@JsonProperty("version")
	public String version;

	@JsonProperty("dependencies")
	public GradleArtifact[] dependencies;

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public GradleArtifact[] getDependencies() {
		return dependencies;
	}

	public void setDependencies(GradleArtifact[] dependencies) {
		this.dependencies = dependencies;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(groupId, artifactId, version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		GradleArtifact other = (GradleArtifact) obj;
		return Objects.equal(artifactId, other.artifactId) && Objects.equal(groupId, other.groupId)
				&& Objects.equal(version, other.version);
	}

}
