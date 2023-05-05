package ca.pfv.spmf.Converter;

import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Item;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Sequence;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.CPT.CPTPlus.CPTPlusPredictor;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.Predictor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PlaylistCreator {

    private CPTPlusPredictor predictor;
    private List<Sequence> database;
    private HashMap<Integer, String> idNameHash;
    private HashMap<String, Integer> nameIdHash;


    public PlaylistCreator(HashMap<Integer, String> idNameHash, HashMap<String, Integer> nameIdHash) {
        this.idNameHash = idNameHash;
        this.nameIdHash = nameIdHash;
    }

    public PlaylistCreator setPredictor(CPTPlusPredictor predictor) {
        this.predictor = predictor;

        return this;
    }

    public PlaylistCreator setDatabase(List<Sequence> database) {
        this.database = database;

        return this;
    }


    public void train() {
        Helper.train(predictor, database);
    }

    private Sequence createSequence(List<Integer> songs) {
        var sequence = new Sequence(0);

        for (var song : songs) {
            sequence.addItem(new Item(song));
        }

        return sequence;
    }

    private List<String> idsToNames(List<Integer> ids) {
        var names = new ArrayList<String>();

        for (var id : ids) {
            names.add(idNameHash.get(id));
        }

        return names;
    }

    private List<Integer> namesToIds(List<String> names) {
        var ids = new ArrayList<Integer>();

        for (var name : names) {
            ids.add(nameIdHash.get(name));
        }

        return ids;
    }

    public List<String> createPlaylist(int playlistLength, List<Integer> history) {
        var remainingSongs = Math.max(0, playlistLength - history.size());
        var sequence = createSequence(history);
        var prediction = predictor.Predict(sequence);
        var countTable = predictor.getCountTable();

        var min = Math.min(remainingSongs, countTable.size());
        var scores = countTable.entrySet();

        var sortedScores = scores.stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .toArray();

        for (var i = 0; i < min; i++) {
            var entry = (HashMap.Entry<Integer, Integer>) sortedScores[i];
            var song = entry.getKey();
            history.add(song);
        }

//        for (int i = 0; i < remainingSongs; i++) {
//            if (prediction == null || prediction.size() == 0) break;
//
//            var predictedSong = prediction.get(0);
//            history.add(predictedSong.val);
//        }

        return idsToNames(history);
    }

    public static void main(String[] args) throws IOException {
        var inputPath = Helper.DATA_PATH;

        var trainingSet = new SequenceDatabase();
        trainingSet.loadFileSPMFFormat(inputPath, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

        var optionalParameters = "CCF:false CBS:true CCFmin:3 CCFmax:3 CCFsup:5 splitMethod:1 splitLength:30 minPredictionRatio:0 noiseRatio:0.5";
        var predictionModel = new CPTPlusPredictor("CPT+", optionalParameters);

        var labelPath = Helper.LABEL_PATH;
        var label = Helper.readLabel(labelPath);

        var playListCreator = new PlaylistCreator(label.idNameHash, label.nameIdHash);
        playListCreator.setPredictor(predictionModel);
        playListCreator.setDatabase(trainingSet.getSequences());
        playListCreator.train();

        var scanner = new java.util.Scanner(System.in);

        try {
            while (true) {
                System.out.println("Enter playlist length and history (separated by @@) or exit to quit");

                var input = scanner.nextLine();
                if (input.equals("exit")) break;

                var inputSplit = input.split("@@");
                var playlistLength = Integer.parseInt(inputSplit[0]);
                var mode = inputSplit[1];
                var historySplit = inputSplit[2].split("--");

                var ids = new ArrayList<Integer>();

                if (mode.equals("n")) {
                    for (var song : historySplit) {
                        ids.add(Integer.parseInt(song));
                    }
                } else {
                    for (var song : historySplit) {
                        ids.add(label.nameIdHash.get(song));
                    }
                }

                var songs = playListCreator.createPlaylist(playlistLength, ids);

                for (var song : songs) {
                    System.out.println(song);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
