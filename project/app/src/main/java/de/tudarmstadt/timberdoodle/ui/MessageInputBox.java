package de.tudarmstadt.timberdoodle.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderMalfunctionError;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import de.tudarmstadt.timberdoodle.R;

/**
 * A message input text box that displays the number of remaining characters.
 */
public class MessageInputBox extends LinearLayout {

    private final static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private final static int DEFAULT_MAX_BYTES = 100;

    // CharsetEncoder for the message charset.
    private CharsetEncoder charsetEncoder;

    // Buffer that takes the encoded message. Size is the maximum message length in bytes.
    private ByteBuffer encodeBuffer;

    // Where the users types his message.
    private EditText edit_inputMessage;

    // Shows the remaining number of characters.
    private TextView text_charactersLeft;

    private Runnable resetCharsLeftStyle;
    private ColorStateList charactersLeftDefaultTextColors;

    // Number of bytes in the encoded message
    private int encodedSize;

    // Average number of bytes per character in the message
    private double avgBytesPerChar;

    public MessageInputBox(Context context) {
        super(context);
        init(context);
    }

    public MessageInputBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MessageInputBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * Returns the message the user typed if it is valid and clears the text field.
     *
     * @return A string that has the correct format according to the encoding and maximum number
     * of encoded bytes or null if the message is empty.
     */
    public String getValidatedMessageAndClear() {
        String message = edit_inputMessage.getText().toString();

        // Empty message?
        if (message.length() == 0) {
            showToastAndErrorVibrate(R.string.messageInputBox_messageIsEmpty);
            return null;
        }

        // Clear EditText and return the message
        clearMessage();
        return message;
    }

    /**
     * Set the message to display. If it is too long, it will be truncated.
     *
     * @param message The message to display.
     */
    public void setMessage(CharSequence message) {
        int useLength = prepareUpdateMessage(message);

        // Use empty message if an invalid message was passed
        if (useLength == -1) {
            clearMessage();
            return;
        }

        // Use the (possibly shortened) message
        edit_inputMessage.setText(message.subSequence(0, useLength));
    }

    // Clears the text field and updates the "characters left" display accordingly
    private void clearMessage() {
        edit_inputMessage.setText("");
        showCharactersLeft(encodeBuffer.limit(), charsetEncoder.averageBytesPerChar(), false);
    }

    /**
     * Lets the phone vibrate so the user notices that an error occurred.
     */
    private void doErrorVibration() {
        ((Vibrator) (getContext().getSystemService(Context.VIBRATOR_SERVICE))).vibrate(1000);
    }

    /**
     * Shows the specified toast and causes an error vibration.
     *
     * @param resId The resource ID of the string to be displayed in the toast.
     */
    private void showToastAndErrorVibrate(int resId) {
        Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
        doErrorVibration();
    }

    /**
     * Updates text_charactersLeft.
     *
     * @param bytesRemaining  Number of unused message bytes of the packet.
     * @param avgBytesPerChar Average number of bytes per character in the message.
     */
    private void showCharactersLeft(int bytesRemaining, double avgBytesPerChar, boolean showError) {
        // Show warning if number of remaining characters is less than or equal to
        final int warnCharsLeft = 10;

        // Update encoded info
        encodedSize = encodeBuffer.limit() - bytesRemaining;
        this.avgBytesPerChar = avgBytesPerChar;

        // Remove possibly running handler that removes the warning style
        removeCallbacks(resetCharsLeftStyle);

        // Vibrate and switch to error style if showError is set
        if (showError) {
            doErrorVibration();
            // Set error style for 1 second
            text_charactersLeft.setError("");
            text_charactersLeft.setTextColor(Color.RED);
            postDelayed(resetCharsLeftStyle, 1000);
        } else {
            // Revert to non-error style
            resetCharsLeftStyle.run();
        }

        int charsLeft = (int) Math.ceil(bytesRemaining / avgBytesPerChar);

        // Hide text_charactersLeft if it has nothing to display
        if (charsLeft > warnCharsLeft) {
            text_charactersLeft.setVisibility(GONE);
            return;
        } else { // Make visible in case it was invisible
            text_charactersLeft.setVisibility(VISIBLE);
        }

        // Update text
        if (bytesRemaining == 0) {
            text_charactersLeft.setText(R.string.messageInputBox_noCharsLeft);
        } else if (charsLeft == 1) {
            text_charactersLeft.setText(R.string.messageInputBox_oneCharLeft);
        } else {
            text_charactersLeft.setText(getResources().getString(R.string.messageInputBox_charsLeft, charsLeft));
        }
    }

    /**
     * Initializations that every constructor of this view has to perform.
     *
     * @param context The Context the view is running in.
     */
    private void init(Context context) {
        // Set properties of LinearLayout
        setOrientation(VERTICAL);

        // Inflate layout and store references to contained views
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_message_input_box, this);
        edit_inputMessage = (EditText) findViewById(R.id.edit_inputMessage);
        text_charactersLeft = (TextView) findViewById(R.id.text_charactersLeft);

