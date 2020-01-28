package com.example.fbrealbase;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.fbrealbase.api.ChatterBot;
import com.example.fbrealbase.api.ChatterBotFactory;
import com.example.fbrealbase.api.ChatterBotSession;
import com.example.fbrealbase.api.ChatterBotType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "xyz";

    //Para obtener fecha y hora
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat simpleDateFormatFecha = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat simpleDateFormatHora = new SimpleDateFormat("HH:mm:ss");

    FirebaseDatabase database = FirebaseDatabase.getInstance(); //Singleton
    ChatSentence chatSentence;

    String botSentenceEn, botSentenceEs, userSentenceEn, userSentenceEs, talker,
            time = simpleDateFormatHora.format(calendar.getTime()),
            fecha = simpleDateFormatFecha.format(calendar.getTime()) + "/";

    private ChatterBot bot;
    private ChatterBotSession botSession;
    private ChatterBotFactory factory;

    Intent intent = new Intent();
    TTS tts;
    ArrayList<String> result = null;
    String auxiliar;
    TextView tvText, tvBot;
    Button btSend;
    FloatingActionButton btRecord;
    EditText etInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initBot();
        initComponentes();
        initEvents();
    }

    private void initBot() {
        tts = new TTS(getApplicationContext());
        if(startBot()) {

        }
    }

    private void initComponentes() {
        btRecord = findViewById(R.id.btRecord);
        btSend = findViewById(R.id.btSend);
        etInput = findViewById(R.id.etInput);
        tvText = findViewById(R.id.tvText);
        tvBot = findViewById(R.id.tvBot);
    }

    private void initEvents() {
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etInput.getText().toString();
                etInput.setText("");
                tvText.setText(text);
                userSentenceEs = text;
                //FB Yo - Español

                //new Chat().execute(text);
                new Translate().execute("es", text, "en");
            }
        });

        btRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),"Say Something...",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                if(intent.resolveActivity(getPackageManager())!=null) {
                    startActivityForResult(intent, 5);
                }
                else {
                    Toast.makeText(view.getContext(),"Your Device Doesn't Support Speech Intent", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==5) {
            if(resultCode==RESULT_OK && data!=null) {
                result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                tvText.setText(result.get(0));
                new Translate().execute("es", result.get(0), "en");
            }
        }
    }

    private boolean startBot() {
        boolean result = true;
        String initialMessage;
        factory = new ChatterBotFactory();
        try {
            bot = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
            botSession = bot.createSession();
            //initialMessage = "conectado" + "\n";
        } catch(Exception e) {
            //initialMessage = "ERROR" + "\n" + " " + e.toString();
            result = false;
        }
        return result;
    }

    public void saveInDatabase(String sentenceEn, String sentenceEs, String talker, String time){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference referenciaItem = database.getReference("user/" + uid); //Se referencia el nodo chatSentence de Firebase
        referenciaItem.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.v(TAG, "data changed: " + dataSnapshot.toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.v(TAG, "error: " + databaseError.toException());
            }
        });

        //referenciaItem.setValue("valor chatSentence"); //Si no existe chatSentence, la crea, y si existe actualiza su valor
        //referenciaItem.child("uno").setValue("hola");
        //String key = referenciaItem.push().getKey();
        //referenciaItem.child(key).setValue("hola 1");
        //key = referenciaItem.push().getKey();
        //referenciaItem.child(key).setValue("hola 2");

        chatSentence = new ChatSentence(sentenceEn, sentenceEs, talker, time);

        Map<String, Object> map = new HashMap<>();
        String key = referenciaItem.push().getKey();
        map.put(fecha + key, chatSentence.toMap());
        referenciaItem.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.v(TAG, "task succesfull");
                } else {
                    Log.v(TAG, task.getException().toString());
                }
            }
        });

    }

    private String chat(final String text) {
        String response;
        try {
            response = botSession.think(text);
        } catch (final Exception e) {
            response = "";
        }

        Log.v("chat", response);
        botSentenceEn = response;
        //FB Bot - Ingles

        //traducir("en", response, "es");
        return response;
    }

    /*private class Chat extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... s) {
            String[] r = {s[0], chat(s[0])};
            return r;
        }

        @Override
        protected void onPostExecute(String[] response) {
            super.onPostExecute(response);
            auxiliar = response[1];
            tvText.append("you> " + response[0] + "\n");
            tvText.append("bot> " + auxiliar + "\n");
            tts.sayHello(response[1]);
        }
    }*/

    class Translate extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            RestClient r = new RestClient();

            HashMap<String, String> httpBodyParams;
            httpBodyParams = new HashMap<>();
            httpBodyParams.put("fromLang", strings[0]);
            httpBodyParams.put("text", strings[1]);
            httpBodyParams.put("to", strings[2]);


            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : httpBodyParams.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");
                try {
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                result.append("=");
                try {
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            String parameters = result.toString();



            String e = r.postHttp("https://www.bing.com/ttranslatev3?isVertical=1&&IG=9AB86C10F77B448D932E5D5DB4E982F1&IID=translator.5026.3", parameters);
            Log.v("parametros", "Respuesta: " + parameters);
            String sub = e.substring(e.indexOf("\"text\":\""), e.indexOf("\"to\":\""));
            auxiliar = sub.substring(8).replace("\"", "").replace(",", "");
            Log.v("traduccionIngles", auxiliar);
            traducir2("en", chat(auxiliar), "es");
            userSentenceEn = auxiliar;
            talker = "user";
            saveInDatabase(userSentenceEn, userSentenceEs, talker, time);
            //FB Yo - Ingles

            return null;
        }

    }

    public void traducir2(String... params){
        new Translate2().execute(params);
    }

    class Translate2 extends AsyncTask<String, String, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            RestClient r = new RestClient();

            HashMap<String, String> httpBodyParams;
            httpBodyParams = new HashMap<>();
            httpBodyParams.put("fromLang", strings[0]);
            httpBodyParams.put("text", strings[1]);
            httpBodyParams.put("to", strings[2]);


            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : httpBodyParams.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");
                try {
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                result.append("=");
                try {
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            String parameters = result.toString();


            String e = r.postHttp("https://www.bing.com/ttranslatev3?isVertical=1&&IG=9AB86C10F77B448D932E5D5DB4E982F1&IID=translator.5026.3", parameters);
            String sub = e.substring(e.indexOf("\"text\":\""), e.indexOf("\"to\":\""));
            auxiliar = sub.substring(8).replace("\"", "").replace(",", "");
            Log.v("auxiliar2", auxiliar);

            botSentenceEs = auxiliar;
            talker = "bot";
            saveInDatabase(botSentenceEn, botSentenceEs, talker, time);
            //FB Bot - Español

            publishProgress();
            tts.sayHello(auxiliar);

            return null;


        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            tvBot.setText(auxiliar);
        }
    }

    /*public class SetTime {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH:mm:ss");


        public  void main(String[] args) {
            Calendar calendar = Calendar.getInstance();

            /*calendar.set(Calendar.HOUR, 17);
            calendar.set(Calendar.MINUTE, 30);
            calendar.set(Calendar.SECOND, 2);
            System.out.println(simpleDateFormat.format(calendar.getTime()));
            System.out.println(simpleDateFormat2.format(calendar.getTime()));

        }

    }*/

    private void logoutUser() {
        FirebaseAuth firebaseAuth;
        FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null){
                    //Do anything here which needs to be done after signout is complete
                    //firebaseAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                    Toast.makeText(MainActivity.this, "Sign out successfull", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "No sa podio hace el signout", Toast.LENGTH_SHORT).show();
                }
            }
        };

        //Init and attach
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(authStateListener);

        //Call signOut()
        firebaseAuth.signOut();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logoutUser();
                return true;
            case R.id.showRecord:
                intent = new Intent(MainActivity.this, ShowRecordActivity.class);
                intent.putExtra("chatSentence", chatSentence);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
