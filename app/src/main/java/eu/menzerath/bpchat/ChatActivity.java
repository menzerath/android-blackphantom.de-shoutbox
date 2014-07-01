package eu.menzerath.bpchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import eu.menzerath.bpchat.chat.ChatMessage;
import eu.menzerath.bpchat.chat.MessageArrayAdapter;

/**
 * Die Haupt-Activity für den gesamten Chat mit der Anzeige der Nachrichten und dem Eingabefeld
 */
public class ChatActivity extends Activity {
    private MessageArrayAdapter mAdapter;
    private EditText mInput;
    private Button mButton;
    private User mUser;

    private SharedPreferences prefs;
    private ScheduledExecutorService scheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        initGui();
        doLogin();
        loadMessages();
    }

    private void initGui() {
        requestWindowFeature(Window.FEATURE_PROGRESS); // Unterstützung für die ProgressBar über der ActionBar

        setContentView(R.layout.activity_chat);

        setProgressBarIndeterminate(true);

        // ListView mit den Nachrichten
        ListView mListView = (ListView) findViewById(R.id.listView1);
        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        mListView.setStackFromBottom(true);
        mAdapter = new MessageArrayAdapter(this, R.layout.listitem_message);
        mListView.setAdapter(mAdapter);

        // Das Eingabefeld
        mInput = (EditText) findViewById(R.id.editText1);
        mInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // "Fertig" / "Enter" ==> Nachricht senden
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
        mInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Berührung entfernt Fehler-Nachricht
                mInput.setError(null);
                return false;
            }
        });

        // Der "Senden"-Button
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    /**
     * Meldet den Nutzer beim Server an
     */
    private void doLogin() {
        String username = prefs.getString("username", "");
        String password = prefs.getString("password", "");

        mUser = new User(username, password, prefs);

        UserLoginTask mUserLoginTask = new UserLoginTask(mUser);
        mUserLoginTask.execute((Void) null);
    }

    /**
     * Asynchone Aufgabe, damit das UI nicht "hängenbleibt"
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final User mUser;

        UserLoginTask(User user) {
            mUser = user;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mUser.login();
        }

        @Override
        protected void onPreExecute() {
            getActionBar().setSubtitle(getString(R.string.login_running));
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                getActionBar().setSubtitle(getString(R.string.loggedin_as) + " " + mUser.username);
                mInput.setEnabled(true);
                mButton.setEnabled(true);
            } else {
                mAdapter.add(new ChatMessage(0, getString(R.string.app_message_user), new Date(), getString(R.string.login_no_success) + mUser.getLastError(), true));
                getActionBar().setSubtitle(getString(R.string.login_no));
                mInput.setEnabled(false);
                mButton.setEnabled(false);
            }
        }
    }

    /**
     * Lädt die letzten Nachrichten vom Server, insofern nicht gerade ein zweiter Task diese Aufgabe übernommen hat
     */
    private void loadMessages() {
        if (mUser.isLoadingMessages()) return;
        UserLoadMessagesTask mUserLoadMessagesTask = new UserLoadMessagesTask(mUser);
        mUserLoadMessagesTask.execute((Void) null);
    }

    /**
     * Asynchone Aufgabe, damit das UI nicht "hängenbleibt"
     */
    public class UserLoadMessagesTask extends AsyncTask<Void, Void, Boolean> {
        private final User mUser;
        private ArrayList<ChatMessage> messages;

        UserLoadMessagesTask(User user) {
            mUser = user;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            messages = mUser.getMessages();
            return true;
        }

        @Override
        protected void onPreExecute() {
            setProgressBarVisibility(true);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (messages != null) {
                try {
                    // Spiele einen Sound ab, falls die ID der letzten angezeigten Nachricht nicht der ID der letzten heruntergeladenen Nachricht entspricht
                    if ((messages.get(messages.size() - 1).getId() != mAdapter.getItem(mAdapter.getCount() - 1).getId()) && prefs.getBoolean("messageSound", true)) {
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.message_sound);
                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mp.release();
                            }
                        });
                        mp.start();
                    }
                } catch (IndexOutOfBoundsException ignored) { // Falls noch keine Nachrichten angezeigt werden, ist die Liste im Adapter noch leer
                }

                // Leere die angezeigten Nachrichten
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();

                // Füge die neuen Nachrichten hinzu
                for (ChatMessage m : messages) {
                    mAdapter.add(m);
                }
            } else {
                // Fehler-Nachricht, falls keine Nachrichten abgerufen werden konnten
                mAdapter.add(new ChatMessage(0, getString(R.string.app_message_user), new Date(), getString(R.string.get_messages_no_success) + mUser.getLastError(), true));
            }
            setProgressBarVisibility(false);
        }
    }

    /**
     * Sendet eine Nachricht an den Server, falls die Anforderungen an diese erfüllt werden
     */
    private void sendMessage() {
        if (!mUser.isLoggedIn() || mUser.isSendingMessage()) return;

        String message = mInput.getText().toString().trim();
        if (message.isEmpty()) {
            mInput.setError(getString(R.string.error_message_empty));
            mInput.requestFocus();
        } else if (message.length() < 2 || message.length() > 2000) {
            mInput.setError(getString(R.string.error_message_length));
            mInput.requestFocus();
        } else {
            UserSendMessageTask mUserSendMessageTask = new UserSendMessageTask(mUser, message);
            mUserSendMessageTask.execute((Void) null);
        }
    }

    /**
     * Asynchone Aufgabe, damit das UI nicht "hängenbleibt"
     */
    public class UserSendMessageTask extends AsyncTask<Void, Void, Boolean> {
        private final User mUser;
        private final String mMessage;
        private int newMessageId;

        UserSendMessageTask(User user, String message) {
            mUser = user;
            mMessage = message;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            newMessageId = mUser.sendMessage(mMessage);
            return newMessageId != 0;
        }

        @Override
        protected void onPreExecute() {
            mInput.setEnabled(false);
            mButton.setEnabled(false);
            setProgressBarVisibility(true);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // Eigene Nachricht anzeigen
                mAdapter.add(new ChatMessage(newMessageId, mUser.username, new Date(), mMessage, false));
                mInput.setText("");
            } else {
                // Fehler-Nachricht anzeigen
                mAdapter.add(new ChatMessage(0, getString(R.string.app_message_user), new Date(), getString(R.string.send_message_no_success) + mUser.getLastError(), true));
            }
            mInput.setEnabled(true);
            mButton.setEnabled(true);
            setProgressBarVisibility(false);
        }
    }

    /**
     * Lädt die gerade eingeloggten User vom Server, insofern nicht gerade ein zweiter Task diese Aufgabe übernommen hat
     */
    private void showOnlineUsers() {
        if (mUser.isLoadingUsers()) return;
        UserLoadUsersTask mUserLoadUsersTask = new UserLoadUsersTask(mUser);
        mUserLoadUsersTask.execute((Void) null);
    }

    /**
     * Asynchone Aufgabe, damit das UI nicht "hängenbleibt"
     */
    public class UserLoadUsersTask extends AsyncTask<Void, Void, Boolean> {
        private final User mUser;
        private ProgressDialog mProgressDialog;
        private String users;

        UserLoadUsersTask(User user) {
            mUser = user;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            users = mUser.getOnlineUsers();
            return true;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(ChatActivity.this);
            mProgressDialog.setTitle(getString(R.string.progressDialog_title));
            mProgressDialog.setMessage(getString(R.string.progressDialog_message));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mProgressDialog.cancel();

            if (users != null) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ChatActivity.this);
                alertDialogBuilder.setTitle(getString(R.string.onlineUsers_title));
                alertDialogBuilder
                        .setMessage(users.replace("\",\"", ", ").replace("[\"", "").replace("\"]", ""))
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else {
                // Fehler-Nachricht, falls keine User abgerufen werden konnten
                Toast.makeText(ChatActivity.this, getString(R.string.onlineUsers_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        // Falls in den Einstellungen aktiviert, starte eine regelmäßige Aufgabe, die die Nachrichten abruft
        if (prefs.getBoolean("autoReload", true)) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            loadMessages();
                        }
                    });
                }
            }, 0, Integer.parseInt(prefs.getString("autoReloadInterval", "10")), TimeUnit.SECONDS);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Nachrichten nicht mehr automatisch abrufen
        scheduler.shutdown();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reload) {
            loadMessages();
        } else if (id == R.id.action_users) {
            showOnlineUsers();
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.action_relogin) {
            doLogin();
        }
        return super.onOptionsItemSelected(item);
    }

    public User getUser() {
        return mUser;
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }
}