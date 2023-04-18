import java.math.BigInteger;
import java.util.Random;

public class DiffieHellmanMultiThreaded {

    private BigInteger p;
    private BigInteger g;
    private BigInteger privateKey;
    private BigInteger publicKey;

    public DiffieHellmanMultiThreaded() {
        // Generate prime number p and generator g
        Random random = new Random();
        int bitLength = 512;
        p = BigInteger.probablePrime(bitLength, random);
        g = BigInteger.valueOf(2);
        
        // Generate private key and public key
        privateKey = new BigInteger(bitLength, random);
        publicKey = g.modPow(privateKey, p);
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public BigInteger getSharedSecret(BigInteger otherPublicKey) {
        return otherPublicKey.modPow(privateKey, p);
    }

    public static void main(String[] args) throws InterruptedException {
        DiffieHellmanMultiThreaded alice = new DiffieHellmanMultiThreaded();
        DiffieHellmanMultiThreaded bob = new DiffieHellmanMultiThreaded();
        
        Thread aliceThread = new Thread(() -> {
            BigInteger alicePublicKey = alice.getPublicKey();
            BigInteger sharedSecret = alice.getSharedSecret(bob.getPublicKey());
            System.out.println("Alice shared secret: " + sharedSecret);
        });
        
        Thread bobThread = new Thread(() -> {
            BigInteger bobPublicKey = bob.getPublicKey();
            BigInteger sharedSecret = bob.getSharedSecret(alice.getPublicKey());
            System.out.println("Bob shared secret: " + sharedSecret);
        });
        
        aliceThread.start();
        bobThread.start();
        
        aliceThread.join();
        bobThread.join();
    }
}

/*

This implementation generates two instances of the DiffieHellmanMultiThreaded class, one for Alice and one for Bob. Each instance generates a private key and a
public key. Then, two threads are created, one for Alice and one for Bob. Each thread executes the code for generating a shared secret using the public key of the
other party. Finally, the main thread waits for the two threads to finish using the join() method, and prints out the two shared secrets.
The multithreading code can be used in the Diffie-Hellman key exchange code to speed up the computation of the shared secret key. 
*/
