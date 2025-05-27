package com.example.llamachatbot1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.util.Log;

public class ChatActivity extends AppCompatActivity {

    private EditText etMessage;
    private Button btnSend;
    private LinearLayout chatLayout;
    private ScrollView scrollView;
    private String username;

    private final OkHttpClient client = new OkHttpClient();
    private final String BASE_URL = "http://10.0.2.2:5000/chat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        chatLayout = findViewById(R.id.chatLayout);
        scrollView = findViewById(R.id.scrollView);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        username = prefs.getString("username", "User");

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                addMessage(username + ": " + message);
                etMessage.setText("");
                getBotResponse(message);
            }
        });
    }

    private void addMessage(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setPadding(16, 12, 16, 12);
        chatLayout.addView(textView);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void getBotResponse(String userMessage) {
        JSONObject json = new JSONObject();
        try {
            json.put("message", userMessage);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ChatActivity", "Network request failed: " + e.getMessage());
                runOnUiThread(() -> {
                    addMessage("LlamaBot: [Error connecting to server]");
                    Toast.makeText(ChatActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("ChatActivity", "Server response: " + responseBody);
                    try {
                        JSONObject responseJson = new JSONObject(responseBody);
                        String botReply = responseJson.getString("response");
                        runOnUiThread(() -> addMessage("LlamaBot: " + botReply));
                    } catch (JSONException e) {
                        Log.e("ChatActivity", "JSON parsing error: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ChatActivity", "Server returned error code: " + response.code());
                    runOnUiThread(() -> addMessage("LlamaBot: [Unexpected server response]"));
                }
            }
        });
    }
}

