package de.tudarmstadt.adtn.errorlogger;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Singleton that handles error logs and stores them. This singleton is used due to multiple
 * threads where errors/exceptions might occur and call this class accordingly.
 */
public class ErrorLoggingSingleton {

    private static final String TAG = "ErrorLoggingSingleton";
    //file name
    private static final String FILENAME = "ErrorLog.txt";
    //developer mail adress or the adress of someone responsible
    private static final String DEV_MAIL = "dev_timberoodle@you-spam.com";
    //mail subject
    private static final String SUBJECT = "Error log of timberdoodle";
    //context that is used.
    private volatile Context context = null;
    private static volatile ErrorLoggingSingleton instance = null;

    /**
     * Mandatory private constructor.
     */
    private ErrorLoggingSingleton() {
    }

    /**
     * Returns the singleton instance.
     *
     * @return ErrorLoggingSingleton
     */
    public static ErrorLoggingSingleton getInstance() {
        if(instance == null){
            instance = new ErrorLoggingSingleton();
        }
        return instance;
    }

    /**
     * Transforms a stack trace from an Exception to a String.
     *
     * @param e Exception that has the stack trace
     * @return returns the stack trace as String.
     */
    public static String getExceptionStackTraceAsFormattedString(Exception e) {
        String ls = System.getProperty("line.separator");
        int i = 0;
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getSimpleName()).append(ls);
        StackTraceElement[] st = e.getStackTrace();
        for (StackTraceElement trace : st) {
            for (int j = 0; j < i; ++j) {
                sb.append("\t");
            }
            sb.append(trace.toString()).append(ls);
            ++i;
        }
        return sb.toString().trim();
    }

    /**
     * Setter for a Context instance. The Context instance should always be the
     * application Context in order to circumvent arbitrary behaviour.
     *
     * @param applicationContext context that is used (should be the application context
     * @return Returns true if the passed in context is set. False if there
     * already is an existing context.
     */
    public boolean setContext(Context applicationContext) {
        if (context == null) {
            context = applicationContext;
            return true;
        } else return false;
    }

    /**
     * Check if there was an Error/Exception
     *
     * @return returns true if there is an entry in the log, else false.
     */
    public boolean hasError() {
        File errorLog = context.getFileStreamPath(FILENAME);
        return errorLog.exists();
    }

    /**
     * Clears the error/exception log.
     *
     * @return Returns true if the log is clear, else false
     * (i.e. if false is returned, an error/exception occured.
     */
    public boolean clearLog() {
        File errorLog = context.getFileStreamPath(FILENAME);
        return errorLog.delete();
    }

    /**
     * Stores the error log in a file
     *
     * @return returns true if the log was stored, else false.
     */
    public boolean storeError(String errorlog) {
        try {
            if (context != null) {
                //Open FileOutputStream to write
                FileOutputStream out = context.openFileOutput(FILENAME, Context.MODE_APPEND);
                BufferedOutputStream writer = new BufferedOutputStream(out);
                //write
                writer.write(errorlog.getBytes());
                //Close file stream
                writer.close();
                return true;
            } else return false;
        } catch (IOException e) {
            Log.wtf(TAG, e);
            return false;
        }
    }

    /**
     * Reads the error log.
     *
     * @return returns the whole error log as String.
     */
    private String readLog() throws IOException{
        //Initialisation
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        StringBuilder sb = new StringBuilder();
        //Open FileInputStream and create a BufferedInputStream with it (for performance)
        FileInputStream in = context.openFileInput(FILENAME);
        BufferedInputStream reader = new BufferedInputStream(in);
        //Read the text file.
        while (reader.read(buffer) != -1) {
            sb.append(new String(buffer));
        }
        //close the reader and return result
        reader.close();
        return sb.toString().trim();
    }

    /**
     * Assembles information that concerns the error and
     * puts them in a string to build the mail body
     *
     * @return mail body as string
     */
    private String createBody() {
        //Initialisation of line separator and the StringBuilder
        String ls = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();
        //Build mail body
        //read stack trace
        try {
            sb.append(readLog());
        } catch(IOException e) {
            Log.w(TAG, e);
        }
        //insert a blank line
        sb.append(ls).append(ls);
        //technical information (currently the api level
        sb.append("Technical information: ").append(ls);
        sb.append("SDK level: ").append(Build.VERSION.SDK_INT);
        return sb.toString();
    }

    /**
     * Creates an e-mail template for the error report. The template
     * contains the e-mail adress, subject,
     * currently stored error logs (with the last being the most
     * current one) and technical Information (OS version)
     * Beware of the fact that this is not supported on API Level 3
     * (this comment might be redundant).
     * The body of the mail is passed in as plain text not as html, thus
     * it is accessed as EXTRA_TEXT.
     *
     * @return Returns the E-Mail template as an Intent.
     */
    public Intent getErrorMail() {
        //Setup Intent to call a mailing app
        Intent result = new Intent(Intent.ACTION_SEND);
        //Set type
        result.setType("text/plain");
        //Set adress
        result.putExtra(Intent.EXTRA_EMAIL, new String[]{DEV_MAIL});
        //Set subject
        result.putExtra(Intent.EXTRA_SUBJECT, SUBJECT);
        //create and set mail body
        result.putExtra(Intent.EXTRA_TEXT, createBody());
        //return Intent for further use
        return result;
    }
}
