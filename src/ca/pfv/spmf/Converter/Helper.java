package ca.pfv.spmf.Converter;

import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Item;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Sequence;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.Predictor;

import java.io.*;
import java.util.*;

public class Helper {
    private static final String NAME_PATTERN = "@ITEM=";

    public static final String NAME_DATA = "TRACK_HISTORY";
    public static final String ROOT = "C:\\Users\\1\\IdeaProjects\\SPMF\\src\\ca\\pfv\\spmf\\Converter\\Data\\";

    public static final String DATA_PATH = ROOT + NAME_DATA + ".txt";
    public static final String TEST_PATH = ROOT + NAME_DATA + "_TEST.txt";

    public static final String LABEL_PATH = ROOT + NAME_DATA + "_LABEL.txt";

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

    public static String[] getRandomLinesFromFile(int percent, int minLength, int maxLength, int maxSplitLength, String path) throws FileNotFoundException {
        var lines = readFile(path);
        lines = convertSPMFToNormalFormat(lines);

        var random = new Random();

        var minLengthDataCount = 0;
        var maxLengthDataCount = 0;

        for (var line : lines) {
            var splitLine = line.split(" ");
            var length = splitLine.length;

            if (length >= minLength) minLengthDataCount++;
            if (length <= maxLength) maxLengthDataCount++;
        }
        var count = (int) (lines.size() * percent / 100.0);

        count = Math.min(count, minLengthDataCount);
        count = Math.min(count, maxLengthDataCount);

        var data = new String[count];

//        for (var i = 0; i < count; i++) {
//            var randomNumber = (int) (Math.random() * lines.length);
//            while (randomPool.contains(randomNumber)
//                    || lines[randomNumber].split(" ").length < minLength
//                    || lines[randomNumber].split(" ").length > maxLength)
//                randomNumber = (int) (Math.random() * lines.length);
//
//            randomPool.add(randomNumber);
//            data[i] = lines[randomNumber];
//        }

        Collections.shuffle(lines);
        var i = 0;
        var j = 0;
        do {
            if (j == lines.size()) break;

            var line = lines.get(j++);
            var splitLine = line.split(" ");
            var length = splitLine.length;

            if (length >= minLength && length <= maxLength) {
                var randomLength = random.nextInt(maxSplitLength - 1) + minLength;
                randomLength = Math.min(randomLength, length);

                if (randomLength != 0 && length > randomLength) {
                    Collections.shuffle(Arrays.asList(splitLine));
                    splitLine = Arrays.copyOf(splitLine, randomLength);

                    line = String.join(" ", splitLine);
                }

                data[i] = line;
                i++;
            }

        } while (i != count);

        return data;
    }

    public static ArrayList<String> convertSPMFToNormalFormat(ArrayList<String> data) {
        var newData = new ArrayList<String>();
        var i = 0;

        for (var s : data) {
            newData.add(s.replace(" -1", "").replace(" -2", ""));
        }

        return newData;
    }

    public static ArrayList<String> readFile(String path) throws FileNotFoundException {
        var file = new File(path);
        var reader = new Scanner(file);
        var lines = new ArrayList<String>();

        while (reader.hasNextLine()) {
            lines.add(reader.nextLine());
        }

        return lines;
    }

    public static void writeFile(String[] data, String path) throws IOException {
        var file = new File(path);
        var writer = new FileWriter(file);

        file.createNewFile();

        for (var s : data) {
            if (s == null) continue;

            writer.write(s + System.lineSeparator());
        }

        writer.close();
    }

    public static SequenceAndResult[] linesToSequences(ArrayList<String> lines) {
        var sequenceAndResults = new SequenceAndResult[lines.size()];

        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
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

    public static void train(Predictor predictor, List<Sequence> database){
        var stopWatch = new StopWatch();

        stopWatch.start();
        predictor.Train(database);
        stopWatch.stop();

        System.out.println("Node Number: " + predictor.size());
        System.out.println("Training Time: " + stopWatch.getElapsedTime() + " ms");
    }
    public static void trainAndPredict(Predictor predictor, List<Sequence> database, SequenceAndResult[] sequenceAndResults) {
        var stopWatch = new StopWatch();

        train(predictor, database);

        var accuracy = 0;
        var coverage = sequenceAndResults.length;

        stopWatch.start();
        for (var sequenceAndResult : sequenceAndResults) {
            var sequence = sequenceAndResult.sequence;
            var result = sequenceAndResult.result;

            var prediction = predictor.Predict(sequence);
            var items = prediction.getItems();
            if (items == null || items.size() == 0) {
                coverage--;
                continue;
            }

            var predictionResult = items.get(0).val;
            var isCorrect = predictionResult == result;
            if (isCorrect) accuracy++;

//            System.out.println("result: " + result + " prediction: " + predictionResult);
        }
        stopWatch.stop();

        System.out.println("Prediction Time: " + stopWatch.getElapsedTime() + " ms");
        System.out.println("Avg Prediction Time: " + stopWatch.getElapsedTime() * 1.0f / sequenceAndResults.length + " ms");

        System.out.println("Total: " + sequenceAndResults.length);
        System.out.println("Correct: " + accuracy);

        System.out.println("Accuracy: " + accuracy * 1.0f / sequenceAndResults.length);
        System.out.println("Coverage: " + coverage * 1.0f / sequenceAndResults.length);
    }

    public static Label readLabel(String path) throws FileNotFoundException {
        var file = new File(path);
        var reader = new Scanner(file);
        var label = new Label();

        while (reader.hasNextLine()) {
            var line = reader.nextLine();
            var splitLine = line.split("=");

            label.add(Integer.parseInt(splitLine[0]), splitLine[1]);
        }

        return label;
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

    public static class Label{
        public HashMap<Integer, String> idNameHash = new HashMap<>();
        public HashMap<String, Integer> nameIdHash = new HashMap<>();

        public void add(int id, String name){
            idNameHash.put(id, name);
            nameIdHash.put(name, id);
        }
    }
}
