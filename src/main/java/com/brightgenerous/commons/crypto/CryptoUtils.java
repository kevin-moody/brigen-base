package com.brightgenerous.commons.crypto;

import static com.brightgenerous.commons.ObjectUtils.*;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.brightgenerous.commons.EqualsUtils;
import com.brightgenerous.commons.HashCodeUtils;
import com.brightgenerous.commons.ToStringUtils;
import com.brightgenerous.lang.Args;

public class CryptoUtils implements Serializable {

    private static final long serialVersionUID = 1285931778321061231L;

    static class InstanceKey implements Serializable {

        private static final long serialVersionUID = -5571606798438371038L;

        private final CryptoAlgorithm algorithm;

        private final HashAlgorithm keyAlgorithm;

        private final byte[] key;

        public InstanceKey(CryptoAlgorithm algorithm, HashAlgorithm keyAlgorithm, byte[] key) {
            this.algorithm = algorithm;
            this.keyAlgorithm = keyAlgorithm;
            this.key = key;
        }

        @Override
        public int hashCode() {
            final int multiplier = 37;
            int result = 17;
            result = (multiplier * result) + hashCodeEscapeNull(algorithm);
            result = (multiplier * result) + hashCodeEscapeNull(keyAlgorithm);
            result = (multiplier * result) + hashCodeEscapeNull(key);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof InstanceKey)) {
                return false;
            }

            InstanceKey other = (InstanceKey) obj;

