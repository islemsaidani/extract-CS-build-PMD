package extractWithPMD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class Utils {

	public static final String CSV_DELIMTER = ",";
	public static final String TABLE_DELIMTER = "#";
	public static final String PMD_DIR = "C:\\pmd-bin-6.31.0\\bin";

	public static String DIRECTORY_GIT = "C:/gitProj/";

	public static void writeInCSV(StringBuilder sb, String file) {

		if (new File(file).exists()) {
			System.out.println(file + " already exists!");
		} else {
			try {
				final PrintWriter writer = new PrintWriter(new File(file));
				writer.write(sb.toString());
				writer.close();
				System.out.println("you file is ready.. please check " + file);
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	public static void cloneProj(String proj) {
		try {
			File f = new File(Utils.DIRECTORY_GIT + proj);
			if (!f.exists()) {
				System.out.println("cloning " + proj + " project..");
				try {
					Git.cloneRepository().setBranch("master").setDirectory(f).setURI("https://github.com/" + proj)
							.call();
				} catch (org.eclipse.jgit.dircache.InvalidPathException e) {
					e.printStackTrace();
				}
			}
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("resource")
	public static List<String> readCSVData(String pathToData) throws IOException {
		List<String> res = new ArrayList<String>();
		String line = "";
		BufferedReader br = new BufferedReader(new FileReader(pathToData));
		br.readLine();// header
		while ((line = br.readLine()) != null) {
			res.add(line);
		}
		return res;
	}

}
