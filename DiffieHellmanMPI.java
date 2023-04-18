import mpi.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import java.io.*;
import java.math.BigInteger;
import java.security.*;

public class DiffieHellmanMPI {

    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (size != 2) {
            System.out.println("This program must be run with 2 processes.");
            MPI.Finalize();
            System.exit(0);
        }

        if (rank == 0) {
            // Generate DH parameters
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            KeyAgreement ka = KeyAgreement.getInstance("DH");
            ka.init(kp.getPrivate());

            // Send public key to process 1
            byte[] pubKeyBytes = kp.getPublic().getEncoded();
            MPI.COMM_WORLD.Send(pubKeyBytes, 0, pubKeyBytes.length, MPI.BYTE, 1, 0);

            // Receive public key from process 1
            byte[] receivedPubKeyBytes = new byte[pubKeyBytes.length];
            MPI.COMM_WORLD.Recv(receivedPubKeyBytes, 0, receivedPubKeyBytes.length, MPI.BYTE, 1, 1);
            KeyFactory kf = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(receivedPubKeyBytes);
            PublicKey receivedPubKey = kf.generatePublic(x509KeySpec);

            // Generate shared secret
            ka.doPhase(receivedPubKey, true);
            byte[] sharedSecret = ka.generateSecret();

            // Encrypt and send text to process 1
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(sharedSecret, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedText = cipher.doFinal("Hello, process 1!".getBytes());
            MPI.COMM_WORLD.Send(encryptedText, 0, encryptedText.length, MPI.BYTE, 1, 2);
        } else {
            // Receive public key from process 0
            byte[] receivedPubKeyBytes = new byte[2048 / 8];
            MPI.COMM_WORLD.Recv(receivedPubKeyBytes, 0, receivedPubKeyBytes.length, MPI.BYTE, 0, 0);
            KeyFactory kf = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(receivedPubKeyBytes);
            PublicKey receivedPubKey = kf.generatePublic(x509KeySpec);

            // Generate DH parameters
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
            DHParameterSpec dhParamSpec = ((DHPublicKey) receivedPubKey).getParams();
            kpg.initialize(dhParamSpec);
            KeyPair kp = kpg.generateKeyPair();
            KeyAgreement ka = KeyAgreement.getInstance("DH");
            ka.init(kp.getPrivate());

            // Send public key to process 0
            byte[] pubKeyBytes = kp.getPublic().getEncoded();
            MPI.COMM_WORLD.Send(pubKeyBytes, 0, pubKeyBytes.length, MPI.BYTE, 0, 1);

            // Generate shared secret
            ka.doPhase(receivedPubKey, true);
            byte[] sharedSecret = ka.generateSecret();

            // Receive and decrypt text from process 0
            byte[]
        byte[] receivedEncryptedText = new byte[128];
        MPI.COMM_WORLD.Recv(receivedEncryptedText, 0, receivedEncryptedText.length, MPI.BYTE, 0, 2);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(sharedSecret, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decryptedText = cipher.doFinal(receivedEncryptedText);
        System.out.println(new String(decryptedText));
    }

    MPI.Finalize();
}

   
   /*The steps to follow the task programming model for Diffie Hellman Secure text transfer is:

Two processes are created using MPI, one for each party involved in the communication. Process 0 generates Diffie-Hellman parameters and sends its public key to
process 1. Process 1 generates its own public key using the received parameters, and sends its public key back to process 0. Both processes then generate a shared
secret using their own private key and the received public key, and use it to encrypt and decrypt text messages.

   */
