package de.tudarmstadt.timberdoodle.ui.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import de.tudarmstadt.timberdoodle.R;
import de.tudarmstadt.timberdoodle.messagehandler.IMessageHandler;
import de.tudarmstadt.timberdoodle.ui.MessageInputBox;

/**
 * A fragment is shown, when the send button on the main
 * screen is clicked. Fragment contains 3 buttons: Discard,
 * Modify, Send with appropriate functionality.
 */
public class MessageReviewFragment extends DoodleFragment {
    public final static String OUTARGNAME_MESSAGE = "message";
    private final static String ARGNAME_MESSAGE = "message";
    private final static String ARGNAME_MAXBYTES = "maxbytes";

    public static Bundle createArgs(String message, int maxBytes) {
        Bundle args = new Bundle(2);
        args.putString(ARGNAME_MESSAGE, message);
        args.putInt(ARGNAME_MAXBYTES, maxBytes);
        return args;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review_message, container, false);
        final ViewSwitcher switcher = (ViewSwitcher) view.findViewById(R.id.my_switcher);

        // Set ActionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(R.string.review_message);

        // Set text
        String passedMessage = getArguments().getString(ARGNAME_MESSAGE);
        final TextView textView = (TextView) view.findViewById(R.id.textView_modify);
        textView.setText(passedMessage);
        final MessageInputBox editableMsg = (MessageInputBox) switcher.findViewById(R.id.hidden_message_input);
        editableMsg.setCharset(IMessageHandler.CHAT_MESSAGE_CHARSET);
        editableMsg.setMaxBytes(getArguments().getInt(ARGNAME_MAXBYTES));
        editableMsg.setMessage(passedMessage);

        // Set Scrollbar
        textView.setMovementMethod(new ScrollingMovementMethod());

        // Handle Discard button
        final Button discardButton = (Button) view.findViewById(R.id.discard_button);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                activity.setResult(Activity.RESULT_CANCELED);
                activity.finish();
            }
        });

        // Handle Modify button
        final Button modifyButton = (Button) view.findViewById(R.id.modify_button);
        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modifyButton.getText().equals(getString(R.string.review_modify))) {
                    modifyButton.setText(R.string.review_review);
                    editableMsg.setMessage(textView.getText().toString());
                    switcher.showNext();
                } else if (modifyButton.getText().equals(getString(R.string.review_review))) {
                    String validatedMessage = editableMsg.getValidatedMessageAndClear();
                    if (validatedMessage != null) {
                        modifyButton.setText(R.string.review_modify);
                        textView.setText(validatedMessage);
                        editableMsg.setMessage(validatedMessage);
                        switcher.showNext();
                    }
                }
            }
        });

        // tapping on text will open the MessageInputBox
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyButton.setText(getString(R.string.review_review));
                editableMsg.setMessage(textView.getText().toString());
                switcher.showNext();
            }
        });

        // handle Send button
        view.findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editableMsg.getValidatedMessageAndClear();
                if (message == null) return;

                FragmentActivity activity = getActivity();
                Intent resultIntent = new Intent();
                resultIntent.putExtra(OUTARGNAME_MESSAGE, message);
                activity.setResult(Activity.RESULT_OK, resultIntent);
                activity.finish();
            }
        });

        setHelpString(R.string.help_11);
        setViewReady(view);
        return view;
    }
}
