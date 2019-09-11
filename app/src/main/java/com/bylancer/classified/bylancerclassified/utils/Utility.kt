package com.bylancer.classified.bylancerclassified.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.telephony.TelephonyManager
import android.text.Html
import android.text.TextUtils
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.agrawalsuneet.dotsloader.loaders.SlidingLoader
import com.asksira.bsimagepicker.BSImagePicker
import com.asksira.bsimagepicker.Utils
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import com.bylancer.classified.bylancerclassified.webservices.notificationmessage.AddTokenStatus
import com.gmail.samehadar.iosdialog.IOSDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern


/**
 * Created by Ani on 3/20/18.
 */
class Utility {
    companion object {
        //show progressView
        fun showProgressView(context: Context, message:String) : IOSDialog {
            val dialog = IOSDialog.Builder(context)
                    .setOnCancelListener {

                    }
                    .setDimAmount(3f)
                    .setSpinnerColorRes(R.color.white_color_text)
                    .setMessageColorRes(R.color.white_color_text)
                    // .setTitle(R.string.standard_title)
                    .setTitleColorRes(R.color.white_color_text)
                    .setMessageContent(LanguagePack.getString(message))
                    .setCancelable(false)
                    .setMessageContentGravity(Gravity.END)
                    .build()
            return dialog
        }

        fun showSnackBar(view: View, msg:String, context: Context) {
            if (view != null && context != null) {
                val snackbar = Snackbar.make(view, LanguagePack.getString(msg), Snackbar.LENGTH_LONG)
                val sbView = snackbar.getView()
                val textView = sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                textView?.typeface = ResourcesCompat.getFont(context, R.font.roboto_bold)
                textView?.gravity = Gravity.CENTER
                textView?.textSize = 18.0f
                textView?.setTextColor(context.resources.getColor(R.color.white_color_text))
                snackbar.show()
            }
        }

        fun slideActivityLeftToRight(activity: Activity) {
            activity.overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end)
        }

        fun slideActivityRightToLeft(activity: Activity) {
            activity.overridePendingTransition(R.anim.right_to_left_start, R.anim.left_to_right_end)
        }

        fun slideActivityBottomToTop(activity: Activity) {
            activity.overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
        }

        fun slideActivityTopToBottom(activity: Activity) {
            activity.overridePendingTransition(R.anim.slide_down, R.anim.slide_down_out)
        }

