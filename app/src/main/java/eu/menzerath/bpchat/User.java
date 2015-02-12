package eu.menzerath.bpchat;

import android.content.SharedPreferences;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import eu.menzerath.bpchat.chat.ChatMessage;
import eu.menzerath.bpchat.chat.Helper;
import eu.menzerath.bpchat.chat.TlsSniSocketFactory;

/**
 * Ein Nutzer, der mit der API interagieren kann
 * TODO: Klasse aufräumen!
 */
public class User {
    private static final String LOGIN_URL = "https://chat.blackphantom.de/api/authentication.php";
    private static final String GET_MESSAGES_URL = "https://chat.blackphantom.de/api/loadLastMessages.php";
    private static final String SEND_MESSAGE_URL = "https://chat.blackphantom.de/api/sendMessage.php";
    private static final String GET_USERS_URL = "https://chat.blackphantom.de/api/onlineusers.php";
    public final String username;
    public final String password;
    private final SharedPreferences prefs;
    private HttpContext localContext;

    private String lastError;
    private boolean isLoggedIn;
    private boolean isLoadingMessages;
    private boolean isSendingMessage;
    private boolean isLoadingUsers;

    /**
     * Konstruktor des Nutzers
     *
     * @param username Nutzername des Users
     * @param password Passwort des Users
     * @param prefs    Kontext der Activity, damit auf die Einstellungen zugegriffen werden kann
     */
    public User(String username, String password, SharedPreferences prefs) {
        this.username = username;
        this.password = password;
        this.prefs = prefs;

        // Cookies müssen bei jedem Request erhalten beleiben, damit der User eingeloggt bleibt
        CookieStore cookieStore = new BasicCookieStore();
        localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }

    /**
     * Meldet den User beim Server an
     *
     * @return Ob der Login-Versuch erfolgreich war
     */
    public boolean login() {
        HttpClient httpclient = new DefaultHttpClient();
        SchemeRegistry schemeRegistry = httpclient.getConnectionManager().getSchemeRegistry();
        schemeRegistry.register(new Scheme("https", new TlsSniSocketFactory(), 443));

        HttpPost httppost = new HttpPost(LOGIN_URL);
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);
            nameValuePairs.add(new BasicNameValuePair("username", this.username));
            nameValuePairs.add(new BasicNameValuePair("password", this.password));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost, localContext);

            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, "UTF-8");

            JSONObject json = new JSONObject(jsonResponse);

            if (json.getBoolean("success")) {
                isLoggedIn = true;
                return true;
            } else {
                JSONArray errors = json.getJSONArray("errors");
                lastError = "";

                for (int i = 0; i < errors.length(); i++) {
                    lastError += errors.getString(i);
                }
            }
        } catch (Exception e) {
            lastError = e.getMessage();
        }
        isLoggedIn = false;
        return false;
    }

    /**
     * Ruft die letzten Nachrichten vom Server ab
     *
     * @return Die X letzten Nachrichten
     */
    public ArrayList<ChatMessage> getMessages() {
        isLoadingMessages = true;

        HttpClient httpclient = new DefaultHttpClient();
        SchemeRegistry schemeRegistry = httpclient.getConnectionManager().getSchemeRegistry();
        schemeRegistry.register(new Scheme("https", new TlsSniSocketFactory(), 443));

        HttpPost httppost = new HttpPost(GET_MESSAGES_URL);
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>(1);
            nameValuePairs.add(new BasicNameValuePair("limit", prefs.getString("maxMessages", "100")));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost, localContext);

            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, "UTF-8");

            JSONObject json = new JSONObject(jsonResponse);

            if (json.getBoolean("success")) {
                ArrayList<ChatMessage> messages = new ArrayList<>();

                JSONObject payloadData = json.getJSONObject("payload");
                Iterator<?> keys = payloadData.keys();

                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    JSONArray messageData = payloadData.getJSONArray(key);

                    messages.add(new ChatMessage(Integer.parseInt(key), messageData.getString(0), Helper.serverTimestampToDate(messageData.getString(1)), messageData.getString(2), messageData.getString(0).equalsIgnoreCase(this.username)));
                }
                Collections.sort(messages);
                isLoadingMessages = false;
                return messages;
            } else {
                JSONArray errors = json.getJSONArray("errors");
                lastError = "";

                for (int i = 0; i < errors.length(); i++) {
                    lastError += errors.getString(i);
                }
            }
        } catch (Exception e) {
            lastError = e.getMessage();
        }
        isLoadingMessages = false;
        return null;
    }

    /**
     * Sendet eine Nachricht an den Server
     *
     * @param message Nachricht
     * @return Ob der Sendevorgang erfolgreich war
     */
    public int sendMessage(String message) {
        isSendingMessage = true;

        HttpClient httpclient = new DefaultHttpClient();
        SchemeRegistry schemeRegistry = httpclient.getConnectionManager().getSchemeRegistry();
        schemeRegistry.register(new Scheme("https", new TlsSniSocketFactory(), 443));

        HttpPost httppost = new HttpPost(SEND_MESSAGE_URL);
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>(1);
            nameValuePairs.add(new BasicNameValuePair("body", message.trim()));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost, localContext);

            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, "UTF-8");

            JSONObject json = new JSONObject(jsonResponse);

            if (json.getBoolean("success")) {
                isSendingMessage = false;
                return Integer.parseInt(json.getString("payload"));
            } else {
                JSONArray errors = json.getJSONArray("errors");
                lastError = "";

                for (int i = 0; i < errors.length(); i++) {
                    lastError += errors.getString(i);
                }
            }
        } catch (Exception e) {
            lastError = e.getMessage();
        }
        isSendingMessage = false;
        return 0;
    }

    /**
     * Gibt die gerade eingeloggten User an
     *
     * @return Aktuelle eingeloggte User
     */
    public String getOnlineUsers() {
        isLoadingUsers = true;

        HttpClient httpclient = new DefaultHttpClient();
        SchemeRegistry schemeRegistry = httpclient.getConnectionManager().getSchemeRegistry();
        schemeRegistry.register(new Scheme("https", new TlsSniSocketFactory(), 443));

        HttpPost httppost = new HttpPost(GET_USERS_URL);
        try {
            HttpResponse response = httpclient.execute(httppost, localContext);

            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, "UTF-8");

            JSONObject json = new JSONObject(jsonResponse);

            if (json.getBoolean("success")) {
                JSONArray payloadData = json.getJSONArray("payload");
                isLoadingUsers = false;
                return payloadData.toString();
            } else {
                JSONArray errors = json.getJSONArray("errors");
                lastError = "";

                for (int i = 0; i < errors.length(); i++) {
                    lastError += errors.getString(i);
                }
            }
        } catch (Exception e) {
            lastError = e.getMessage();
        }
        isLoadingUsers = false;
        return null;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public boolean isLoadingMessages() {
        return isLoadingMessages;
    }

    public boolean isSendingMessage() {
        return isSendingMessage;
    }

    public boolean isLoadingUsers() {
        return isLoadingUsers;
    }

    public String getLastError() {
        return lastError;
    }
}