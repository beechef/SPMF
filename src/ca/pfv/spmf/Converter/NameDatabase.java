package ca.pfv.spmf.Converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;

public class NameDatabase {
    private final Hashtable<Integer, String> database;
    private final Hashtable<String, Integer> invertedDatabase;

    public NameDatabase(String nameDatabasePath) throws FileNotFoundException {
        var file = new File(nameDatabasePath);
        var nameDatabase = Helper.readFile(file);
        this.database = convertData(nameDatabase);
        this.invertedDatabase = reverseHashTable(database);
    }

    private Hashtable<Integer, String> convertData(String nameDatabase) {
        var database = new Hashtable<Integer, String>();
        var splitNames = nameDatabase.split("\n");

        for (var splitName : splitNames) {
            var keyPair = splitName.split("=");
            if (keyPair.length != 2) continue;

            var id = Integer.parseInt(keyPair[0]);
            var name = keyPair[1];

            if (!database.containsKey(id)) database.put(id, name);
        }

        return database;
    }

    private <K, V> Hashtable<V, K> reverseHashTable(Hashtable<K, V> data) {
        var revertedData = new Hashtable<V, K>();

        data.forEach((k, v) -> {
            if (!revertedData.containsKey(v)) revertedData.put(v, k);
        });

        return revertedData;
    }

    public String idToName(int id) {
        if (database.containsKey(id)) return database.get(id);
        return "";
    }

    public int[] stringToIds(String data) {
        var splitData = data.split(" ");
        var ids = new int[splitData.length];

        for (int i = 0; i < splitData.length; i++) {
            String word = splitData[i];

            ids[i] = invertedDatabase.getOrDefault(word, -99);
        }

        return ids;
    }
}
