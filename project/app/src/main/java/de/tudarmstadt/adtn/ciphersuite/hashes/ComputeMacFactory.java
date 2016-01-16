package de.tudarmstadt.adtn.ciphersuite.hashes;


/**
 * Factory to instantiate an IComputeMAC instance
 */
public class ComputeMacFactory {

    private static IComputeMAC mac;

    /**
     * Instantiates and returns an IComputeMAC instance
     *
     * @return IComputeMAC
     */
    public static IComputeMAC getInstance() throws Exception {
        if(mac == null){
            mac = new Poly1305();
        }
        return mac;
    }

    /**
     * Returns the length of the underlying message authentication code
     * @return returns the length in bytes
     * @throws Exception
     */
    public static int getLength() throws Exception{
        if(mac == null){
            mac = new Poly1305();
        }
        return mac.length();
    }
}
