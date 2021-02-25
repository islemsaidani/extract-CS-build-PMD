package extractWithPMD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainProgram {
	/*
	 * list of top-10 java projects on TravisTorrent
	 */
	final static String[] top_10_java_projects = { 
			
			"apache/jackrabbit-oak", "CloudifySource/cloudify", "gradle/gradle",
			"Graylog2/graylog2-server", "HubSpot/Singularity", "orbeon/orbeon-forms", "owncloud/android",
			"perfectsense/brightspot-cms", "SonarSource/sonarqube", "square/okhttp"
			 };

	public static void main(String[] args) throws IOException {
		///////////////////////////////// load build information://////////////////////////////////////////////
		/*
		 * the csv file (data_top10) contains: gh_project_name:project name tr_build_id:
		 * build ID build_successful: Build result (TRUE=passed, FALSE=failed)
		 * git_all_built_commits: list of built commits separated by #
		 */
		final List<String> allbuiltCommits = Utils.readCSVData("data_top10.csv");// the data of all the studied projects

		HashMap<String, List<Build>> hashmap_build = new HashMap<String, List<Build>>();// create a hashmap

		for (String proj : top_10_java_projects) {
			// extract data
			List<Build> builds_proj = new ArrayList<Build>();
			for (String row : allbuiltCommits) {
				String[] infos = row.split(Utils.CSV_DELIMTER);
				if (infos[0].contains(proj)) {
					int buildRes = infos[2].toLowerCase().equals("false") ? 1 : 0;
					builds_proj.add(new Build(infos[1], // build ID
							buildRes, Arrays.asList(infos[3].split(Utils.TABLE_DELIMTER))// commits
					));
				}
			}
			hashmap_build.put(proj, builds_proj);
		}
		// start the CS detection using PMD
		for (String proj : hashmap_build.keySet()) {
			PMDExtractor pmd = new PMDExtractor(proj, hashmap_build.get(proj));
				pmd.start();
		}
	}
}
