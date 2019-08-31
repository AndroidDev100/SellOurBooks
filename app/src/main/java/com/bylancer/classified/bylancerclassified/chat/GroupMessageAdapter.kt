package com.bylancer.classified.bylancerclassified.chat

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.dashboard.DashboardActivity
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.ColorCircleDrawable
import com.bylancer.classified.bylancerclassified.webservices.chat.GroupChatModel
import kotlinx.android.synthetic.main.group_chat_adapter.view.*

class GroupMessageAdapter(val mChatMessageListModel: List<GroupChatModel>, val parentActivity: DashboardActivity): RecyclerView.Adapter<GroupMessageAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_chat_adapter, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (mChatMessageListModel != null) return mChatMessageListModel.size else return 0
    }

    override fun onBindViewHolder(chatViewHolder: ChatViewHolder, position: Int) {
        val groupChatModel = mChatMessageListModel.get(position)
        chatViewHolder.clientUserChatCounterTextView.background = ColorCircleDrawable(parentActivity.resources.getColor(R.color.android_def_cursor_color))
        chatViewHolder.clientUserChatCounterTextView.text = groupChatModel.unseen.toString()
        chatViewHolder.clientUserChatOnlineStatusTextView.text = groupChatModel.status.toString()
        if (AppConstants.ZERO.equals(groupChatModel.unseen.toString())) {
            chatViewHolder.clientUserChatCounterTextView.visibility = View.GONE
        } else  {
            chatViewHolder.clientUserChatCounterTextView.visibility = View.VISIBLE
        }
        chatViewHolder.clientUserNameTextView.text = groupChatModel.fromFullname
        Glide.with(chatViewHolder.currentUserChatImageView.context).load(AppConstants.IMAGE_URL + groupChatModel!!.fromUserImage).apply(RequestOptions().circleCrop()).into(chatViewHolder.currentUserChatImageView)
        chatViewHolder.groupChatCardView.setOnClickListener() {
            val bundle = Bundle()
            bundle.putString(AppConstants.CHAT_TITLE, groupChatModel!!.fromFullname)
            bundle.putString(AppConstants.CHAT_USER_NAME, groupChatModel!!.fromUsername)
            bundle.putString(AppConstants.CHAT_USER_IMAGE, groupChatModel!!.fromUserImage)
            bundle.putString(AppConstants.CHAT_USER_ID, groupChatModel!!.fromUserId)
            parentActivity.startActivity(ChatActivity::class.java, false, bundle)
        }
    }

    class ChatViewHolder(view: View): RecyclerView.ViewHolder(view) {
        internal var clientUserNameTextView = view.group_chat_user_name_text_view
        internal var clientUserChatCounterTextView = view.group_chat_counter_text_view
        internal var clientUserChatOnlineStatusTextView = view.group_chat_user_online_status_text_view
        internal var currentUserChatImageView = view.group_chat_user_image_view
        internal var groupChatCardView = view.group_chat_card_view
    }
}