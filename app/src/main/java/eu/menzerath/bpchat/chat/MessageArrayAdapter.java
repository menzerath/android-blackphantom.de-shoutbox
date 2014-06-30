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

import eu.menzerath.bpchat.R;

/**
 * Der Adapter für das ListView, welches die Nachrichten anzeigt
 */
public class MessageArrayAdapter extends ArrayAdapter<ChatMessage> {
    private List<ChatMessage> messages = new ArrayList<ChatMessage>(); // Liste mit den Chat-Nachrichten

    public MessageArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
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
        TextView tvMessage = (TextView) row.findViewById(R.id.message);
        TextView tvFrom = (TextView) row.findViewById(R.id.data);

        // Setzt die Nachricht / den Absender + Zeitpunkt
        tvMessage.setText(message.getMessage());
        tvFrom.setText(message.getFrom() + " - " + Helper.formatDateToString(message.getTime()));

        // Blase links: gelb
        // Blase rechts: grün
        messageBubble.setBackgroundResource(message.isLeft() ? R.drawable.bubble_yellow : R.drawable.bubble_green);
        messageBubble.setGravity(message.isLeft() ? Gravity.LEFT : Gravity.RIGHT);
        wrapper.setGravity(message.isLeft() ? Gravity.LEFT : Gravity.RIGHT);
        return row;
    }

    public ChatMessage getItem(int index) {
        return this.messages.get(index);
    }
}