package cl.bposeguros.ms_usuarios.service;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;


@Service
public class ClaveService {

    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijkmnpqrstuvwxyz";
    private static final String NUMEROS = "23456789";
    private static final String ESPECIALES = "@#!$%&*?";
    private static final int LARGO_CLAVE = 12;

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int VUELTAS = 120_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;

    private final SecureRandom generador = new SecureRandom();

    public String generarTemporal() {
        String todos = MAYUSCULAS + MINUSCULAS + NUMEROS + ESPECIALES;
        char[] caracteres = new char[LARGO_CLAVE];
        caracteres[0] = elegir(MAYUSCULAS);
        caracteres[1] = elegir(MINUSCULAS);
        caracteres[2] = elegir(NUMEROS);
        caracteres[3] = elegir(ESPECIALES);
        for (int i = 4; i < LARGO_CLAVE; i++) {
            caracteres[i] = elegir(todos);
        }
        
        for (int i = caracteres.length - 1; i > 0; i--) {
            int j = generador.nextInt(i + 1);
            char auxiliar = caracteres[i];
            caracteres[i] = caracteres[j];
            caracteres[j] = auxiliar;
        }
        return new String(caracteres);
    }

    public String hash(String clave) {
        byte[] sal = new byte[BYTES_SAL];
        generador.nextBytes(sal);
        byte[] hash = derivar(clave, sal, VUELTAS);
        return "pbkdf2$" + VUELTAS + "$" + Base64.getEncoder().encodeToString(sal)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public boolean verificar(String clave, String almacenado) {
        if (clave == null || almacenado == null) {
            return false;
        }
        String[] partes = almacenado.split("\\$");
        if (partes.length != 4 || !partes[0].equals("pbkdf2")) {
            return false;
        }
        try {
            int vueltas = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(clave, sal, vueltas);
            // isEqual compara en tiempo constante: no filtra información por cuánto se demora
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] derivar(String clave, byte[] sal, int vueltas) {
        try {
            PBEKeySpec especificacion = new PBEKeySpec(clave.toCharArray(), sal, vueltas, BITS_HASH);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la clave", e);
        }
    }

    private char elegir(String conjunto) {
        return conjunto.charAt(generador.nextInt(conjunto.length()));
    }
}