        // Workaround: Ignored if set in layout file
        edit_inputMessage.setHorizontallyScrolling(false);
        edit_inputMessage.setMaxLines(3);

        // Create encoder for message charset and output buffer
        createCharsetEncoder(DEFAULT_CHARSET);
        createEncodeBuffer(DEFAULT_MAX_BYTES);

        // Monitor typed message for changes and refuse or shorten if necessary.
        edit_inputMessage.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                // Create the resulting message
                SpannableStringBuilder newMessage = new SpannableStringBuilder(dest).replace(dstart, dend, source, start, end);

                int useLength = prepareUpdateMessage(newMessage);
                if (useLength == newMessage.length()) { // newMessage can be used as is
                    return null;
                } else if (useLength == 0 || useLength == -1) { // newMessage is empty or invalid
                    return "";
                } else { // newMessage has to be shortened to fit
                    return new InputFilter.LengthFilter(useLength).filter(source, start, end, dest, dstart, dend);
                }
            }
        }});

        // Create runnable that resets the default style of text_charactersLeft
        resetCharsLeftStyle = new Runnable() {
            @Override
            public void run() {
                text_charactersLeft.setError(null);
                text_charactersLeft.setTextColor(charactersLeftDefaultTextColors);
            }
        };

        // Save default text colors of edit_inputMessage
        charactersLeftDefaultTextColors = text_charactersLeft.getTextColors();

        clearMessage();
    }

    /**
     * Calculates the max. number of chars of newMessage that would still fit in the encode buffer
     * after encoding and shows the estimated number of remaining characters if the (possibly
     * truncated) newMessage was used as new message.
     *
     * @param newMessage The message that should be assumed to be used.
     * @return Number of chars of newMessage that can be used or -1 if the message is invalid.
     * If -1 is returned, the remaining characters info is not updated.
     */
    private int prepareUpdateMessage(CharSequence newMessage) {
        // Encode newMessage
        CharBuffer inputBuffer = CharBuffer.wrap(newMessage);
        CoderResult coderResult = tryEncodeMessage(inputBuffer);
        if (coderResult == null) return -1; // Message could not be encoded?

        // Calculate estimated number of remaining characters and show them
        int numChars = inputBuffer.position(), numBytes = encodeBuffer.position();
        double avgBytesPerChar = numChars == 0 ? charsetEncoder.averageBytesPerChar() : (double) numBytes / numChars;
        showCharactersLeft(encodeBuffer.remaining(), avgBytesPerChar, coderResult.isOverflow());

        // Return number of characters in newMessage whose encoded bytes fit in the packet
        return numChars;
    }

    /**
     * Tries to encode inputBuffer into encodeBuffer using charsetEncoder and shows a warning if an
     * error occurred.
     *
     * @param inputBuffer The characters to be encoded.
     * @return An overflow or underflow CoderResult or null if encoding failed.
     */
    private CoderResult tryEncodeMessage(CharBuffer inputBuffer) {
        // Encode message
        encodeBuffer.position(0);
        charsetEncoder.reset();
        CoderResult result = null;
        boolean gotException = false;
        try {
            result = charsetEncoder.encode(inputBuffer, encodeBuffer, true);
        } catch (CoderMalfunctionError ex) {
            gotException = true;
        }

        // Inform user if encoding failed and return null
        if (gotException || result.isError()) {
            showToastAndErrorVibrate(R.string.messageInputBox_encodingMessageFailed);
            return null;
        }

        // Return successful CoderResult
        return result;
    }

    /**
     * @return The number of bytes in the encoded message
     */
    public int getEncodedSize() {
        return encodedSize;
    }

    /**
     * @return The average number of bytes per character in the message.
     */
    public double getAvgBytesPerChar() {
        return avgBytesPerChar;
    }

    /**
     * Sets the charset to use for calculating the remaining number of remaining chars when using
     * the encoding of the charset.
     *
     * @param charset The charset to use for calculation of remaining characters.
     */
    public void setCharset(Charset charset) {
        createCharsetEncoder(charset);
        setMessage(edit_inputMessage.getText()); // Update message and truncate if needed
    }

    /**
     * Sets the maximum number of bytes the encoded message must not exceed.
     *
     * @param size The maximum number of bytes in the encoded message.
     */
    public void setMaxBytes(int size) {
        createEncodeBuffer(size);
        setMessage(edit_inputMessage.getText()); // Update message and truncate if needed
    }

    // Creates the charset encoder for the specified charset
    private void createCharsetEncoder(Charset charset) {
        charsetEncoder = charset.newEncoder();
        charsetEncoder.onMalformedInput(CodingErrorAction.REPLACE);
        charsetEncoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    // Creates the buffer where the encoded message is stored using the specified size
    private void createEncodeBuffer(int size) {
        encodeBuffer = ByteBuffer.allocate(size);
    }
}
