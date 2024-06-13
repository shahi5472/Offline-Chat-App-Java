package com.shahi.offlinechatapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ClientActivity extends AppCompatActivity {
    private WebSocketClient webSocketClient;
    private ArrayList<String> messages;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        messages = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        ListView listView = findViewById(R.id.messages);
        listView.setAdapter(adapter);

        EditText messageInput = findViewById(R.id.message);
        Button sendButton = findViewById(R.id.send_button);

        try {
            webSocketClient = new WebSocketClient(new URI("ws://192.168.0.102:3000")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    runOnUiThread(() -> {
                        messages.add("Connected");
                        adapter.notifyDataSetChanged();
                    });
                }

                @Override
                public void onMessage(String message) {
                    runOnUiThread(() -> {
                        messages.add("Server: " + message);
                        adapter.notifyDataSetChanged();
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    runOnUiThread(() -> {
                        messages.add("Disconnected");
                        adapter.notifyDataSetChanged();
                    });
                }

                @Override
                public void onError(Exception ex) {
                    runOnUiThread(() -> {
                        messages.add("Error: " + ex.getMessage());
                        adapter.notifyDataSetChanged();
                    });
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                webSocketClient.send(message);
                messageInput.setText("");
                runOnUiThread(() -> {
                    messages.add("Client: " + message);
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocketClient.close();
    }
}