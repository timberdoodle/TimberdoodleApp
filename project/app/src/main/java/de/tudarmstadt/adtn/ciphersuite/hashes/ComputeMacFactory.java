package de.tudarmstadt.adtn.ciphersuite.hashes;


import java.security.NoSuchAlgorithmException;

/**
 * Factory to instantiate an IComputeMAC instance
 */
public class ComputeMacFactory {

    private static IComputeMAC mac;


    private ComputeMacFactory() {}

    /**
     * Instantiates and returns an IComputeMAC instance
     *
     * @return IComputeMAC
     * @throws NoSuchAlgorithmException
     */
    public static IComputeMAC getInstance() throws NoSuchAlgorithmException {
        if(mac == null){
            mac = new Poly1305();
        }
        return mac;
    }

    /**
     * Returns the length of the underlying message authentication code
     * @return returns the length in bytes
     * @throws NoSuchAlgorithmException
     */
    public static int getLength() throws NoSuchAlgorithmException{
        if(mac == null){
            mac = new Poly1305();
        }
        return mac.length();
    }
}
