package org.jfrog.eclipse.utils;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + Arrays.hashCode(dependencies);
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GradleArtifact other = (GradleArtifact) obj;
		if (artifactId == null) {
			if (other.artifactId != null)
				return false;
		} else if (!artifactId.equals(other.artifactId))
			return false;
		if (!Arrays.equals(dependencies, other.dependencies))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

}
