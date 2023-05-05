package ca.pfv.spmf.Converter;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
//        Converter.splitNameData("C:\\Users\\1\\Desktop\\bible_with_items.txt", "C:\\Users\\1\\Desktop\\sequence_bible_with_items.txt", "C:\\Users\\1\\Desktop\\name_bible_with_items.txt");
//        var database = new NameDatabase("C:\\Users\\1\\Desktop\\name_bible_with_items.txt");
//        var scanner = new Scanner(System.in);
//        var readData = "";
//
//        do {
//            readData = scanner.nextLine();
//
//            if (readData.contains("1")) System.out.println(Arrays.toString(database.stringToIds(readData)));
//            if (readData.contains("2")) {
//                var id = readData.split(" ")[1];
//                System.out.println(database.idToName(Integer.parseInt(id)));
//            }
//        }
//        while (!readData.equals("exit"));

        var randomLines = Helper.getRandomLinesFromFile(50, 10, 50, 50,"C:\\Users\\1\\IdeaProjects\\SPMF\\src\\ca\\pfv\\spmf\\Converter\\Data\\TRACK_HISTORY.txt");
//        randomLines = Converter.convertSPMFToNormalFormat(randomLines);
//
        for (var line : randomLines) {
            System.out.println(line);
        }

        Helper.writeFile(randomLines, "C:\\Users\\1\\IdeaProjects\\SPMF\\src\\ca\\pfv\\spmf\\Converter\\Data\\TRACK_HISTORY_TEST.txt");
    }
}
