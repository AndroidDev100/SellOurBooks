package com.bylancer.classified.bylancerclassified.webservices

import com.bylancer.classified.bylancerclassified.dashboard.DashboardDetailModel
import com.bylancer.classified.bylancerclassified.appconfig.AppConfigModel
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.webservices.chat.ChatMessageModel
import com.bylancer.classified.bylancerclassified.webservices.chat.ChatSentStatus
import com.bylancer.classified.bylancerclassified.webservices.chat.GroupChatModel
import com.bylancer.classified.bylancerclassified.webservices.languagepack.LanguagePackModel
import com.bylancer.classified.bylancerclassified.webservices.login.UserLoginStatus
import com.bylancer.classified.bylancerclassified.webservices.makeanoffer.MakeAnOfferStatus
import com.bylancer.classified.bylancerclassified.webservices.notificationmessage.AddTokenStatus
import com.bylancer.classified.bylancerclassified.webservices.notificationmessage.NotificationDataModel
import com.bylancer.classified.bylancerclassified.webservices.productlist.ProductsData
import com.bylancer.classified.bylancerclassified.webservices.registration.UserForgetPasswordStatus
import com.bylancer.classified.bylancerclassified.webservices.registration.UserRegistrationStatus
import com.bylancer.classified.bylancerclassified.webservices.settings.CityListModel
import com.bylancer.classified.bylancerclassified.webservices.settings.CountryListModel
import com.bylancer.classified.bylancerclassified.webservices.settings.ProductUploadProductModel
import com.bylancer.classified.bylancerclassified.webservices.settings.StateListModel
import com.bylancer.classified.bylancerclassified.webservices.uploadproduct.PostedProductResponseModel
import com.bylancer.classified.bylancerclassified.webservices.uploadproduct.UploadProductModel
import retrofit2.Call
import retrofit2.http.*
import okhttp3.RequestBody
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.POST
import retrofit2.http.Multipart



interface WebServiceApiInterface {

    @POST(AppConstants.REGISTER_URL)
    fun registerUser(@Query("name") name: String, @Query("email") email: String,
                     @Query("username") username: String, @Query("password") password: String, @Query("fb_login") fbLogin: String): Call<UserRegistrationStatus>

    @GET(AppConstants.LOGIN_URL)
    fun loginUserUsingUsername(@Query("username") name: String, @Query("password") email: String): Call<UserLoginStatus>

    @GET(AppConstants.LOGIN_URL)
    fun loginUserUsingEmail(@Query("email") name: String, @Query("password") email: String): Call<UserLoginStatus>

    @GET(AppConstants.FORGOT_PASSWORD_URL)
    fun forgetPassword(@Query("email") name: String): Call<UserForgetPasswordStatus>

    @GET(AppConstants.PRODUCT_LIST_URL)
    fun fetchProducts(@Query("status") status: String, @Query("country_code") countryCode: String,
                      @Query("page") page:String, @Query("limit") limit:String): Call<List<ProductsData>>

    @GET(AppConstants.FEATURED_URGENT_LIST_URL)
    fun fetchFeaturedAndUrgentProducts(@Query("status") status: String, @Query("country_code") countryCode: String,
                      @Query("page") page:String, @Query("limit") limit:String): Call<List<ProductsData>>

    @GET(AppConstants.PRODUCT_LIST_URL)
    fun fetchProductsForUser(@Query("page") page:String, @Query("limit") limit:String, @Query("user_id") user_id:String): Call<List<ProductsData>>

    @GET(AppConstants.SEARCH_LIST_URL)
    fun fetchProductsByCategory(@Query("status") status: String, @Query("country_code") countryCode: String,
                                @Query("page") page:String, @Query("limit") limit:String, @Query("category_id") categoryId: String, @Query("subcategory_id") subcategoryId: String, @Query("keywords") keywords: String, @Query("additionalinfo") additionalInfo: String): Call<List<ProductsData>>

