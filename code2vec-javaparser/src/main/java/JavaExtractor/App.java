package JavaExtractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.kohsuke.args4j.CmdLineException;

import JavaExtractor.Common.CommandLineValues;
import JavaExtractor.FeaturesEntities.ProgramRelation;

public class App {
	private static CommandLineValues s_CommandLineValues;

	public static void main(String[] args) {
		args = "--max_path_length 8 --max_path_width 2 --dir ../code2vec-satd/build-dataset/java-small/test --num_threads 10".split(" ");
		try {
			s_CommandLineValues = new CommandLineValues(args);
		} catch (CmdLineException e) {
			e.printStackTrace();
			return;
		}

		if (s_CommandLineValues.NoHash) {
			ProgramRelation.setNoHash();
		}

		if (s_CommandLineValues.File != null) {
			ExtractFeaturesTask extractFeaturesTask = new ExtractFeaturesTask(s_CommandLineValues,
					s_CommandLineValues.File.toPath());
			extractFeaturesTask.processFile();
		} else if (s_CommandLineValues.Dir != null) {
			extractDir();
		}
	}

	private static void extractDir() {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(s_CommandLineValues.NumThreads);
		LinkedList<ExtractFeaturesTask> tasks = new LinkedList<>();
		try {
			Files.walk(Paths.get(s_CommandLineValues.Dir)).filter(Files::isRegularFile)
					.filter(p -> p.toString().toLowerCase().endsWith(".java")).forEach(f -> {
						ExtractFeaturesTask task = new ExtractFeaturesTask(s_CommandLineValues, f);
						tasks.add(task);
					});
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			executor.invokeAll(tasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			executor.shutdown();
		}
	}
}
