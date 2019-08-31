package com.bylancer.classified.bylancerclassified.chat

import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.SessionState
import com.bylancer.classified.bylancerclassified.webservices.chat.ChatMessageModel

class ChatMessageAdapter(private val mChatMessageListModel: List<ChatMessageModel>): RecyclerView.Adapter<ChatMessageAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_message_adapter, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (mChatMessageListModel != null) return mChatMessageListModel.size else return 0
    }

    override fun onBindViewHolder(chatViewHolder: ChatViewHolder, position: Int) {
        val chatModel = mChatMessageListModel.get(position)
        if (chatModel?.senderId != null && !chatModel.senderId.equals(SessionState.instance.userId)) {
            chatViewHolder.clientUserChatParentLayout.visibility = View.GONE
            chatViewHolder.currentUserChatParentLayout.visibility = View.VISIBLE
            if (AppConstants.ASSET_TYPE_TEXT.equals(chatModel.mtype)) {
                chatViewHolder.currentUserChatImageLayout.visibility = View.GONE
                chatViewHolder.currentUserChatTextLayout.visibility = View.VISIBLE
                chatViewHolder.currentUserChatTextView.text = chatModel.message
                chatViewHolder.currentUserChatTimeTextView.text = chatModel.time
            } else {
                chatViewHolder.currentUserChatTextLayout.visibility = View.GONE
                chatViewHolder.currentUserChatImageLayout.visibility = View.VISIBLE
                Glide.with(chatViewHolder.currentUserChatImageView.context).load(AppConstants.IMAGE_URL + chatModel.message).apply(RequestOptions().circleCrop()).into(chatViewHolder.currentUserChatImageView)
                chatViewHolder.currentUserChatImageTimeTextView.text = chatModel.time
            }
        } else {
            chatViewHolder.clientUserChatParentLayout.visibility = View.VISIBLE
            chatViewHolder.currentUserChatParentLayout.visibility = View.GONE
            if (AppConstants.ASSET_TYPE_TEXT.equals(chatModel.mtype)) {
                chatViewHolder.clientUserChatImageLayout.visibility = View.GONE
                chatViewHolder.clientUserChatTextLayout.visibility = View.VISIBLE
                chatViewHolder.clientUserChatTextView.text = chatModel.message
                chatViewHolder.clientUserChatTimeTextView.text = chatModel.time
            } else {
                chatViewHolder.clientUserChatTextLayout.visibility = View.GONE
                chatViewHolder.clientUserChatImageLayout.visibility = View.VISIBLE
                Glide.with(chatViewHolder.clientUserChatImageView.context).load(AppConstants.IMAGE_URL + chatModel.message).apply(RequestOptions().centerCrop()).into(chatViewHolder.clientUserChatImageView)
                chatViewHolder.clientUserChatImageTimeTextView.text = chatModel.time
            }
        }
    }

    class ChatViewHolder(view: View): RecyclerView.ViewHolder(view) {
        internal var currentUserChatParentLayout = view.findViewById(R.id.current_user_chat_parent_layout) as FrameLayout
        internal var clientUserChatParentLayout = view.findViewById(R.id.client_user_chat_parent_layout) as FrameLayout
        internal var currentUserChatTextLayout = view.findViewById(R.id.current_user_chat_text_layout) as LinearLayout
        internal var clientUserChatTextLayout = view.findViewById(R.id.client_user_chat_text_layout) as LinearLayout
        internal var clientUserChatTextView = view.findViewById(R.id.client_user_chat_text) as AppCompatTextView
        internal var clientUserChatTimeTextView = view.findViewById(R.id.client_user_chat_text_time) as AppCompatTextView
        internal var currentUserChatTextView = view.findViewById(R.id.current_user_chat_text) as AppCompatTextView
        internal var currentUserChatTimeTextView = view.findViewById(R.id.current_user_chat_text_time) as AppCompatTextView
        internal var currentUserChatImageLayout = view.findViewById(R.id.current_user_chat_image_layout) as LinearLayout
        internal var currentUserChatImageView = view.findViewById(R.id.current_user_chat_image) as AppCompatImageView
        internal var currentUserChatImageTimeTextView = view.findViewById(R.id.current_user_chat_image_time) as AppCompatTextView
        internal var clientUserChatImageLayout = view.findViewById(R.id.client_user_chat_image_layout) as LinearLayout
        internal var clientUserChatImageView = view.findViewById(R.id.client_user_chat_image) as AppCompatImageView
        internal var clientUserChatImageTimeTextView = view.findViewById(R.id.client_user_chat_image_time) as AppCompatTextView
    }
}