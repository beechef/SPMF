package ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReadDataCsv {
    public static void main(String[] args) throws FileNotFoundException, InterruptedException, ParseException {

        var inputPath = "C:\\Users\\1\\Desktop\\Data\\data.csv";
        var inputFile = new File(inputPath);
        var inputReader = new Scanner(inputFile);

        var labelOutputPath = "C:\\Users\\1\\Desktop\\Data\\label.txt";
        var labelOutputFile = new File(labelOutputPath);
        var labelOutputWriter = new java.io.PrintWriter(labelOutputFile);

        var sequenceOutputPath = "C:\\Users\\1\\Desktop\\Data\\sequence.txt";
        var sequenceOutputFile = new File(sequenceOutputPath);
        var sequenceOutputWriter = new java.io.PrintWriter(sequenceOutputFile);

        inputReader.nextLine();

        var format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX");

        var preDay = 0;
        var preMonth = 0;
        var preYear = 0;

        var preUser = "";
        var sequence = new StringBuilder();
        var labels = new HashMap<String, Integer>();
        var labelIdCount = 0;

        var sequences = new ArrayList<String>();

        var sequenceCount = 0;
        var itemCount = 0;
        var totalLength = 0;
        var avgLength = 0;

        while (inputReader.hasNext()) {

            var line = inputReader.nextLine();
            var split = line.split(",");

            var user = split[0];
            var date = split[1];
            var songName = split[2];

            var formatedDate = format.parse(date);
            var calendar = Calendar.getInstance();
            calendar.setTime(formatedDate);

            var day = calendar.get(Calendar.DAY_OF_MONTH);
            var month = calendar.get(Calendar.MONTH);
            var year = calendar.get(Calendar.YEAR);

            if (!labels.containsKey(songName)) {
                labels.put(songName, labelIdCount);
                songName = String.valueOf(labelIdCount);

                labelIdCount++;
            } else {
                songName = String.valueOf(labels.get(songName));
            }

            totalLength++;

            if (day != preDay || month != preMonth || year != preYear || !user.equals(preUser)) {
                preDay = day;
                preMonth = month;
                preYear = year;
                preUser = user;

                if (sequence.length() != 0) {
                    sequence.delete(sequence.length() - 4, sequence.length());
                    sequence.append(" -2\n");

                    sequences.add(sequence.toString());
                }

                sequence = new StringBuilder();
            }

            sequence.append(songName).append(" -1 ");
        }

        sequences.add(sequence.toString());
        Collections.shuffle(sequences);

        var percent = 50;
        var trainCount = sequences.size() * percent / 100;
        sequences = new ArrayList<>(sequences.subList(0, trainCount));

        for (var tmp : sequences) {
            sequenceOutputWriter.write(tmp);
        }

        for (var entry : labels.entrySet()) {
            labelOutputWriter.write(entry.getValue() + "=" + entry.getKey() + "\n");
        }

        itemCount = labelIdCount;
        sequenceCount = sequences.size();
        avgLength = totalLength / sequenceCount;

        System.out.println("Sequence Count: " + sequenceCount);
        System.out.println("Item Count: " + itemCount);
        System.out.println("Avg Length: " + avgLength);
    }
}
