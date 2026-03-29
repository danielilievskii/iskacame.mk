package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.service.intf.EncryptionService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Implementation of the EncryptionService.
 */
@Service
class EncryptionServiceImpl(
    @Value("\${encryption.algorithm}") private val algorithm: String,
    @Value("\${encryption.secret.key}") private val secretKey: String
) : EncryptionService {

    private val keySpec = SecretKeySpec(secretKey.toByteArray(), algorithm)

    override fun encrypt(value: String): String {

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)

        val plainBytes = value.toByteArray()
        val encryptedBytes = cipher.doFinal(plainBytes)

        return Base64
            .getEncoder()
            .encodeToString(encryptedBytes)
    }

    override fun decrypt(encryptedValue: String): String {

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, keySpec)

        val encryptedBytes = Base64
            .getDecoder()
            .decode(encryptedValue)

        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes)
    }
}