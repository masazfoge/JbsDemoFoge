package com.dynademo.csvdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CsvTableStore {

    private static final String CONFIG_FILE = "C:\\workspace-eclipse\\JbsFoge\\JbsDemoWar\\dbTable.csv";
    private static final String HEADER = "key,value";

    private final Gson gson;
    private final Path csvPath;

    public CsvTableStore() {
        this.gson = new GsonBuilder().create();
        this.csvPath = loadCsvPathFromConfig();
        initializeIfNeeded();
    }

    /**
     * key,value を新規追加する。
     * 同じ key が既に存在する場合は例外。
     */
    public void insert(String key, Object value) {
        validateKey(key);

        Map<String, String> data = readAllAsRawMap();
        if (data.containsKey(key)) {
            throw new IllegalArgumentException("Key already exists: " + key);
        }

        data.put(key, gson.toJson(value));
        writeAll(data);
    }

    /**
     * key,value を更新する。
     * key が存在しない場合は例外。
     */
    public void update(String key, Object value) {
        validateKey(key);

        Map<String, String> data = readAllAsRawMap();
        if (!data.containsKey(key)) {
            throw new IllegalArgumentException("Key not found: " + key);
        }

        data.put(key, gson.toJson(value));
        writeAll(data);
    }

    /**
     * key に対応する value を指定型で返す。
     * 見つからない場合は null。
     */
    public <T> T select(String key, Class<T> clazz) {
        validateKey(key);
        Objects.requireNonNull(clazz, "clazz must not be null");

        String json = readAllAsRawMap().get(key);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, clazz);
    }

    /**
     * List<Foo> などの総称型用。
     * 例: new TypeToken<List<Foo>>() {}.getType()
     */
    public <T> T select(String key, Type type) {
        validateKey(key);
        Objects.requireNonNull(type, "type must not be null");

        String json = readAllAsRawMap().get(key);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, type);
    }

    /**
     * key を削除する。
     * 存在しない場合は何もしない。
     */
    public void delete(String key) {
        validateKey(key);

        Map<String, String> data = readAllAsRawMap();
        if (data.remove(key) != null) {
            writeAll(data);
        }
    }

    private Path loadCsvPathFromConfig() {
        return Path.of(CONFIG_FILE);

//        if (!Files.exists(configPath)) {
//            throw new IllegalStateException(CONFIG_FILE + " not found in project root.");
//        }
//
//        try (BufferedReader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
//            DbConfig config = gson.fromJson(reader, DbConfig.class);
//            if (config == null || config.csvFile == null || config.csvFile.isBlank()) {
//                throw new IllegalStateException("csvFile is missing in " + CONFIG_FILE);
//            }
//            return Path.of(config.csvFile);
//        } catch (IOException e) {
//            throw new UncheckedIOException("Failed to read " + CONFIG_FILE, e);
//        }
    }

    private void initializeIfNeeded() {
        try {
            if (!Files.exists(csvPath)) {
                try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8)) {
                    writer.write(HEADER);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to initialize CSV file: " + csvPath, e);
        }
    }

    private Map<String, String> readAllAsRawMap() {
        Map<String, String> map = new LinkedHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // ヘッダ読み飛ばし
                }
                if (line.isBlank()) {
                    continue;
                }

                String[] cols = parseCsvLine(line);
                if (cols.length < 2) {
                    continue;
                }

                String key = cols[0];
                String value = cols[1];
                map.put(key, value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read CSV file: " + csvPath, e);
        }

        return map;
    }

    private void writeAll(Map<String, String> data) {
        try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();

            for (Map.Entry<String, String> entry : data.entrySet()) {
                writer.write(toCsvField(entry.getKey()));
                writer.write(",");
                writer.write(toCsvField(entry.getValue()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write CSV file: " + csvPath, e);
        }
    }

    /**
     * 最小限のCSVパーサ。
     * 今回は key,value の2列前提。
     */
    private String[] parseCsvLine(String line) {
        StringBuilder current = new StringBuilder();
        String[] result = new String[2];
        int colIndex = 0;
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                if (colIndex < 2) {
                    result[colIndex++] = current.toString();
                }
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        if (colIndex < 2) {
            result[colIndex] = current.toString();
        }

        return new String[]{
                result[0] == null ? "" : result[0],
                result[1] == null ? "" : result[1]
        };
    }

    private String toCsvField(String value) {
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
    }

    private static class DbConfig {
        String csvFile;
    }
}
