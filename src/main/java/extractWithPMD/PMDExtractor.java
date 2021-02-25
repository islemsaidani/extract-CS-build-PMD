package extractWithPMD;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class PMDExtractor extends Thread {

	String project;
	List<Build> builds_proj;
	public StringBuilder codeSmells = new StringBuilder("buildID,build_Failed,codeSmell,nbrOcc\n");

	public PMDExtractor(String urlWithoutGit, List<Build> builds_proj) {
		this.project = urlWithoutGit;
		this.builds_proj = builds_proj;
	}

	public StringBuilder getCodeSmells() {
		return codeSmells;
	}

	@Override
	public void run() {
		try {
			System.out.println("/////////////////////  "+project+"  /////////////////////");
			Utils.cloneProj( project);//clone GIT
			for (Build build : builds_proj) {
				System.out.println(project+" ********** BUILD (" + build.buildId + ")");
				extractCS(build);
			}
			Utils.writeInCSV(codeSmells, project.replaceAll("/", "-") + "_data.csv");
			// delete the project directory
			try {
				FileUtils.deleteDirectory(new File(Utils.DIRECTORY_GIT + project));
			} catch (final IOException e) {
				System.err.println("Please delete the directory " + Utils.DIRECTORY_GIT + project + " manually");

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * we use Command line to run PMD
	 */
	public void extractCS(Build build) throws IOException {
		String local_dir = Utils.DIRECTORY_GIT + project;
		String commit = build.commits.get(build.getNbrCommits() - 1);// extract code version at the time of the last
																		// built commit
		String code_version = local_dir + "/versions_builds/" + build.buildId;

		new File(local_dir + "/versions_builds/").mkdir();
		new File(code_version).mkdir();
		/*
		 * use git worktree to extract the code version at the time of a commit using
		 * the command line PS. you should install "git" on your machine to run the
		 * command. You should also download PMD and set the path of the bin folder (
		 * PMD_DIR in Utils.java)
		 */
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
				"cd \"" + local_dir + "\" && " + "git worktree add \"" + code_version + "\" " + commit);

		builder.redirectErrorStream(true);
		Process p = builder.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line2;
		while ((line2 = r.readLine()) != null) {
			if (line2.contains("fatal")) {
				System.out.println(project+": "+Arrays.toString(builder.command().toArray()));
				System.out.println(project+" ********** ERROR IN COMMIT " + commit + ":\n" + line2);
			}
		}
		if (new File(code_version).exists()) {
			// detect code smells
			codeSmells.append(
					runPMD("\"" + code_version + "/\" ", build.buildId, build.build_failed, project).toString());
		} else
			System.out.println("VERSION PROBLEM" + Arrays.toString(builder.command().toArray()));

		// delete the version
		try {
			FileUtils.deleteDirectory(new File(code_version));
		} catch (final IOException e) {
		}
	}

	private StringBuilder runPMD(String dir, String buildID, int build_failed, String proj) throws IOException {

		
		/*
		 * 
		 * List of used CS:
		 */
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
				"cd \"" + Utils.PMD_DIR + "\" && " + "pmd -dir " + dir + " -format csv -R "
						+ "category/java/design.xml/GodClass" + ",category/java/design.xml/NPathComplexity"
						+ ",category/java/design.xml/CyclomaticComplexity"
						// + ",category/java/bestpractices.xml/LooseCoupling"
						+ ",category/java/design.xml/DataClass"
		// + ",category/java/design.xml/ExcessiveParameterList"
		// + ",category/java/design.xml/TooManyMethods,"
		// + ",category/java/design.xml/TooManyFields"
		);

		builder.redirectErrorStream(true);
		Process p = builder.start();
		StringBuilder codeSmells = new StringBuilder("");
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		String[] details = null;
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
			line = line.replaceAll("\"", "").replaceAll("NPathComplexity", "GodMethod")
					.replaceAll("Problem,Package,File,Priority,Line,Description,Rule set,Rule", "");
			details = line.split(",");
			if (details.length > 7) { // header : "Problem","Package","File","Priority","Line","Description","Rule
										// set","Rule"
				codeSmells.append(buildID + "," + build_failed + "," + details[details.length - 1] + ",1" + '\n');
			} // else System.out.println(line);
		}
		codeSmells.append(duplicatedCode(dir, buildID, build_failed)); // detect duplicated code
		if (!codeSmells.toString().contains("GodClass")) {
			codeSmells.append(buildID + "," + build_failed + ",GodClass,0" + '\n');
		}
		if (!codeSmells.toString().contains("GodMethod")) {
			codeSmells.append(buildID + "," + build_failed + ",GodMethod,0" + '\n');
		}
		if (!codeSmells.toString().contains("CyclomaticComplexity")) {
			codeSmells.append(buildID + "," + build_failed + ",CyclomaticComplexity,0" + '\n');
		}
		if (!codeSmells.toString().contains("DataClass")) {
			codeSmells.append(buildID + "," + build_failed + ",DataClass,0" + '\n');
		}
		// delete extracted version
		try {
			FileUtils.deleteDirectory(new File("C:/git/" + proj + "/version/"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return codeSmells;
	}

	private StringBuilder duplicatedCode(String dir, String buildID, int build_failed) throws IOException {

		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd \"" + Utils.PMD_DIR + "\" && "
				+ ".\\cpd.bat --minimum-tokens 100 --files " + dir + " --format xml");
		// System.out.println( Arrays.toString(builder.command().toArray()));
		builder.redirectErrorStream(true);
		Process p = builder.start();
		StringBuilder s = new StringBuilder("");
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		int nbr = 0;
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
			if (line.contains("</duplication>")) { // header : lines,tokens,occurrences
				nbr++;
			}
		}
		s.append(buildID + "," + build_failed + ",DuplicatedCode," + nbr + '\n');
		return s;
	}

}
