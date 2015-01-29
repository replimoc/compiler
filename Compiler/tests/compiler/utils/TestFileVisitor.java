package compiler.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Ignore;

/**
 * common class to test that expected output matches compiler's output
 */
@Ignore
public class TestFileVisitor extends SimpleFileVisitor<Path> {

	private static final int CHECK_TIMEOUT = 200;
	private static final int NUMBER_OF_THREADS = 1;

	public interface FileTester {
		void testSourceFile(TestFileVisitor visitor, Path sourceFilePath, Path expectedResultFilePath) throws Exception;
	}

	public static final String JAVA_EXTENSION = ".java";
	public static final String MINIJAVA_EXTENSION = ".mj";

	private final String expectedResultFileExtension;
	private final String sourceFileExtension;
	private final String sourceFile;

	private final PathMatcher matcher;
	private final FileTester fileTester;
	private final List<Entry<Path, Throwable>> failedTestsList = new ArrayList<>();

	private final ExecutorService threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS, Utils.getThreadFactory(Utils.DEFAULT_STACK_SIZE_MB));
	private final Set<Path> currentlyWorkedFiles = Collections.synchronizedSet(new HashSet<Path>());

	private int numberOfTests = 0;

	public static void runTests(TestFileVisitor.FileTester visitor, String folder, String sourceFileExtension, String destinationFileExtension)
			throws IOException {
		TestFileVisitor tester = new TestFileVisitor(sourceFileExtension, destinationFileExtension, visitor);
		Files.walkFileTree(Paths.get(folder), tester);
		tester.checkForFailedTests();
	}

	public static void runTest(TestFileVisitor.FileTester visitor, String folder, String sourceFileExtension, String destinationFileExtension,
			String javaTestFileName)
			throws IOException {
		TestFileVisitor tester = new TestFileVisitor(sourceFileExtension, destinationFileExtension, visitor, javaTestFileName);
		Files.walkFileTree(Paths.get(folder), tester);
		tester.checkForFailedTests();
	}

	public static void runTestsForFolder(TestFileVisitor.FileTester visitor, String folder) throws IOException {
		TestFileVisitor.runTests(visitor, folder, TestFileVisitor.JAVA_EXTENSION, TestFileVisitor.JAVA_EXTENSION);
		TestFileVisitor.runTests(visitor, folder, TestFileVisitor.MINIJAVA_EXTENSION, TestFileVisitor.MINIJAVA_EXTENSION);
	}

	public TestFileVisitor(String expectedResultFileExtension, FileTester fileTester) {
		this(JAVA_EXTENSION, expectedResultFileExtension, fileTester, null);
	}

	public TestFileVisitor(String expectedResultFileExtension, FileTester fileTester, String sourceFile) {
		this(JAVA_EXTENSION, expectedResultFileExtension, fileTester, sourceFile);
	}

	public TestFileVisitor(String sourceFileExtension, String expectedResultFileExtension, FileTester fileTester) {
		this(sourceFileExtension, expectedResultFileExtension, fileTester, null);
	}

	private TestFileVisitor(String sourceFileExtension, String expectedResultFileExtension, FileTester fileTester, String sourceFile) {
		this.expectedResultFileExtension = expectedResultFileExtension;
		this.sourceFile = sourceFile;
		if (this.sourceFile == null) {
			this.matcher = FileSystems.getDefault().getPathMatcher("glob:*" + expectedResultFileExtension);
		} else {
			this.matcher = FileSystems.getDefault().getPathMatcher("glob:*" + sourceFile + expectedResultFileExtension);
		}
		this.fileTester = fileTester;
		this.sourceFileExtension = sourceFileExtension;
	}

	@Override
	public FileVisitResult visitFile(final Path file, BasicFileAttributes attrs) {
		final Path name = file.getFileName();

		if (name != null && matcher.matches(name)) {
			numberOfTests++;

			Runnable task = new Runnable() {
				@Override
				public void run() {
					try {
						currentlyWorkedFiles.add(file);
						testFile(file, name.toString());
					} finally {
						currentlyWorkedFiles.remove(file);
					}
				}
			};
			threadPool.submit(task);
		}

		return FileVisitResult.CONTINUE;
	}

	private void testFile(Path file, String fileName) {
		Path sourceFilePath = getFileWithEnding(file, expectedResultFileExtension, getSourceFileExtension());

		try {
			if (!Files.exists(sourceFilePath)) {
				Assert.fail("cannot find program to output " + sourceFilePath);
			}

			fileTester.testSourceFile(this, sourceFilePath, file);
		} catch (Throwable e) {
			testFailed(sourceFilePath, e);
		}
	}

	public Path getFileWithEnding(Path sourceFilePath, String newEnding) {
		return getFileWithEnding(sourceFilePath, getSourceFileExtension(), newEnding);
	}

	public static Path getFileWithEnding(Path sourceFilePath, String oldEnding, String newEnding) {
		String fileName = sourceFilePath.getFileName().toString();
		String newFileName = fileName.replace(oldEnding, newEnding);
		return sourceFilePath.getParent().resolve(newFileName);
	}

	private void testFailed(Path sourceFilePath, Throwable e) {
		printException(sourceFilePath, e);
		failedTestsList.add(new SimpleEntry<Path, Throwable>(sourceFilePath, e));
	}

	public void checkForFailedTests() {
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(CHECK_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (Path notFinishedFile : currentlyWorkedFiles) {
			failedTestsList.add(new SimpleEntry<Path, Throwable>(notFinishedFile, new RuntimeException("Testfile did not terminate!")));
		}

		printFailedTestsMessages();
		if (failedTestsList.size() != 0) {
			Assert.fail("Tests for " + failedTestsList.size() + " test(s) failed");
		}
	}

	private void printFailedTestsMessages() {
		for (Entry<Path, Throwable> curr : failedTestsList) {
			printException(curr.getKey(), curr.getValue());
		}
		System.out.printf("Tests fail for following files (%d of %d):\n", failedTestsList.size(), numberOfTests);
		for (Entry<Path, Throwable> curr : failedTestsList) {
			System.out.println("* " + curr.getKey());
		}
	}

	private void printException(Path file, Throwable exception) {
		System.err.println();
		System.err.println(">>>>>~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~<<<<<");
		System.err.println("Test for file = " + file + " failed");
		exception.printStackTrace();
		System.err.println("^^^^^~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~^^^^^");
		System.err.println();
	}

	public String getSourceFileExtension() {
		return sourceFileExtension;
	}
}
