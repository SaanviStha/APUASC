package storage;

import model.Storable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic text-file storage. Every subclass only has to say how one
 * line is turned back into an object (parseLine).
 */
public abstract class FileManager<T extends Storable> {

    private final String filePath;

    protected FileManager(String filePath) {
        this.filePath = filePath;
        createFileIfMissing();
    }

    // Turns one line of the text file into an object
    protected abstract T parseLine(String line);

    // =========================
    // READ
    // =========================

    // Reads every line of the file and turns each line into an object
    public List<T> readAll() {
        List<T> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    T item = parseLine(line);
                    if (item != null) {
                        items.add(item);
                    }
                } catch (RuntimeException e) {
                    // A damaged line should not stop the whole system
                    System.out.println("Skipped invalid line in " + filePath + ": " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + filePath);
        }
        return items;
    }

    // Returns the record with this ID, or null if there is none
    public T findById(String id) {
        if (id == null) {
            return null;
        }
        for (T item : readAll()) {
            if (item.getId().equalsIgnoreCase(id)) {
                return item;
            }
        }
        return null;
    }

    // =========================
    // WRITE
    // =========================

    // Writes all records to the file, replacing the old contents
    public void saveAll(List<T> items) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (T item : items) {
                writer.write(item.toFileLine());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing file: " + filePath);
        }
    }

    // Adds one record to the end of the file
    public void add(T item) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(item.toFileLine());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error writing file: " + filePath);
        }
    }

    // Replaces the record that has the same ID
    public void update(T item) {
        List<T> items = readAll();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                saveAll(items);
                return;
            }
        }
        add(item);
    }

    // Removes the record with this ID
    public void delete(String id) {
        List<T> items = readAll();
        List<T> kept = new ArrayList<>();
        for (T item : items) {
            if (!item.getId().equals(id)) {   // keep every record except the one being deleted
                kept.add(item);
            }
        }
        saveAll(kept);
    }

    // =========================
    // HELPERS
    // =========================

    // Next ID with the given prefix, e.g. A001, A002 ...
    public String nextId(String prefix) {
        int max = 0;
        for (T item : readAll()) {
            String id = item.getId();
            if (id.startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(prefix.length())));
                } catch (NumberFormatException e) {
                    // ID with a different prefix that starts the same way (e.g. C vs CM)
                }
            }
        }
        return String.format("%s%03d", prefix, max + 1);
    }

    // Splits a line into its fields at each | character
    protected String[] split(String line) {
        return line.split("\\|", -1);
    }

    // Creates the folder and the empty file the first time the program runs
    private void createFileIfMissing() {
        try {
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Unable to create file: " + filePath);
        }
    }

    public String getFilePath() {
        return filePath;
    }
}
