package de.tudarmstadt.timberdoodle.ui.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.AlignmentSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.tudarmstadt.timberdoodle.IService;
import de.tudarmstadt.timberdoodle.R;
import de.tudarmstadt.timberdoodle.chatlog.IChatLog;
import de.tudarmstadt.timberdoodle.chatlog.PublicChatLogEntry;

/**
 * A simple {@link Fragment} subclass.
 * Activites or Fragments that use this Fragment should first create this Fragment with
 * new DisplayPublicMessageFragment(), create a Bundle and pass in the Bundle by calling setArguments(Bundle bundle).
 * <p/>
 * Example:
 * <p/>
 * //create Bundle that holds the fragments arguments
 * Bundle arguments = new Bundle();
 * //put arguments into Bundle
 * arguments.putString(DisplayPublicMessageFragment.MESSAGE_KEY, message1);
 * arguments.putSerializable(DisplayPublicMessageFragment.DATE_KEY, new Date());
 * //pass Bundle to Fragment
 * fragment.setArguments(arguments);
 */
public class DisplayPublicMessageFragment extends DoodleFragment {

    /**
     * If the activity result is RESULT_OK and the message ID specified in this result argument
     * is not 0 then the message should be deleted.
     */
    public static final String OUTARGNAME_DELETE = "delete";
    private static final String ARGNAME_MESSAGE_ID = "message_id";
    // Formats the timestamp of the message
    private final DateTimeFormatter timestampFormatter = DateTimeFormat.longDateTime();

    /**
     * Creates the bundle containing the arguments to pass to this fragment.
     *
     * @param messageId The ID of the message to display.
     * @return The argument bundle.
     */
    public static Bundle createArgs(long messageId) {
        Bundle args = new Bundle(1);
        args.putLong(ARGNAME_MESSAGE_ID, messageId);
        return args;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_public_message, container, false);

        // Set scrollbar
        TextView txtPublicMessage = (TextView) view.findViewById(R.id.txtPublicMessage);
        txtPublicMessage.setMovementMethod(new ScrollingMovementMethod());

        setHelpString(R.string.help_12);
        setViewReady(view);
        return view;
    }

    @Override
    protected void onViewAndServiceReady(View view, IService service) {
        super.onViewAndServiceReady(view, service);

        final long messageID = getArguments().getLong(ARGNAME_MESSAGE_ID);
        final IChatLog chatLog = service.getChatLog();

        // Create string to display
        PublicChatLogEntry message = chatLog.getPublicMessage(messageID);
        String content = message.getContent();
        String timestamp = timestampFormatter.print(new LocalDateTime(message.getTimestamp()));
        SpannableString displayText = new SpannableString("Date: " + timestamp + "\n\n" + content);
        displayText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                content.length(), displayText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Display message string in TextView.
        TextView txtPublicMessage = (TextView) view.findViewById(R.id.txtPublicMessage);
        txtPublicMessage.setText(displayText);

        Button button = (Button) view.findViewById(R.id.delete_public_msg_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Warnung -> Delete ja nein
                // Löschen ->
                AlertDialog deleteDialog = new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.message_delete_dialog)
                        .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Close activity and indicate in result that message should be deleted
                                FragmentActivity activity = getActivity();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra(OUTARGNAME_DELETE, messageID);
                                activity.setResult(Activity.RESULT_OK, resultIntent);
                                activity.finish();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
                deleteDialog.show();
            }
        });

        // Mark message as read
        chatLog.setPublicMessageRead(messageID);
    }
}