        @SuppressLint("NewApi")
        fun getPath(context: Context, uri: Uri): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKat) {
                if (DocumentsContract.isDocumentUri(context, uri)) {
                    // ExternalStorageProvider
                    if (isExternalStorageDocument(uri)) {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val type = split[0]

                        if ("primary".equals(type, ignoreCase = true)) {
                            return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                        }

                        // TODO handle non-primary volumes
                    } else if (isDownloadsDocument(uri)) {

                        val id = DocumentsContract.getDocumentId(uri)
                        val contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!)

                        return getDataColumn(context, contentUri, null, null)
                    } else if (isMediaDocument(uri)) {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val type = split[0]

                        var contentUri: Uri? = null
                        if ("image" == type) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        } else if ("video" == type) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        } else if ("audio" == type) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }

                        val selection = "_id=?"
                        val selectionArgs = arrayOf(split[1])

                        return getDataColumn(context, contentUri, selection, selectionArgs)
                    }// MediaProvider
                    // DownloadsProvider
                } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                    return getDataColumn(context, uri, null, null)
                } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                    return uri.path
                }// File

            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                return getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }// File
            // MediaStore (and general)

            return null
        }

        /**
         * Get the value of the data column for this Uri. This is useful for
         * MediaStore Uris, and other file-based ContentProviders.
         *
         * @param context The context.
         * @param uri The Uri to query.
         * @param selection (Optional) Filter used in the query.
         * @param selectionArgs (Optional) Selection arguments used in the query.
         * @return The value of the _data column, which is typically a file path.
         */
        fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                          selectionArgs: Array<String>?): String? {

            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)

            try {
                cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
                }
            } finally {
                if (cursor != null)
                    cursor.close()
            }
            return null
        }


        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        /**
         * Check text is empty or not
         */
        fun hasText(editText: EditText) : Boolean {
            if (editText == null) {
                return false
            } else if (TextUtils.isEmpty(editText.text)) {
                return false
            }

            return true
        }

        /**
         * show sliding progress indicator
         */
        fun showSlidingProgressIndicator(context: Context) : SlidingLoader {
            return SlidingLoader(context, 40, 10,
                    ContextCompat.getColor(context, R.color.denied_red),
                    ContextCompat.getColor(context, R.color.denied_red),
                    ContextCompat.getColor(context, R.color.denied_red)).apply {
                animDuration = 1000
                distanceToMove = 12
            }
        }

        /**
         * Hide keyboard
         */
        fun hideKeyboard(activity: Activity) {
            if(activity != null) {
                val imm: InputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                //Find the currently focused view, so we can grab the correct window token from it.
                var view = activity.getCurrentFocus()
                //If no view currently has focus, create a new one, just so we can grab a window token from it
                if (view == null) {
                    view = View(activity)
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
            }
        }

        /**
         * Hide keyboard
         */
        fun hideKeyboardFromDialogs(activity: Activity) {
            if (activity != null) {
                val imm: InputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                //Find the currently focused view, so we can grab the correct window token from it.
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        }

        /**
         * Hide keyboard
         */
        fun dismissKeyboardFromSettings(activity: Activity, view: View) {
            val imm: InputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        fun isValidEmail(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun isValidAlphaNumeric(username: String): Boolean {
            val regex = "^[a-zA-Z0-9]+$"
            val a = Pattern.compile(regex).matcher(username).matches()
            return a
        }

        fun animateProgressView(context: Context, progressView: ImageView) : Animation {
            val animUpDown = AnimationUtils.loadAnimation(context,
                    R.anim.up_down)
            animUpDown?.setRepeatCount(Animation.INFINITE)
            animUpDown?.setRepeatMode(Animation.INFINITE)
            animUpDown?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    if(animUpDown != null)
                        progressView.startAnimation(animUpDown)
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            progressView.startAnimation(animUpDown)

            return animUpDown
        }

        fun getCircularProgressDrawable(context: Context): CircularProgressDrawable {
            val circularProgressDrawable = CircularProgressDrawable(context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()
            return circularProgressDrawable
        }

        fun shareProduct(url: String, appName: String, context: Context) {
            val shareBody = "Have a look of this product on " + appName +"\n $url"
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.setType("text/plain")
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, appName)
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
            context.startActivity(Intent.createChooser(sharingIntent, context.getResources().getString(R.string.share_using)))
        }

        /**
         * Progress Dialog
         * @param context
         * @param message
         * @return
         */
        fun getProgressDialog(context: Context, message: String): IOSDialog {
            return IOSDialog.Builder(context)
                    .setTitle("")
                    .setSpinnerColorRes(R.color.white_color_text)
                    .setMessageColorRes(R.color.white_color_text)
                    .setMessageContent(message)
                    .setCancelable(true)
                    .setMessageContentGravity(Gravity.END)
                    .build()
        }

        fun checkLocationAndPhonePermission(MY_PERMISSIONS_REQUEST_LOCATION : Int, activity: Activity): Boolean {
            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.CALL_PHONE) ) {
                    ActivityCompat.requestPermissions(activity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE),
                            MY_PERMISSIONS_REQUEST_LOCATION)
                } else {
                    ActivityCompat.requestPermissions(activity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE),
                            MY_PERMISSIONS_REQUEST_LOCATION)
                }
                return false
            } else {
                return true
            }
        }

        fun decodeUnicode(theString: String): String {
            if (theString != null) {
                return Html.fromHtml(theString).toString();
            }
            return ""
        }

        fun removeProgressBar(view: View) {
            if(view != null) view.visibility = View.GONE
        }

        fun valueInDp(sizeInDp : Int, context: Context): Int {
            val scale = context.resources.getDisplayMetrics().density
            return ((sizeInDp*scale + 0.5f).toInt())
        }

        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager: ConnectivityManager = context.getSystemService (Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

    }
}

fun Context.getDeviceId(): String {
    return Settings.Secure.getString(getContentResolver(),
            Settings.Secure.ANDROID_ID)
}

