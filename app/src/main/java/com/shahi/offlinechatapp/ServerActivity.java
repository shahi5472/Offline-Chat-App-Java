package com.shahi.offlinechatapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;

public class ServerActivity extends AppCompatActivity {

    private WebSocketServer webSocketServer;
    private ArrayList<String> messages;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        messages = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        ListView listView = findViewById(R.id.messages);
        listView.setAdapter(adapter);

        EditText messageInput = findViewById(R.id.message);
        Button sendButton = findViewById(R.id.send_button);

        // Initialize WebSocket server
        webSocketServer = new WebSocketServer(new InetSocketAddress(3000)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                runOnUiThread(() -> {
                    messages.add("Client connected");
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                runOnUiThread(() -> {
                    messages.add("Client disconnected");
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                runOnUiThread(() -> {
                    messages.add("Client: " + message);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                runOnUiThread(() -> {
                    messages.add("Error: " + ex.getMessage());
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onStart() {
                runOnUiThread(() -> {
                    messages.add("Server started");
                    adapter.notifyDataSetChanged();
                });
            }
        };

        webSocketServer.start();

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                webSocketServer.broadcast(message);
                messageInput.setText("");
                runOnUiThread(() -> {
                    messages.add("Server: " + message);
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            webSocketServer.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}