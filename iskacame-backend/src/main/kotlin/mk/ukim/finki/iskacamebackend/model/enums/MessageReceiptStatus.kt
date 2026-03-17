package mk.ukim.finki.iskacamebackend.model.enums


enum class MessageReceiptStatus {

    /** Message stored on server, not yet confirmed received by recipient's client */
    SENT,

    /** Recipient's client has received the message */
    DELIVERED,

    /** Recipient has opened/read the message */
    SEEN
}