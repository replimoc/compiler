package compiler.utils;

import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Ignore;

/**
 * common class to test that expected output matches compiler's output
 */
@Ignore
public class TestFileVisitor extends SimpleFileVisitor<Path> {

	public interface FileTester {
		void testSourceFile(Path sourceFilePath, Path expectedResultFilePath) throws Exception;
	}

	private static final String JAVA_EXTENSION = ".java";

	private final String expectedResultFileExtension;
	private final String sourceFileExtension;
	private final String sourceFile;

	private final PathMatcher matcher;
	private final FileTester fileTester;
	private final List<Entry<Path, Throwable>> failedTestsList = new ArrayList<>();

	public TestFileVisitor(String expectedResultFileExtension, FileTester fileTester) {
		this(JAVA_EXTENSION, expectedResultFileExtension, fileTester, null);
	}

	public TestFileVisitor(String expectedResultFileExtension, FileTester fileTester, String sourceFile) {
		this(JAVA_EXTENSION, expectedResultFileExtension, fileTester, sourceFile);
	}

	public TestFileVisitor(String sourceFileExtension, String expectedResultFileExtension,
			FileTester fileTester) {
		this(sourceFileExtension, expectedResultFileExtension, fileTester, null);
	}

	private TestFileVisitor(String sourceFileExtension, String expectedResultFileExtension,
			FileTester fileTester, String sourceFile) {
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
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			String parseFilename = name.toString();
			String sourceFilename = parseFilename.replace(expectedResultFileExtension, sourceFileExtension);

			Path sourceFilePath = file.getParent().resolve(sourceFilename);

			try {
				if (!Files.exists(sourceFilePath)) {
					Assert.fail("cannot find program to output " + sourceFilePath);
				}

				fileTester.testSourceFile(sourceFilePath, file);
			} catch (Throwable e) {
				printException(sourceFilePath, e);
				failedTestsList.add(new SimpleEntry<Path, Throwable>(sourceFilePath, e));
			}
		}
		return FileVisitResult.CONTINUE;
	}

	public void checkForFailedTests() {
		printFailedTestsMessages();
		if (failedTestsList.size() != 0) {
			Assert.fail("Tests for " + failedTestsList.size() + " test(s) failed");
		}
	}

	private void printFailedTestsMessages() {
		for (Entry<Path, Throwable> curr : failedTestsList) {
			printException(curr.getKey(), curr.getValue());
		}
		System.out.println("Tests fail for following files:");
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
}
