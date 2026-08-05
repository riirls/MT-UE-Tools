package com.riirls.mtue.tools;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MTUE_MAIN";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        List<String> paths = extractPathsFromIntent(intent);

        String message;
        if (paths.size() > 0) {
            message = handlePaths(paths);
            showMessageDialog("MT-UE-Tools", message, true);
        } else {
            // No files: show simple info UI (user can open Keys via separate activity)
            showMessageDialog("MT-UE-Tools", "No file path received from MT Manager. Open plugin from file to analyze.\n\nYou can also manage AES keys in the plugin.\n", true);
        }
    }

    private List<String> extractPathsFromIntent(Intent intent) {
        List<String> paths = new ArrayList<>();

        if (intent == null) return paths;

        Uri data = intent.getData();
        if (data != null) {
            String p = data.getPath();
            if (p != null) paths.add(p);
        }

        // Common extras
        String pathExtra = intent.getStringExtra("path");
        if (pathExtra == null) pathExtra = intent.getStringExtra("file");
        if (pathExtra != null) paths.add(pathExtra);

        ArrayList<String> listExtra = intent.getStringArrayListExtra("paths");
        if (listExtra != null) paths.addAll(listExtra);

        // ClipData (multiple items)
        ClipData clip = intent.getClipData();
        if (clip != null) {
            for (int i = 0; i < clip.getItemCount(); i++) {
                ClipData.Item item = clip.getItemAt(i);
                Uri uri = item.getUri();
                if (uri != null) {
                    String p = uri.getPath();
                    if (p != null) paths.add(p);
                }
            }
        }

        return paths;
    }

    private String handlePaths(List<String> paths) {
        StringBuilder out = new StringBuilder();
        for (String p : paths) {
            out.append(handleFile(p));
            out.append("\n--------------------------------------------------\n");
        }
        return out.toString();
    }

    private String handleFile(String path) {
        if (path == null) return "Received null path";
        try {
            File f = new File(path);
            if (!f.exists() || !f.isFile()) {
                return "Path not a file: " + path + "\n";
            }
            long length = f.length();
            int readLen = (int)Math.min(64, length);
            byte[] buf = new byte[readLen];
            InputStream is = new FileInputStream(f);
            int got = is.read(buf);
            is.close();

            StringBuilder sb = new StringBuilder();
            sb.append("File: ").append(f.getName()).append('\n');
            sb.append("Path: ").append(path).append('\n');
            sb.append("Size: ").append(length).append(' ').append("bytes").append('\n');

            String detect = detectPak(buf, f.getName());
            if (detect != null) {
                sb.append("Detected: ").append(detect).append('\n');
            }

            sb.append("Header (hex):\n");
            for (int i = 0; i < got; i++) {
                sb.append(String.format("%02X ", buf[i]));
                if ((i+1)%16==0) sb.append('\n');
            }
            sb.append('\n');
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "handleFile error", e);
            return "Error reading file: " + e.getMessage() + "\n";
        }
    }

    private String detectPak(byte[] header, String filename) {
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".pak")) return "PAK (by extension)";
            if (lower.endsWith(".utoc") || lower.endsWith(".ucas")) return "IoStore (by extension)";
        }
        if (header != null && header.length > 3) {
            String ascii = new String(header);
            if (ascii.contains("PAK")) return "PAK (by header)";
            if (ascii.contains("UTOC") || ascii.contains("UCAS")) return "IoStore (by header)";
            // Some UE IoStore files may have 'TOC' or other magic; keep heuristics broad
        }
        return null;
    }

    private void showMessageDialog(String title, String message, boolean finishOnOk) {
        TextView tv = new TextView(this);
        int padding = (int)(12 * getResources().getDisplayMetrics().density);
        tv.setPadding(padding, padding, padding, padding);
        tv.setText(message);
        tv.setTextIsSelectable(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(tv)
                .setPositiveButton("OK", (dialog, which) -> {
                    if (finishOnOk) finish();
                })
                .setCancelable(true);

        // Add a Keys button to open KeysActivity, keeping UI consistent with MT Manager
        builder.setNeutralButton("Keys", (dialog, which) -> {
            try {
                Intent it = new Intent(this, KeysActivity.class);
                startActivity(it);
            } catch (Exception e) {
                Log.e(TAG, "Open KeysActivity failed", e);
            }
        });

        builder.show();
    }
}
