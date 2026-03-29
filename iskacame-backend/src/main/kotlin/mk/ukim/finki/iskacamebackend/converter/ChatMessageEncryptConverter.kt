package mk.ukim.finki.iskacamebackend.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import mk.ukim.finki.iskacamebackend.service.intf.EncryptionService
import org.springframework.stereotype.Component
/**
 * Converter that encrypts chat messages before persisting
 * to the database and decrypts them when retrieved
 */
@Converter
@Component
class ChatMessageEncryptConverter(
    private val encryptionService: EncryptionService
) : AttributeConverter<String, String> {

    /**
     * Encrypts the chat message before storing it in the database.
     *
     * @param plainMessage the plain chat message from the entity
     * @return the encrypted chat message to store in the database
     */
    override fun convertToDatabaseColumn(plainMessage: String?): String? {
        return plainMessage?.let { encryptionService.encrypt(it) }
    }

    /**
     * Decrypts the chat message when reading it from the database.
     *
     * @param encryptedMessage the encrypted chat message from the database
     * @return the plain chat message for use in the entity
     */
    override fun convertToEntityAttribute(encryptedMessage: String?): String? {
        return encryptedMessage?.let { encryptionService.decrypt(it) }
    }
}