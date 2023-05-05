package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.Converter.Helper;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Sequence;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceStatsGenerator;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.CPT.CPTPlus.CPTPlusPredictor;

/**
 * Example of how to use the CPT+ sequence prediction model in the source code.
 * Copyright 2015.
 */
public class MainTestCPTPlus {

    public static void main(String[] arg) throws IOException, ClassNotFoundException {

        // Load the set of training sequences
        String inputPath = Helper.DATA_PATH;

        SequenceDatabase trainingSet = new SequenceDatabase();
        trainingSet.loadFileSPMFFormat(inputPath, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

        // Print the training sequences to the console
        System.out.println("--- Training sequences ---");
        for (Sequence sequence : trainingSet.getSequences()) {
            System.out.println(sequence.toString());
        }
        System.out.println();

        // Print statistics about the training sequences
        SequenceStatsGenerator.prinStats(trainingSet, " training sequences ");

        // The following line is to set optional parameters for the prediction model.
        // We want to
        // activate the CCF and CBS strategies which generally improves its performance (see paper)
        String optionalParameters = "CCF:false CBS:true CCFmin:3 CCFmax:3 CCFsup:5 splitMethod:1 splitLength:30 minPredictionRatio:0 noiseRatio:0.1";
        // Here is a brief description of the parameter used in the above line:
        //  CCF:true  --> activate the CCF strategy
        //  CBS:true -->  activate the CBS strategy
        //  CCFmax:6 --> indicate that the CCF strategy will not use pattern having more than 6 items
        //  CCFsup:2 --> indicate that a pattern is frequent for the CCF strategy if it appears in at least 2 sequences
        //  splitMethod:0 --> 0 : indicate to not split the training sequences    1: indicate to split the sequences
        //  splitLength:4  --> indicate to split sequence to keep only 4 items, if splitting is activated
        //  minPredictionRatio:1.0  -->  the amount of sequences or part of sequences that should match to make a prediction, expressed as a ratio
        //  noiseRatio:1.0  -->   ratio of items to remove in a sequence per level (see paper).

        // Train the prediction model
        CPTPlusPredictor predictionModel = new CPTPlusPredictor("CPT+", optionalParameters);
        var lines = Helper.readFile(Helper.TEST_PATH);
        var sequenceAndResults = Helper.linesToSequences(lines);

        Helper.trainAndPredict(predictionModel, trainingSet.getSequences(),sequenceAndResults);

        // Now we will make a prediction.
        // We want to predict what would occur after the sequence <1, 2>.
        // We first create the sequence
//		Sequence sequence = new Sequence(0);
//		sequence.addItem(new Item(1));
//		sequence.addItem(new Item(2));

        // Then we perform the prediction
//		Sequence thePrediction = predictionModel.Predict(sequence);


//
//		// If we want to see why that prediction was made, we can also
//		// ask to see the count table of the prediction algorithm. The
//		// count table is a structure that stores the score for each symbols
//		// for the last prediction that was made.  The symbol with the highest
//		// score was the prediction.
//		System.out.println();
//		System.out.println("To make the prediction, the scores were calculated as follows:");
//		 Map<Integer, Float> countTable = predictionModel.getCountTable();
//		 for(Entry<Integer,Float> entry : countTable.entrySet()){
//			 System.out.println("symbol"  + entry.getKey() + "\t score: " + entry.getValue());
//		 }

//		System.out.println("For the sequence <(1),(2)>, the prediction for the next symbol is: +" + thePrediction);

//		// ======================== OPTIONAL ==============================================
//		// *******  IF we want to save the trained model to a file ******* ///
//		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("model.ser"));
//		stream.writeObject(predictionModel);
//		stream.close();
//		
//		// ****** Then, we can also load the trained model from the file ****** ///
//		ObjectInputStream stream2 = new ObjectInputStream(new FileInputStream("model.ser"));
//		Predictor predictionModel2 = (Predictor) stream2.readObject();
//		stream.close();
//		// and then make a prediction
//		Sequence thePrediction2 = predictionModel2.Predict(sequence);
//		System.out.println("For the sequence <(1),(4)>, the prediction for the next symbol is: +" + thePrediction2);

    }


    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = MainTestCPTPlus.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
    }
}
