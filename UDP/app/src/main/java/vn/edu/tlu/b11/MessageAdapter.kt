package vn.edu.tlu.b11

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.edu.tlu.b11.databinding.ItemMessageReceivedBinding
import vn.edu.tlu.b11.databinding.ItemMessageSentBinding

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 0
        private const val VIEW_TYPE_RECEIVED = 1
    }

    class SentMessageViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessage.text = message.content
            binding.tvSender.visibility = View.GONE
        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessage.text = message.content
            binding.tvSender.text = "Từ: ${message.senderAddress}"
            binding.tvSender.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SentMessageViewHolder(binding)
            }
            else -> {
                val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ReceivedMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSent) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }
}
