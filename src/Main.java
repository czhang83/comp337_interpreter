import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Main {

    static Parser parser = new Parser(); // FIXME change this line to use your code if necessary
    static Interpreter interpreter = new Interpreter(); // FIXME change this line to use your code if necessary
    static ConstantFoldingTransform transformer = new ConstantFoldingTransform(); // FIXME change this line to use your code if necessary

    static class SExpParse {
        String sexp = "";
        int index = -1;

        public SExpParse(String sexp, int index) {
            this.sexp = sexp;
            this.index = index;
        }
    }

    public static String normalizeSExpression(String sexp) {
        SExpParse result = parseSExpression(sexp, 0);
        if (result.index != sexp.length()) {
            System.out.println("Malformed S-Expression");
            System.exit(1);
            return "Malformed S-Expression";
        } else {
            return result.sexp;
        }
    }

    public static SExpParse parseWhitespace(String sexp, int index) {
        while (index < sexp.length() && (sexp.charAt(index) == ' ' || sexp.charAt(index) == '\n')) {
            index++;
        }
        return new SExpParse("", index);
    }

    public static SExpParse parseAtom(String sexp, int index) {
        index = parseWhitespace(sexp, index).index;
        // match an atom
        String word = "";
        while (index < sexp.length()
                && sexp.charAt(index) != ' ' && sexp.charAt(index) != '\n'
                && sexp.charAt(index) != '(' && sexp.charAt(index) != ')') {
            word += sexp.charAt(index);
            index++;
        }
        if (word.length() == 0) {
            return new SExpParse("", -1);
        } else {
            return new SExpParse(word, index);
        }
    }

    public static SExpParse parseSExpression(String sexp, int index) {
        if (index == sexp.length()) {
            return new SExpParse("", index);
        }
        // skip whitespace
        index = parseWhitespace(sexp, index).index;
        // match an atom
        if (index < sexp.length() && sexp.charAt(index) != '(') {
            return parseAtom(sexp, index);
        }
        SExpParse atom = parseAtom(sexp, index + 1);
        String result = "(" + atom.sexp;
        index = atom.index;
        while (true) {
            // skip whitespace
            index = parseWhitespace(sexp, index).index;
            SExpParse child = parseSExpression(sexp, index);
            if (child.index == -1 || child.sexp.equals("")) {
                break;
            }
            result += " " + child.sexp;
            index = child.index;
        }
        index = parseWhitespace(sexp, index).index;
        if (index < sexp.length() && sexp.charAt(index) != ')') {
            return new SExpParse("", -1);
        } else {
            index = parseWhitespace(sexp, index + 1).index;
            return new SExpParse(result + ")", index);
        }
    }

    public static class Finder extends SimpleFileVisitor<Path> {
        // from https://docs.oracle.com/javase/tutorial/essential/io/find.html

        private final PathMatcher matcher;

        Finder(String pattern) {
            this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        }

        // invoke the pattern matching method on each file.
        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
            Path name = path.getFileName();
            if (name != null && this.matcher.matches(name)) {
                testWithFile(path);
            }
            return CONTINUE;
        }

    }

    private static String fixNewlines(String string) {
        return String.join("\n", string.split("\\r?\\n"));
    }

    public static void testSExp(Path sexpPath, Parse parse, String message) throws IOException {
        if (!Files.exists(sexpPath)) {
            return;
        }
        String expectedSExp = normalizeSExpression(fixNewlines(Files.readString(sexpPath)));
        String actualSExp = normalizeSExpression(parse.toString());
        if (!actualSExp.equals(expectedSExp)) {
            System.out.println("EXPECTED S-EXPRESSION (multiple whitespaces are ignored)");
            System.out.println(expectedSExp);
            System.out.println("ACTUAL S-EXPRESSION (multiple whitespaces are ignored)");
            System.out.println(actualSExp);
            throw new AssertionError(message);
        }
    }

    public static void testWithFile(Path langPath) {
        try {
            // read the program to run
            String program = fixNewlines(Files.readString(langPath));
            System.out.println();
            System.out.println("RUNNING " + langPath + ":");
            System.out.println(program);
            // parse the code
            Parse parse = Main.parser.parse(program);
            String actualOutput = null;
            if (parse == null) {
                // if there's a syntax error, that's our only output
                actualOutput = "syntax error";
            } else {
                // otherwise, check against the intermediate representation
                Path sexpPath = langPath.resolveSibling(langPath.getFileName().toString().replace(".lang", ".sexp"));
                testSExp(sexpPath, parse, "intermediate representation does not match");
                // check against the transformed intermediate representation
                Path sexp2Path = langPath.resolveSibling(langPath.getFileName().toString().replace(".lang", ".sexp2"));
                parse = transformer.visit(parse);
                testSExp(sexp2Path, parse, "transformed intermediate representation does not match");
                // run the program to get the output
                actualOutput = Main.interpreter.execute(parse);
            }
            // read the expected output
            Path outPath = langPath.resolveSibling(langPath.getFileName().toString().replace(".lang", ".out"));
            String expectedOutput = Files.readString(outPath);
            // process the expected and actual output to deal with newlines
            expectedOutput = fixNewlines(expectedOutput);
            actualOutput = fixNewlines(actualOutput);
            // check against the output
            if (!actualOutput.equals(expectedOutput)) {
                String message = "\n\n";
                message += "EXPECTED OUTPUT:\n";
                message += expectedOutput + "\n";
                message += "\n";
                message += "ACTUAL OUTPUT:\n";
                message += actualOutput + "\n";
                throw new AssertionError(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testWithDirectory(Path dirPath) {
        try {
            Files.walkFileTree(dirPath, new Finder("*.lang"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        for (String arg : args) {
            Path path = Paths.get(arg);
            if (Files.isDirectory(path)) {
                Main.testWithDirectory(path);
            } else {
                Main.testWithFile(path);
            }
        }
    }
}
