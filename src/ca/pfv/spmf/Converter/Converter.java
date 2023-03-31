package ca.pfv.spmf.Converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Converter {
    private static final String NAME_PATTERN = "@ITEM=";

    public static void splitNameData(String inputPath, String outSequencePath, String outNamePath) throws IOException {
        var inputFile = new File(inputPath);
        var outSequenceFile = new File(outSequencePath);
        var outNameFile = new File(outNamePath);

        var inputData = "";
        var outSequenceData = new StringBuilder();
        var outNameData = new StringBuilder();
        var sequenceDataWriter = new FileWriter(outSequenceFile);
        var nameDataWriter = new FileWriter(outNameFile);

        nameDataWriter.write("");
        sequenceDataWriter.write("");

        outSequenceFile.createNewFile();
        outNameFile.createNewFile();

        inputData = readFile(inputFile);
        var splitLines = inputData.split("\n");

        for (var line : splitLines) {
            if (isNameLine(line)) {
                var name = getName(line);
                outNameData.append(name).append("\n");
            } else {
                outSequenceData.append(line).append("\n");
            }
        }

        nameDataWriter.write(outNameData.toString());
        sequenceDataWriter.write(outSequenceData.toString());
    }

    private static String getName(String line) {
        return line.split(NAME_PATTERN)[1];
    }

    private static boolean isNameLine(String line) {
        return line.contains(NAME_PATTERN);
    }

    public static String readFile(File file) throws FileNotFoundException {
        var reader = new Scanner(file);
        var data = new StringBuilder();

        while (reader.hasNextLine()) {
            data.append(reader.nextLine() + "\n");
        }

        return data.toString();
    }
}
