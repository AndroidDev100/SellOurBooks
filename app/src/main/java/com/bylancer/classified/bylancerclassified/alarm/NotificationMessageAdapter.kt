package com.bylancer.classified.bylancerclassified.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.chat.ChatActivity
import com.bylancer.classified.bylancerclassified.dashboard.DashboardProductDetailActivity
import com.bylancer.classified.bylancerclassified.fragments.BylancerBuilderFragment
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.SessionState
import com.bylancer.classified.bylancerclassified.webservices.notificationmessage.NotificationDataModel
import kotlinx.android.synthetic.main.notification_message_adapter.view.*

class NotificationMessageAdapter(private val mBaseFragment: BylancerBuilderFragment, private val mNotificationMessageListModel: List<NotificationDataModel>): RecyclerView.Adapter<NotificationMessageAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_message_adapter, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (mNotificationMessageListModel != null) return mNotificationMessageListModel.size else return 0
    }

    override fun onBindViewHolder(chatViewHolder: ChatViewHolder, position: Int) {
        val notificationMessageModel = mNotificationMessageListModel.get(position)
        chatViewHolder?.notificationMessageDetailIcon?.visibility = View.VISIBLE
        chatViewHolder?.notificationMessage!!.text = notificationMessageModel.message
        chatViewHolder?.notificationMessageTitleName?.text = notificationMessageModel.productTitle
        if (notificationMessageModel.productTitle.isNullOrEmpty()) {
            chatViewHolder.notificationMessageTitleName?.visibility = View.GONE
        } else {
            chatViewHolder.notificationMessageTitleName?.visibility = View.VISIBLE
        }

        when (notificationMessageModel?.type) {
            AppConstants.AD_APPROVE -> {
                chatViewHolder?.notificationMessageTitleName?.setTextColor(chatViewHolder?.notificationMessageTitleName?.context?.resources!!.getColor(R.color.dark_green))
                chatViewHolder?.notificationMessageType?.setImageResource(R.drawable.ic_ad_aprove)
                setColorFilter(chatViewHolder!!.notificationMessageType, R.color.dark_green)
            }
            AppConstants.AD_DELETE -> {
                chatViewHolder?.notificationMessageDetailIcon?.visibility = View.GONE
                chatViewHolder?.notificationMessageTitleName?.setTextColor(chatViewHolder?.notificationMessageTitleName?.context?.resources!!.getColor(R.color.denied_red))
                chatViewHolder?.notificationMessageType?.setImageResource(R.drawable.ic_ad_deleted)
                setColorFilter(chatViewHolder!!.notificationMessageType, R.color.denied_red)
            }
            else -> {
                chatViewHolder?.notificationMessageTitleName?.setTextColor(chatViewHolder?.notificationMessageTitleName?.context?.resources!!.getColor(R.color.yellow_dark))
                chatViewHolder?.notificationMessageType?.setImageResource(R.drawable.ic_new_message)
                setColorFilter(chatViewHolder!!.notificationMessageType, R.color.yellow_dark)
            }
        }

        chatViewHolder?.itemView!!.setOnClickListener() {
            when (notificationMessageModel?.type) {
                AppConstants.AD_APPROVE -> {
                    onItemClicked(notificationMessageModel?.type, notificationMessageModel?.productId)
                }
                AppConstants.AD_DELETE ->{}
                else -> {
                    onItemClicked(notificationMessageModel?.type, notificationMessageModel?.senderId,
                            notificationMessageModel?.senderName ?: " ")
                }
            }
        }
    }

    private fun onItemClicked(type: String?, id: String?, senderName: String = "") {
        if (!type.isNullOrEmpty() && !id.isNullOrEmpty()) {
            when (type) {
                AppConstants.AD_APPROVE -> {
                    val bundle = Bundle()
                    bundle.putString(AppConstants.PRODUCT_ID, id)
                    bundle.putString(AppConstants.PRODUCT_NAME, "")
                    bundle.putString(AppConstants.PRODUCT_OWNER_NAME, SessionState.instance.userName)
                    mBaseFragment?.startActivity(DashboardProductDetailActivity::class.java, bundle)
                }
                else -> {
                    val bundle = Bundle()
                    bundle.putString(AppConstants.CHAT_TITLE, senderName)
                    bundle.putString(AppConstants.CHAT_USER_NAME, senderName)
                    bundle.putString(AppConstants.CHAT_USER_IMAGE, "")
                    bundle.putString(AppConstants.CHAT_USER_ID, id)
                    mBaseFragment?.startActivity(ChatActivity::class.java, false, bundle)
                }
            }
        }
    }

    private fun setColorFilter(imageView: AppCompatImageView, colorResId: Int) {
        imageView.setColorFilter(ContextCompat.getColor(imageView!!.context, colorResId), android.graphics.PorterDuff.Mode.SRC_ATOP);
    }

    class ChatViewHolder(view: View): RecyclerView.ViewHolder(view) {
        internal var notificationMessage = view.notification_message_text_view
        internal var notificationMessageTitleName = view.notification_title_name
        internal var notificationMessageType = view.notification_type_icon
        internal var notificationMessageDetailIcon = view.notification_details_icon
    }
}