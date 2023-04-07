package ca.pfv.spmf.Converter;

import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Item;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Sequence;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.Predictor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public static String[] getRandomLinesFromFile(int count, int minLength, int maxLength, String path) throws FileNotFoundException {
        var lines = readFile(path);
        lines = convertSPMFToNormalFormat(lines);

        var randomPool = new ArrayList<Integer>();

        var minLengthDataCount = 0;
        var maxLengthDataCount = 0;

        for (var line : lines) {
            var splitLine = line.split(" ");
            var length = splitLine.length;

            if (length >= minLength) minLengthDataCount++;
            if (length <= maxLength) maxLengthDataCount++;
        }
        count = Math.min(count, minLengthDataCount);
        count = Math.min(count, maxLengthDataCount);

        var data = new String[count];

        for (var i = 0; i < count; i++) {
            var randomNumber = (int) (Math.random() * lines.length);
            while (randomPool.contains(randomNumber)
                    || lines[randomNumber].split(" ").length < minLength
                    || lines[randomNumber].split(" ").length > maxLength)
                randomNumber = (int) (Math.random() * lines.length);

            randomPool.add(randomNumber);
            data[i] = lines[randomNumber];
        }

        return data;
    }

    public static String[] convertSPMFToNormalFormat(String[] data) {
        var newData = new String[data.length];
        var i = 0;

        for (var s : data) {
            newData[i++] = s.replace(" -1", "").replace(" -2", "");
        }

        return newData;
    }

    public static String[] readFile(String path) throws FileNotFoundException {
        var file = new File(path);
        var reader = new Scanner(file);
        var lines = new ArrayList<String>();

        while (reader.hasNextLine()) {
            lines.add(reader.nextLine());
        }

        return lines.toArray(new String[0]);
    }

    public static void writeFile(String[] data, String path) throws IOException {
        var file = new File(path);
        var writer = new FileWriter(file);

        file.createNewFile();

        for (var s : data) {
            writer.write(s + System.lineSeparator());
        }

        writer.close();
    }

    public static SequenceAndResult[] linesToSequences(String[] lines) {
        var sequenceAndResults = new SequenceAndResult[lines.length];

        for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            var splitLine = line.split(" ");
            var sequenceAndResult = new SequenceAndResult();
            var sequence = new Sequence(0);

            for (var j = 0; j < splitLine.length - 1; j++) {
                sequence.addItem(new Item(Integer.parseInt(splitLine[j])));
            }

            sequenceAndResult.sequence = sequence;
            sequenceAndResult.result = Integer.parseInt(splitLine[splitLine.length - 1]);

            sequenceAndResults[i] = sequenceAndResult;
        }

        return sequenceAndResults;
    }

    public static void trainAndPredict(Predictor predictor, List<Sequence> database, SequenceAndResult[] sequenceAndResults) {
        var stopWatch = new StopWatch();

        stopWatch.start();
        predictor.Train(database);
        stopWatch.stop();

        System.out.println("Node Number: " + predictor.size());
        System.out.println("Training Time: " + stopWatch.getElapsedTime() + " ms");

        var accuracy = 0;

        stopWatch.start();
        for (var sequenceAndResult : sequenceAndResults) {
            var sequence = sequenceAndResult.sequence;
            var result = sequenceAndResult.result;

            var prediction = predictor.Predict(sequence);
            var items = prediction.getItems();
            if (items == null || items.size() == 0) continue;

            var predictionResult = items.get(0).val;
            var isCorrect = predictionResult == result;
            if (isCorrect) accuracy++;

//            System.out.println("result: " + result + " prediction: " + predictionResult);
        }
        stopWatch.stop();

        System.out.println("Prediction Time: " + stopWatch.getElapsedTime() + " ms");

        System.out.println("Total: " + sequenceAndResults.length);
        System.out.println("Correct: " + accuracy);

        System.out.println("Accuracy: " + accuracy * 1.0f / sequenceAndResults.length);
    }

    public static class SequenceAndResult {
        public Sequence sequence;
        public int result;
    }

    public static class StopWatch {
        private long startTime;
        private long endTime;

        public void start() {
            startTime = System.currentTimeMillis();
        }

        public void stop() {
            endTime = System.currentTimeMillis();
        }

        public long getElapsedTime() {
            return endTime - startTime;
        }
    }
}
