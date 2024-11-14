package com.example.colormixingquiz.Model.Data;

import android.content.Context;
import android.util.Log;
import java.io.*;

public class FileHelper {
    private static final String TAG = "FileHelper";
    private final Context context;
    private final String fileName;

    public FileHelper(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
    }

    public String readJsonFromAssets() throws IOException {
        try (InputStream is = context.getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            return jsonString.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error reading JSON from assets: " + e.getMessage());
            throw e;
        }
    }

    public String readJsonFromInternal() throws IOException {
        File file = new File(context.getFilesDir(), fileName);
        if (!file.exists()) {
            throw new IOException("File does not exist in internal storage");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return content.toString();
        }
    }

    public void writeJsonToInternal(String jsonContent) throws IOException {
        File file = new File(context.getFilesDir(), fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonContent);
            Log.d(TAG, "Successfully wrote JSON to internal storage");
        } catch (IOException e) {
            Log.e(TAG, "Error writing JSON to internal storage: " + e.getMessage());
            throw e;
        }
    }
}