    @GET(AppConstants.PRODUCT_DETAIL_URL)
    fun fetchProductsDetails(@Query("item_id") itemID: String): Call<DashboardDetailModel>

    @GET(AppConstants.COUNTRY_DETAIL_URL)
    fun fetchCountryDetails(): Call<List<CountryListModel>>

    @GET(AppConstants.STATE_DETAIL_URL)
    fun fetchStateDetailsByCountry(@Query("country_code") countryId: String): Call<List<StateListModel>>

    @GET(AppConstants.CITY_DETAIL_URL)
    fun fetchCityDetailsByState(@Query("state_code") stateId: String): Call<List<CityListModel>>

    @GET(AppConstants.APP_CONFIG_URL)
    fun fetchAppConfiguration(@Query("lang_code") languageCode:String): Call<AppConfigModel>

    @GET(AppConstants.FETCH_CHAT_URL)
    fun fetchChatMessage(@Query("ses_userid") myUserId:String, @Query("client_id") clientId:String, @Query("page") pageNumber:String): Call<List<ChatMessageModel>>

    @GET(AppConstants.FETCH_GROUP_CHAT_URL)
    fun fetchGroupChatMessage(@Query("session_user_id") sessionUserId:String): Call<List<GroupChatModel>>

    @GET(AppConstants.FETCH_LANGUAGE_PACK_URL)
    fun fetchLanguagePack(): Call<List<LanguagePackModel>>

    @GET(AppConstants.MAKE_AN_OFFER)
    fun makeAnOffer(@Query("OwnerId") ownerId:String, @Query("message") message:String, @Query("SenderId") senderId:String, @Query("SenderName") senderName:String, @Query("OwnerName") ownerName:String,
                    @Query("email") email:String,  @Query("subject") subject:String,  @Query("productTitle") productName:String, @Query("type") type:String, @Query("productId") productId:String): Call<MakeAnOfferStatus>

    @GET(AppConstants.SEND_CHAT_URL)
    fun sendChatMessage(@Query("from_id") fromId:String, @Query("to_id") toId:String, @Query("message") message:String): Call<ChatSentStatus>

    @GET(AppConstants.GET_NOTIFICATION_MESSAGE_URL)
    fun getNotificationMessage(@Query("user_id") user_id:String): Call<List<NotificationDataModel>>

    @GET(AppConstants.ADD_FIREBASE_DEVICE_TOKEN_URL)
    fun addFirebaseDeviceToken(@Query("user_id") userId:String, @Query("device_id") deviceId:String
        , @Query("name") name:String, @Query("token") token:String): Call<AddTokenStatus>

    @Multipart
    @POST(AppConstants.UPLOAD_PROFILE_PIC_URL)
    fun updateProfilePic(@Query("user_id") id: String, @Part image: MultipartBody.Part): Call<ProductUploadProductModel>

    @Multipart
    @POST(AppConstants.UPLOAD_PRODUCT_PIC_URL)
    fun updateUserPostedProductPic(@Part image: MultipartBody.Part): Call<UploadProductModel>

    @POST(AppConstants.UPLOAD_PRODUCT_SAVE_POST_URL)
    fun postUserProduct(@Query("user_id") userId:String,
                        @Query("title") title:String,
                        @Query("category_id") categoryId:String,
                        @Query("subcategory_id") subcategoryId:String,
                        @Query("country_code") countryCode:String,
                        @Query("state") state:String,
                        @Query("city") city:String,
                        @Query("description") description:String,
                        @Query("location") location:String,
                        @Query("hide_phone") hidePhone:String,
                        @Query("negotiable") negotiable:String,
                        @Query("price") price:String,
                        @Query("phone") phone:String,
                        @Query("latitude") latitude:String,
                        @Query("longitude") longitude:String,
                        @Query("item_screen") itemScreen:String,
                        @Query("additionalinfo") additionalinfo:String): Call<PostedProductResponseModel>

}