fun Context.sendTokenToServer() {
    RetrofitController.addDeviceToken(SessionState.instance.userId, SessionState.instance.displayName,
            getDeviceId(), SessionState.instance.token, object : Callback<AddTokenStatus> {
        override fun onFailure(call: Call<AddTokenStatus>?, t: Throwable?) {
            if (Utility.isNetworkAvailable(this@sendTokenToServer)) {
                sendTokenToServer()
            }
        }

        override fun onResponse(call: Call<AddTokenStatus>?, response: Response<AddTokenStatus>?) {
            if (response != null && response.isSuccessful) {
                SessionState.instance.saveValuesToPreferences(this@sendTokenToServer, AppConstants.Companion.PREFERENCES.DEVICE_ID.toString(),
                        SessionState.instance.token)
            } else if (Utility.isNetworkAvailable(this@sendTokenToServer)) {
                sendTokenToServer()
            }
        }
    })
}

fun RecyclerView.initScrollListener(onProductLoading: LazyProductLoading) {
    addOnScrollListener( object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx:Int, dy:Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0) { // dy to check it is scrolling down
                var gridLayoutManager = recyclerView.getLayoutManager() as GridLayoutManager
                if (onProductLoading != null && gridLayoutManager != null) {
                    onProductLoading.onProductLoadRequired(gridLayoutManager.findLastCompletelyVisibleItemPosition())
                }
            }
        }
    })
}

fun NestedScrollView.initNestedScrollListener(onProductLoading: LazyProductLoading) {
    setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
        if (scrollY > oldScrollY) {

        }
        if (scrollY < oldScrollY) {
            //Log.i("AA", "Scroll UP");
        }

        if (scrollY == 0) {
            //Log.i("Aa", "TOP SCROLL");
        }

        if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() / 3)  - (v.getMeasuredHeight() / 3)) &&
                scrollY > oldScrollY) {
            onProductLoading.onProductLoadRequired(Integer.MAX_VALUE)
        }
    })
}

fun Context.getCurrentCountry(): String {
    if (AppConstants.YES.equals(SessionState.instance.detectLiveLocation)) {
        try {
            var tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var simCountry = tm.simCountryIso
            if (simCountry != null && simCountry.length == 2) { // SIM country code is available
                return simCountry.toLowerCase()
            }
            else if (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                var networkCountry = tm.networkCountryIso
                if (networkCountry != null && networkCountry.length == 2) { // network country code is available
                    return networkCountry.toLowerCase()
                }
            }
        }
        catch (e: Exception) { }
        return "in";
    } else  {
        return SessionState.instance.defaultCountry.toLowerCase()
    }
}

fun getSingleImagePickerDialog(tag: String) : BSImagePicker {
    return BSImagePicker.Builder("com.bylancer.classified.bylancerclassified.fileprovider")
            .setMaximumDisplayingImages(Integer.MAX_VALUE) //Default: Integer.MAX_VALUE. Don't worry about performance :)
            .setSpanCount(3) //Default: 3. This is the number of columns
            .setGridSpacing(Utils.dp2px(2)) //Default: 2dp. Remember to pass in a value in pixel.
            .setPeekHeight(Utils.dp2px(360)) //Default: 360dp. This is the initial height of the dialog.
            //.hideCameraTile() //Default: show. Set this if you don't want user to take photo.
            .hideGalleryTile() //Default: show. Set this if you don't want to further let user select from a gallery app. In such case, I suggest you to set maximum displaying images to Integer.MAX_VALUE.
            .setTag(tag) //Default: null. Set this if you need to identify which picker is calling back your fragment / activity.
            //.dismissOnSelect(true) //Default: true. Set this if you do not want the picker to dismiss right after selection. But then you will have to dismiss by yourself.
            .build()
}

fun Context.isLocationEnabled() : Boolean {
     var  locationManager: LocationManager? = null;
     var gps_enabled = false
     var network_enabled = false

     if(locationManager ==null)
         locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
     try{
         gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
     }catch(ex: Exception){
         //do nothing...
     }

     try{
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
     }catch(ex: Exception){
         //do nothing...
     }

     return gps_enabled || network_enabled;
}

fun checkIfNumber(price : String) : Boolean {
    val pattern : Pattern = Pattern.compile("\\d+(?:\\.\\d+)?")
    return price.matches(pattern.toRegex())
}

fun Context.isOver600dp(): Boolean {
    val resources = this.resources
    val displayMetrics = resources.displayMetrics
    return displayMetrics.widthPixels / displayMetrics.density >= 600
}

fun Context.getDefaultFont() = ResourcesCompat.getFont(this, R.font.roboto_regular)

