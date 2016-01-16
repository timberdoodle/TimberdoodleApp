package de.tudarmstadt.timberdoodle.friendkeystore;

import android.content.Context;

import org.spongycastle.jce.X509Principal;
import org.spongycastle.x509.X509V3CertificateGenerator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.GregorianCalendar;

import de.tudarmstadt.adtn.errorlogger.ErrorLoggingSingleton;

/**
 * Stores the own public/private key pair.
 */
public class PrivateKeyStore implements IPrivateKeyStore {

    private final String KEY_ALIAS = "key";

    // The password to use for encryption and decryption.
    private final KeyStore.PasswordProtection protection;

    // Context for file access
    private final Context context;

    private final String filename;

    private volatile KeyPair keyPair;

    /**
     * Creates a new keystore for the own public/private key pair and loads the key pair from the
     * key store file.
     *
     * @param context  The context to use for file access.
     * @param filename The filename of the private key store.
     * @param password The password of the key store.
     * @throws UnrecoverableKeyException If the password is wrong.
     */
    public PrivateKeyStore(Context context, String filename, String password, boolean createEmpty) throws UnrecoverableKeyException {
        if (password.isEmpty()) throw new IllegalArgumentException("password cannot be empty");

        this.context = context;
        this.filename = filename;
        protection = new KeyStore.PasswordProtection(password.toCharArray());

        if (createEmpty) return;

        // Open key store file
        FileInputStream fileStream;
        try {
            fileStream = context.openFileInput(filename);
        } catch (FileNotFoundException e) {
            return;
        }

        // Load key store
        KeyStore keyStore;
        try {
            keyStore = loadKeyStore(fileStream);
            fileStream.close();
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e);
        }

        // Get key pair from store
        PrivateKey privateKey;
        Certificate certificate;
        try {
            privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, protection.getPassword());
            certificate = keyStore.getCertificate(KEY_ALIAS);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e);
        }
        if (privateKey == null || certificate == null) return;
        keyPair = new KeyPair(certificate.getPublicKey(), privateKey);
    }

    /* Loads and returns a key store using the specified input stream and the password stored in the
     * protection attribute. */
    private KeyStore loadKeyStore(InputStream stream)
            throws CertificateException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType(), "BC");
        } catch (NoSuchProviderException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e); // BC is included in the app so it is available
        }
        try {
            keyStore.load(stream, protection.getPassword());
        } catch (IOException e) {
            throw new UnrecoverableKeyException();
        }
        return keyStore;
    }

    /**
     * Persistently stores the key pair.
     */
    @Override
    public void save() {
        try {
            // Create empty key store file
            FileOutputStream fileStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            KeyStore keyStore;
            try {
                keyStore = loadKeyStore(null);
            } catch (UnrecoverableKeyException e) {
                ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
                log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
                throw new RuntimeException(e); // Password cannot be wrong in newly created store
            }

            if (keyPair != null) {
                // Create key store entry
                Certificate[] chain = new Certificate[]{generateCertificate(keyPair)};
                KeyStore.PrivateKeyEntry entry = new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), chain);

                // Store key pair in key store file
                try {
                    keyStore.setEntry(KEY_ALIAS, entry, protection);
                } catch (KeyStoreException e) {
                    throw new RuntimeException(e);
                }
            }

            // Save and close key store file
            keyStore.store(fileStream, protection.getPassword());
            fileStream.close();
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a X509Certificate for the given keypair
     *
     * @param keyPair
     * @return
     */
    private X509Certificate generateCertificate(KeyPair keyPair) {
        Calendar start = new GregorianCalendar();
        Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 200);
        X509V3CertificateGenerator cert = new X509V3CertificateGenerator();
        cert.setSerialNumber(BigInteger.ONE);   //or generate a random number
        cert.setSubjectDN(new X509Principal("CN=localhost"));  //see examples to add O,OU etc
        cert.setIssuerDN(new X509Principal("CN=localhost")); //same since it is self-signed
        cert.setPublicKey(keyPair.getPublic());
        cert.setNotBefore(start.getTime());
        cert.setNotAfter(end.getTime());
        cert.setSignatureAlgorithm("SHA1WithRSAEncryption");
        PrivateKey signingKey = keyPair.getPrivate();
        X509Certificate certChain = null;
        try {
            certChain = cert.generate(signingKey, "BC");
        } catch (CertificateEncodingException | NoSuchProviderException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e);
        }

        return certChain;
    }

    /**
     * @return The own friend cipher key pair.
     */
    @Override
    public KeyPair getKeyPair() {
        return keyPair;
    }

    /**
     * Sets the own friend cipher key pair.
     *
     * @param keyPair The key to use as own private key.
     */
    @Override
    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
        save();
    }
}
