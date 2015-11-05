package de.tu_darmstadt.adtn.errorloggingtests;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import java.lang.reflect.Method;

import de.tu_darmstadt.adtn.errorlogger.ErrorLoggingSingleton;

/**
 * Tests for ErrorLoggingSingleton
 */
public class Errorloggertests extends AndroidTestCase {
    //Dummy stack trace
    private final StackTraceElement[] stackTrace =
            {
                    new StackTraceElement("classA", "methodA", "fileA", 10),
                    new StackTraceElement("classB", "methodB", "fileB", 20),
                    new StackTraceElement("classC", "methodC", "fileC", 30),
                    new StackTraceElement("classD", "methodD", "fileD", 40),
                    new StackTraceElement("classE", "methodE", "fileE", 50),
                    new StackTraceElement("classF", "methodF", "fileF", 60),
                    new StackTraceElement("classG", "methodG", "fileG", 70),
            };
    //System line separator
    private final String lineSeparator = System.getProperty("line.separator");
    //Result for dummy stack trace after method calls
    private final String stackTraceResult =
            "Exception" + lineSeparator +
                    "classA.methodA(fileA:10)" + lineSeparator +
                    "\tclassB.methodB(fileB:20)" + lineSeparator +
                    "\t\tclassC.methodC(fileC:30)" + lineSeparator +
                    "\t\t\tclassD.methodD(fileD:40)" + lineSeparator +
                    "\t\t\t\tclassE.methodE(fileE:50)" + lineSeparator +
                    "\t\t\t\t\tclassF.methodF(fileF:60)" + lineSeparator +
                    "\t\t\t\t\t\tclassG.methodG(fileG:70)";
    //Mail body
    private final String mailbody =
            stackTraceResult + lineSeparator + lineSeparator
                    + "Technical information: " + lineSeparator
                    + "SDK level: " + Build.VERSION.SDK_INT;
    //Unit under test
    private ErrorLoggingSingleton uut;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        uut = ErrorLoggingSingleton.getInstance();
        uut.setContext(new RenamingDelegatingContext(getContext(), "test."));
        uut.storeError(stackTraceResult);
    }

    @SmallTest
    public void testStackTraceToString() {
        Exception e = new Exception();
        e.setStackTrace(stackTrace);
        String result = ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e);
        assertEquals(stackTraceResult, result);
    }

    @MediumTest
    public void testHasError() {
        assertTrue(uut.hasError());
    }

    @MediumTest
    public void testClearLog() {
        uut.clearLog();
        assertFalse(uut.hasError());
    }

    @MediumTest
    public void testStoreError() {
        String simpleMessage = "An error occurred";
        assertTrue(uut.storeError(simpleMessage));
    }

    @MediumTest
    public void testReadLog() throws Exception {
        Class cut = uut.getClass();
        Method mut = cut.getDeclaredMethod("readLog");
        mut.setAccessible(true);
        String result = (String) mut.invoke(uut);
        assertEquals(stackTraceResult, result);
    }

    @MediumTest
    public void testCreateBody() throws Exception {
        Class cut = uut.getClass();
        Method mut = cut.getDeclaredMethod("createBody");
        mut.setAccessible(true);
        String result = (String) mut.invoke(uut);
        assertEquals(mailbody, result);
    }

    @SmallTest
    public void testGetErrorMail() {
        Intent intent = uut.getErrorMail();
        Bundle extras = intent.getExtras();
        String devMail = "jaegermeister_europa@yahoo.de";
        boolean rightAdress = devMail.equals(extras.getStringArray(Intent.EXTRA_EMAIL)[0]);
        String subject = "Error log of timberdoodle";
        boolean rightSubject = subject.equals(extras.getString(Intent.EXTRA_SUBJECT));
        boolean rightBody = mailbody.equals(extras.getString(Intent.EXTRA_TEXT));
        assertTrue(rightAdress && rightBody && rightSubject);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        uut.clearLog();
    }
}
