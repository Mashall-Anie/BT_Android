package vn.edu.tlu.b11

data class Message(
    val content: String,
    val isSent: Boolean,
    val senderAddress: String? = null
)
