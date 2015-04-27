package eu.menzerath.bpchat.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import eu.menzerath.bpchat.ChatActivity;
import eu.menzerath.bpchat.R;

/**
 * Der Adapter für das ListView, welches die Nachrichten anzeigt
 */
public class MessageArrayAdapter extends ArrayAdapter<ChatMessage> {
    private ChatActivity chatActivity;
    private List<ChatMessage> messages = new ArrayList<>(); // Liste mit den Chat-Nachrichten

    public MessageArrayAdapter(ChatActivity chatActivity, int textViewResourceId) {
        super(chatActivity, textViewResourceId);
        this.chatActivity = chatActivity;
    }

    @Override
    public void add(ChatMessage object) {
        // Nachricht wird zunächst in die Liste eingefügt, damit sie angezeigt werden kann
        messages.add(object);
        super.add(object);
    }

    @Override
    public void clear() {
        // Methode muss überschrieben werden, damit die Liste zunächst geleert wird
        messages.clear();
        super.clear();
    }

    public View getView(int position, View row, ViewGroup parent) {
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.listitem_message, parent, false);
        }

        LinearLayout wrapper = (LinearLayout) row.findViewById(R.id.wrapper);
        LinearLayout messageBubble = (LinearLayout) row.findViewById(R.id.bubble);

        ChatMessage message = getItem(position);
        TextView tvFrom = (TextView) row.findViewById(R.id.from);
        TextView tvTime = (TextView) row.findViewById(R.id.time);
        TextView tvMessage = (TextView) row.findViewById(R.id.message);

        // Setzt den Absender + Zeitpunkt / Nachricht
        tvFrom.setText(message.getFrom());
        tvTime.setText(Helper.formatDateToString(message.getTime()));
        tvMessage.setText(prepareMessage(message.getMessage()));

        if (message.isOwnMessage())
            tvFrom.setText(""); // "tvFrom.setVisibility(View.GONE);" funktioniert nicht korrekt!

        // Anpassung der Blasen für besseren Look
        messageBubble.setBackgroundResource(message.isOwnMessage() ? R.drawable.bubble_right : R.drawable.bubble_left);
        if (message.getMessage().contains("@" + chatActivity.getPrefs().getString("username", ""))) {
            messageBubble.setBackgroundResource(message.isOwnMessage() ? R.drawable.bubble_right_mention : R.drawable.bubble_left_mention);
        }
        messageBubble.setGravity(message.isOwnMessage() ? Gravity.RIGHT : Gravity.LEFT);
        wrapper.setGravity(message.isOwnMessage() ? Gravity.RIGHT : Gravity.LEFT);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (message.isOwnMessage()) {
            params.setMargins(14, 3, 3, 3);
        } else {
            params.setMargins(3, 3, 14, 3);
        }
        messageBubble.setLayoutParams(params);

        // Lange auf Nachricht tippen um Text zu kopieren
        final String tvMessageText = tvMessage.getText().toString();
        tvMessage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cManager = (ClipboardManager) chatActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData cData = ClipData.newPlainText("text", tvMessageText);
                cManager.setPrimaryClip(cData);
                Toast.makeText(chatActivity, R.string.textCopied, Toast.LENGTH_SHORT).show();

                // kurze Vibration
                Vibrator vibrator = (Vibrator) chatActivity.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(200);
                return true;
            }
        });
        return row;
    }

    private String prepareMessage(String rawMessage) {
        if (chatActivity.getPrefs().getBoolean("showEmojis", true)) {
            rawMessage = Emoji.replaceInText(rawMessage);
        }

        return rawMessage;
    }

    public ChatMessage getItem(int index) {
        return this.messages.get(index);
    }
}