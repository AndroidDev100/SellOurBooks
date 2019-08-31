package com.bylancer.classified.bylancerclassified.chat

import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.utils.SessionState
import com.bylancer.classified.bylancerclassified.utils.Utility
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import com.bylancer.classified.bylancerclassified.webservices.chat.ChatMessageModel
import com.bylancer.classified.bylancerclassified.webservices.chat.ChatSentStatus
import kotlinx.android.synthetic.main.activity_chat.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : BylancerBuilderActivity(), View.OnClickListener, Callback<List<ChatMessageModel>> {
    var chatUserId = ""
    var isConnectionSuccess = true
    var isFirstTime = true
    var handler: Handler? = null

    override fun setLayoutView() = R.layout.activity_chat

    override fun initialize(savedInstanceState: Bundle?) {
        val bundle = intent.getBundleExtra(AppConstants.BUNDLE)
        handler = Handler()
        write_chat_edit_text.hint = LanguagePack.getString(getString(R.string.enter_message))

        if (bundle != null) {
            chat_title_text_view.text = bundle.getString(AppConstants.CHAT_TITLE, "")
            var chatUserName = bundle.getString(AppConstants.CHAT_USER_NAME, "")
            chatUserId = bundle.getString(AppConstants.CHAT_USER_ID, "")
            val chatUserImage = AppConstants.IMAGE_URL + bundle.getString(AppConstants.CHAT_USER_IMAGE, "")
            Glide.with(this).load(chatUserImage).apply(RequestOptions().circleCrop()).into(chat_user_image_view)

            val linearLayoutManager = LinearLayoutManager(this)
            linearLayoutManager.reverseLayout = true
            recycler_view_chat_message_list.layoutManager = linearLayoutManager
            recycler_view_chat_message_list.setHasFixedSize(false)
            getChatMessages()
        }
    }

    private fun getChatMessages() {
        if (isConnectionSuccess) {
            if (isFirstTime) {
                chat_sliding_progress_indicator.visibility = View.VISIBLE
            }
            RetrofitController.fetchChatMessages(SessionState.instance.userId,
                    chatUserId, "1", this)
        }
    }

    override fun onFailure(call: Call<List<ChatMessageModel>>?, t: Throwable?) {
        Utility.removeProgressBar(chat_sliding_progress_indicator)
        isConnectionSuccess = false
        if (isFirstTime) {
            isFirstTime = false
            Utility.showSnackBar(chat_parent_layout, LanguagePack.getString(getString(R.string.internet_issue)), this@ChatActivity)
        }
    }

    override fun onResponse(call: Call<List<ChatMessageModel>>?, response: Response<List<ChatMessageModel>>?) {
        Utility.removeProgressBar(chat_sliding_progress_indicator)
        if(response != null && response.isSuccessful && recycler_view_chat_message_list != null) {
            recycler_view_chat_message_list.adapter = ChatMessageAdapter(response.body())
            initializeContinuousMonitor()
        } else  {
            isConnectionSuccess = false
            if (isFirstTime) {
                Utility.showSnackBar(chat_parent_layout, LanguagePack.getString(getString(R.string.some_wrong)), this@ChatActivity)
            }
        }

        isFirstTime = false
    }

    private fun sendChatMessage(message: String) {
        write_chat_edit_text.setText("")
        if (!chatUserId.isNullOrEmpty()) {
            RetrofitController.sendChatMessage(SessionState.instance.userId, chatUserId, message, object: Callback<ChatSentStatus>{
                override fun onFailure(call: Call<ChatSentStatus>?, t: Throwable?) {
                    Utility.showSnackBar(chat_parent_layout, LanguagePack.getString(getString(R.string.internet_issue)), this@ChatActivity)
                }

                override fun onResponse(call: Call<ChatSentStatus>?, response: Response<ChatSentStatus>?) {
                    if (response != null  && response.isSuccessful) {
                        getChatMessages()
                    } else {
                        Utility.showSnackBar(chat_parent_layout, LanguagePack.getString(getString(R.string.some_wrong)), this@ChatActivity)
                    }
                }

            })
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.chat_back_image_view -> {
                handler = null
                onBackPressed()
            }
            R.id.chat_box_send -> {
                Utility.hideKeyboard(this)
                if (!write_chat_edit_text.text.isNullOrEmpty()) {
                    sendChatMessage(write_chat_edit_text.text.toString())
                }
            }
            R.id.chat_box_image_attachment -> Utility.hideKeyboard(this)
        }
    }

    private fun initializeContinuousMonitor() {
        if (handler != null) {
            handler?.postDelayed({
                getChatMessages()
            }, 5000)
        }
    }
}
