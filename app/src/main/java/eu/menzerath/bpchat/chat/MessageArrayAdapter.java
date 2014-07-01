package eu.menzerath.bpchat.chat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.menzerath.bpchat.ChatActivity;
import eu.menzerath.bpchat.R;

/**
 * Der Adapter für das ListView, welches die Nachrichten anzeigt
 */
public class MessageArrayAdapter extends ArrayAdapter<ChatMessage> {
    private ChatActivity chatActivity;
    private List<ChatMessage> messages = new ArrayList<ChatMessage>(); // Liste mit den Chat-Nachrichten

    public MessageArrayAdapter(ChatActivity chatActivity, int textViewResourceId) {
        super(chatActivity.getApplicationContext(), textViewResourceId);
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
        tvFrom.setText(message.getFrom().equalsIgnoreCase(chatActivity.getUser().username) && chatActivity.getPrefs().getBoolean("twoBubbles", true) ? "" : message.getFrom());
        tvTime.setText(Helper.formatDateToString(message.getTime()));
        tvMessage.setText(chatActivity.getPrefs().getBoolean("showEmojis", true) ? Emoji.replaceInText(message.getMessage()) : message.getMessage());

        // Anpassung der Blasen für besseren Look
        messageBubble.setBackgroundResource(message.isLeft() ? R.drawable.bubble_left : R.drawable.bubble_right);
        messageBubble.setGravity(message.isLeft() ? Gravity.LEFT : Gravity.RIGHT);
        wrapper.setGravity(message.isLeft() ? Gravity.LEFT : Gravity.RIGHT);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (message.isLeft()) {
            params.setMargins(3, 3, 14, 3);
        } else {
            params.setMargins(14, 3, 3, 3);
        }
        messageBubble.setLayoutParams(params);
        return row;
    }

    public ChatMessage getItem(int index) {
        return this.messages.get(index);
    }
}