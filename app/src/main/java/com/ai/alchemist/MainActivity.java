package com.ai.alchemist;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;

    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private AdView adView1;
    private AdView adView2;

    private static final int MAX_QUERY_HISTORY = 5; // Maximum number of past queries to send

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        MobileAds.initialize(this);

        adView1 = findViewById(R.id.adView1);
        adView2 = findViewById(R.id.adView2);

        // Load ads for adView1
        AdRequest adRequest1 = new AdRequest.Builder().build();
        adView1.loadAd(adRequest1);

        // Load ads for adView2
        AdRequest adRequest2 = new AdRequest.Builder().build();
        adView2.loadAd(adRequest2);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Update text size of menu items
        updateNavigationMenuTextSize();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawer(GravityCompat.START);
                int itemId = menuItem.getItemId();
                if (itemId == R.id.nav_chat) {
                    // Redirect to MainActivity (Chat)
                    return true;
                }
//                else if (itemId == R.id.nav_profiles) {
//                    // Redirect to ProfileActivity
//                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
//                    return true;
//                }
                else if (itemId == R.id.nav_images) {
                    // Redirect to ImagesActivity
                    startActivity(new Intent(MainActivity.this, ImagesActivity.class));
                    return true;
                }
                return false;
            }
        });

        messageList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        sendButton.setOnClickListener((v) -> {
            String question = messageEditText.getText().toString().trim();
            addToChat(question, Message.SENT_BY_ME);
            messageEditText.setText("");
            callAPI(question);
            welcomeTextView.setVisibility(View.GONE);
        });
    }

    void addToChat(String message, String sentBy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.smoothScrollToPosition(messageList.size() - 1);
            }
        });
    }

    void addResponse(String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!messageList.isEmpty()) {
                    messageList.remove(messageList.size() - 1);
                    messageAdapter.notifyItemRemoved(messageList.size());
                }
                messageList.add(new Message(response, Message.SENT_BY_BOT));
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.smoothScrollToPosition(messageList.size() - 1);
            }
        });
    }

    void callAPI(String question) {
        messageList.add(new Message("Typing... ", Message.SENT_BY_BOT));

        JSONArray messagesArray = new JSONArray();

        // Prepare the message history
        int startIndex = Math.max(0, messageList.size() - MAX_QUERY_HISTORY);
        for (int i = startIndex; i < messageList.size(); i++) {
            Message message = messageList.get(i);
            JSONObject messageObj = new JSONObject();
            try {
                messageObj.put("role", message.getSentBy().equals(Message.SENT_BY_ME) ? "user" : "assistant");
                messageObj.put("content", message.getContent());
                messagesArray.put(messageObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");
            jsonBody.put("messages", messagesArray);
//            jsonBody.put("temperature", 0);
            jsonBody.put("max_tokens", 1000);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer OpenAI API KEY")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("message");

                        // Extract the content from the JSON result
                        JSONObject resultObj = new JSONObject(result);
                        String content = resultObj.getString("content");

                        addResponse(content.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body().toString());
                }
            }
        });
    }

    private void updateNavigationMenuTextSize() {
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            SpannableString spannableString = new SpannableString(menuItem.getTitle());
            spannableString.setSpan(new AbsoluteSizeSpan(20, true), 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            menuItem.setTitle(spannableString);
        }
    }

}
