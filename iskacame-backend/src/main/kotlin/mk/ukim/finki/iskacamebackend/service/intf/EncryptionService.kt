package mk.ukim.finki.iskacamebackend.service.intf

/**
 * Service responsible for encrypting and decrypting sensitive data.
 *
 * Implementations should provide a secure algorithm for reversible encryption,
 * suitable for storing secrets like API keys or tokens.
 */
interface EncryptionService {

    /**
     * Encrypts the given plain text value.
     *
     * @param value the plain text to encrypt
     * @return the encrypted string, suitable for storing in a database or file
     */
    fun encrypt(value: String): String

    /**
     * Decrypts the given encrypted value.
     *
     * @param encryptedValue the encrypted string previously produced by [encrypt]
     * @return the original plain text value
     */
    fun decrypt(encryptedValue: String): String
}