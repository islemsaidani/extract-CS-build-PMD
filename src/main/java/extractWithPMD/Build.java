package extractWithPMD;

import java.util.Arrays;
import java.util.List;

public class Build{
	
	public String buildId;
	public List<String> commits;
	public int build_failed;
	
	public Build( String buildId, int build_failed,List<String> commits) {
		this.buildId = buildId;
		this.commits = commits;
		this.build_failed = build_failed;
	}
	
	public String getBuildId() {
		return buildId;
	}
	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}
	public List<String> getCommits() {
		return commits;
	}
	public void setCommits(List<String> commits) {
		this.commits = commits;
	}
	public int getBuild_failed() {
		return build_failed;
	}
	public void setBuild_failed(int build_failed) {
		this.build_failed = build_failed;
	}
	public int getNbrCommits() {
		return commits.size();
	}

	@Override
	public String toString() {
		return "Build [buildId=" + buildId + ", commits=" + Arrays.toString(commits.toArray()) + ", build_failed=" + build_failed + "]";
	}
	
	
}
