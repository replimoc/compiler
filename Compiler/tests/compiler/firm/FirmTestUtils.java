package compiler.firm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.Ignore;

import compiler.ast.declaration.MemberDeclaration;
import compiler.firm.generation.FirmGenerationVisitor;
import compiler.utils.TestUtils;
import firm.Dump;
import firm.Graph;
import firm.Program;

@Ignore
public final class FirmTestUtils {
	private FirmTestUtils() {
	}

	public static void exportFirmProgram(String saveFolder) throws IOException {
		Path outputFolder = Paths.get(saveFolder);
		Files.createDirectories(outputFolder);

		for (Graph graph : Program.getGraphs()) {
			graph.check();

			Path dumpedFile = dumpGraph(graph);
			Path newFileName = createCleanFileName(saveFolder, dumpedFile);
			Files.move(dumpedFile, newFileName, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public static void assertGraphEquals(String assertionFolder) {
		assertExportEquals(assertionFolder, Program.getGraphs());
	}

	public static void assertExportEquals(String assertionFolder, Graph... graphs) {
		assertExportEquals(assertionFolder, Arrays.asList(graphs));
	}

	public static void assertExportEquals(String assertionFolder, Iterable<Graph> graphs) {
		try {
			for (Graph graph : Program.getGraphs()) {
				graph.check();

				Path actualFile = dumpGraph(graph);
				Path expectedFile = createCleanFileName(assertionFolder, actualFile);

				try {
					assertFilesEqual(expectedFile, actualFile);
				} finally {
					Files.delete(actualFile);
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void assertExportEquals(String assertionFolder, String javaFile, boolean export) throws Exception {
		compiler.ast.Program ast = TestUtils.getAstForFile(javaFile);

		for (MemberDeclaration classMember : ast.getClasses().get(0).getMembers()) {
			classMember.accept(new FirmGenerationVisitor());
		}

		if (export) {
			FirmTestUtils.exportFirmProgram(assertionFolder);
			fail("The assert export is overwritten every time: " + javaFile);
		} else {
			FirmTestUtils.assertExportEquals(assertionFolder);
		}
	}

	private static void assertFilesEqual(Path expectedFile, Path actualFile) throws IOException {
		if (!Files.exists(expectedFile))
			fail("No expectedFile found (" + expectedFile + ")");

		List<String> expectedLines = Files.readAllLines(expectedFile, StandardCharsets.US_ASCII);
		List<String> actualLines = Files.readAllLines(actualFile, StandardCharsets.US_ASCII);

		Iterator<String> expectedIter = expectedLines.iterator();
		Iterator<String> actualIter = actualLines.iterator();

		int line = 0;
		while (expectedIter.hasNext() && actualIter.hasNext()) {
			line++;
			assertEquals("Expected file " + expectedFile + " and actual file " + actualFile + " do not match in line " + line,
					expectedIter.next(), actualIter.next());
		}

		assertEquals("Expected file " + expectedFile + " and actual file " + actualFile + " do not have the same length.",
				expectedLines.size(), actualLines.size());
	}

	private static Path dumpGraph(Graph graph) throws FileNotFoundException {
		UUID uuid = UUID.randomUUID();
		Dump.dumpGraph(graph, "--" + uuid);
		Path dumpedFile = findFile(uuid.toString());
		return dumpedFile;
	}

	private static Path createCleanFileName(String saveFolder, Path dumpedFile) {
		String dumpedFileName = dumpedFile.getFileName().toString();
		Path newFileName = Paths.get(saveFolder + "/" + dumpedFileName.substring(0, dumpedFileName.length() - 43) + ".vcg");
		return newFileName;
	}

	private static Path findFile(String uuid) throws FileNotFoundException {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("."))) {
			for (Path path : directoryStream) {
				if (path.toString().endsWith(uuid + ".vcg")) {
					return path;
				}
			}
		} catch (IOException ex) {
		}

		throw new FileNotFoundException("Was not able to find graph with uuid " + uuid);
	}
}