package compiler.perf;

import compiler.StringTable;
import compiler.lexer.Lexer;
import compiler.parser.Parser;
import compiler.parser.ParserException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TODO document me
 */
public class ParserPerf {

    static class RoundReader extends Reader {

        private final String content;
        private StringReader reader;
        int repeat;

        RoundReader(Path filePath, int repeat) throws IOException {
            byte[] content = Files.readAllBytes(filePath);
            this.content = new String(content);
            reader = new StringReader(this.content);
            this.repeat = repeat;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int read = reader.read(cbuf, off, len);
            if(read < 0)
            {
                repeat--;
                if(repeat <= 0)
                {
                    return -1;
                }
                reader = new StringReader(this.content);
                read = reader.read(cbuf, off, len);
            }
            return read;
        }

        @Override
        public int read() throws IOException {
            int c = reader.read();
            if(c < 0)
            {
                repeat--;
                if(repeat <= 0)
                {
                    return -1;
                }
                reader = new StringReader(this.content);
                c = reader.read();
            }
            return c;
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }

        public void reset(int repeat)
        {
            this.repeat = repeat;
        }
    }

    public static long measureParser(Reader reader) throws IOException, ParserException {
        Lexer lexer = new Lexer(reader, new StringTable());
        Parser parser = new Parser(lexer);

        long startTime = System.currentTimeMillis();
        parser.parse();
        return System.currentTimeMillis() - startTime;
    }


    public static void main(String[] args) throws IOException, ParserException {
        Path path = Paths.get("testdata/parser5/PerformanceGrammar.java");
        int repeat = 1000  ;
        int numMeasures = 100;

        DescriptiveStatistics stats = new DescriptiveStatistics();
        RoundReader reader = new RoundReader(path,repeat);

        for (int j = 0; j < numMeasures; j++)
        {
            long estimatedTime = measureParser(reader);
            stats.addValue((double)estimatedTime);
            reader.reset(repeat);
        }
        System.out.println("results for " + path + "(repeat " + repeat + " times)");
        System.out.printf("\t mean(ms) = %.3f\n",stats.getMean());
//        System.out.println("\t  min(ns) = " + stats.getMin());
        System.out.printf("\tstdev(ms) = %.3f\n" ,stats.getStandardDeviation());
        System.out.printf("\t stdev(%%) = %.2f\n", stats.getStandardDeviation()/stats.getMean() * 100);

    }

}
