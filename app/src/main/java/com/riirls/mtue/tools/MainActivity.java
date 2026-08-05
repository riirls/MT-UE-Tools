package com.riirls.mtue.tools;

import android.app.Activity;
import android.app.AlertDialog;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MTUE_MAIN";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();

        String message;
        if (data != null) {
            String path = data.getPath();
            message = handleFile(path);
        } else {
            // Try extras commonly used by MT Manager
            String pathExtra = intent.getStringExtra("path");
            if (pathExtra == null) {
                pathExtra = intent.getStringExtra("file");
            }
            if (pathExtra != null) {
                message = handleFile(pathExtra);
            } else {
                message = "No file path received from MT Manager.\nAction=" + action;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("MT-UE-Tools")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private String handleFile(String path) {
        if (path == null) return "Received null path";
        try {
            File f = new File(path);
            if (!f.exists() || !f.isFile()) {
                return "Path not a file: " + path;
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
            sb.append("Header (hex):\n");
            for (int i = 0; i < got; i++) {
                sb.append(String.format("%02X ", buf[i]));
                if ((i+1)%16==0) sb.append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "handleFile error", e);
            return "Error reading file: " + e.getMessage();
        }
    }
}