            if (!equalsEscapeNull(algorithm, other.algorithm)) {
                return false;
            }
            if (!equalsEscapeNull(keyAlgorithm, other.keyAlgorithm)) {
                return false;
            }
            if (!equalsEscapeNull(key, other.key)) {
                return false;
            }
            return true;
        }
    }

    private final CryptoAlgorithm algorithm;

    private final HashAlgorithm keyAlgorithm;

    private final byte[] key;

    protected CryptoUtils(CryptoAlgorithm algorithm, byte[] key) {
        this(algorithm, null, key);
    }

    protected CryptoUtils(CryptoAlgorithm algorithm, HashAlgorithm keyAlgorithm, byte[] key) {
        Args.notNull(algorithm, "algorithm");
        Args.notNull(key, "key");

        this.algorithm = algorithm;
        this.keyAlgorithm = keyAlgorithm;
        this.key = key;
    }

    public static CryptoUtils get(CryptoAlgorithm algorithm, byte[] key) {
        return getInstance(algorithm, null, key);
    }

    public static CryptoUtils get(CryptoAlgorithm algorithm, HashAlgorithm keyAlgorithm, byte[] key) {
        return getInstance(algorithm, keyAlgorithm, key);
    }

    private static volatile Map<InstanceKey, SoftReference<CryptoUtils>> cache;

    protected static CryptoUtils getInstance(CryptoAlgorithm algorithm, HashAlgorithm keyAlgorithm,
            byte[] key) {
        if (cache == null) {
            synchronized (CryptoUtils.class) {
                if (cache == null) {
                    cache = new ConcurrentHashMap<>();
                }
            }
        }
        InstanceKey ik = new InstanceKey(algorithm, keyAlgorithm, key);
        SoftReference<CryptoUtils> sr = cache.get(ik);
        CryptoUtils ret;
        if (sr != null) {
            ret = sr.get();
            if (ret != null) {
                return ret;
            }
            Set<InstanceKey> dels = new HashSet<>();
            for (Entry<InstanceKey, SoftReference<CryptoUtils>> entry : cache.entrySet()) {
                if (entry.getValue().get() == null) {
                    dels.add(entry.getKey());
                }
            }
            for (InstanceKey del : dels) {
                cache.remove(del);
            }
        }
        ret = new CryptoUtils(algorithm, keyAlgorithm, key);
        cache.put(ik, new SoftReference<>(ret));
        return ret;
    }

    public byte[] encrypt(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeyException,
            InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException {
        return encrypt(algorithm, keyAlgorithm, key, bytes);
    }

    public byte[] decrypt(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeyException,
            InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException {
        return decrypt(algorithm, keyAlgorithm, key, bytes);
    }

    public static byte[] encrypt(CryptoAlgorithm algorithm, byte[] key, byte[] bytes)
            throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return encrypt(algorithm, null, key, bytes);
    }

    public static byte[] encrypt(CryptoAlgorithm algorithm, HashAlgorithm keyAlgorithm, byte[] key,
            byte[] bytes) throws NoSuchAlgorithmException, InvalidKeyException,
            InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException {
        Args.notNull(algorithm, "algorithm");
        Args.notNull(key, "key");
        Args.notNull(bytes, "bytes");

        SecretKey secretKey;
        switch (algorithm) {
            case DES:
                secretKey = SecretKeyFactory.getInstance(algorithm.value()).generateSecret(
                        new DESKeySpec(getPssKey(algorithm, keyAlgorithm, key)));
                break;
            case DESEDE:
                secretKey = SecretKeyFactory.getInstance(algorithm.value()).generateSecret(
                        new DESedeKeySpec(getPssKey(algorithm, keyAlgorithm, key)));
                break;
            case AES:
                secretKey = new SecretKeySpec(getPssKey(algorithm, keyAlgorithm, key),
                        algorithm.value());
                break;
            default:
                throw new IllegalStateException();
        }
        Cipher cipher = Cipher.getInstance(algorithm.value());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(bytes);
    }

    public static byte[] decrypt(CryptoAlgorithm algorithm, byte[] key, byte[] bytes)
            throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return decrypt(algorithm, null, key, bytes);
    }

    public static byte[] decrypt(CryptoAlgorithm algorithm, HashAlgorithm keyAlgorithm, byte[] key,
            byte[] bytes) throws NoSuchAlgorithmException, InvalidKeyException,
            InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException {
        Args.notNull(algorithm, "algorithm");
        Args.notNull(key, "key");
        Args.notNull(bytes, "bytes");

        SecretKey secretKey;
        switch (algorithm) {
            case DES:
                secretKey = SecretKeyFactory.getInstance(algorithm.value()).generateSecret(
                        new DESKeySpec(getPssKey(algorithm, keyAlgorithm, key)));
                break;
            case DESEDE:
                secretKey = SecretKeyFactory.getInstance(algorithm.value()).generateSecret(
                        new DESedeKeySpec(getPssKey(algorithm, keyAlgorithm, key)));
                break;
            case AES:
                secretKey = new SecretKeySpec(getPssKey(algorithm, keyAlgorithm, key),
                        algorithm.value());
                break;
            default:
                throw new IllegalStateException();
        }
        Cipher cipher = Cipher.getInstance(algorithm.value());
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(bytes);
    }

    private static byte[] getPssKey(CryptoAlgorithm algorithm, HashAlgorithm keyAlgorithm,
            byte[] key) throws NoSuchAlgorithmException {
        byte[] bytes = (keyAlgorithm != null) ? getHash(keyAlgorithm, key) : key;
        switch (algorithm) {
            case DES:
                return Arrays.copyOf(bytes, 8);
            case DESEDE:
                return Arrays.copyOf(bytes, 24);
            case AES:
                return Arrays.copyOf(bytes, 16);
            default:
                throw new IllegalStateException();
        }
    }

    public static byte[] getHash(HashAlgorithm algorithm, byte[] bytes)
            throws NoSuchAlgorithmException {
        Args.notNull(algorithm, "algorithm");
        Args.notNull(bytes, "bytes");

        byte[] ret;
        switch (algorithm) {
            case MD2:
            case MD5:
            case SHA:
            case SHA256:
            case SHA384:
            case SHA512:
                MessageDigest md = MessageDigest.getInstance(algorithm.value());
                md.update(bytes);
                ret = md.digest();
                break;
            default:
                throw new IllegalStateException();
        }
        return ret;
    }

    @Override
    public int hashCode() {
        if (HashCodeUtils.resolved()) {
            return HashCodeUtils.hashCodeAlt(null, this);
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (EqualsUtils.resolved()) {
            return EqualsUtils.equalsAlt(null, this, obj);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        if (ToStringUtils.resolved()) {
            return ToStringUtils.toStringAlt(this);
        }
        return super.toString();
    }
}
