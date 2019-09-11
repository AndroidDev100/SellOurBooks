package com.bylancer.classified.bylancerclassified.widgets.htmlcontent

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.text.Html.ImageGetter
import android.view.View
import android.widget.TextView
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL


class URLImageParser
/***
 * Construct the URLImageParser which will execute AsyncTask and refresh the container
 * @param co
 * @param context
 */
(private var container: TextView, private var context: Context) : ImageGetter {

    override fun getDrawable(source: String): Drawable {
        val urlDrawable = URLDrawable()

        // get the actual source
        val asyncTask = ImageGetterAsyncTask(urlDrawable, container)

        asyncTask.execute(source)

        // return reference to URLDrawable where I will change with actual image from
        // the src tag
        return urlDrawable
    }

    class ImageGetterAsyncTask(internal var urlDrawable: URLDrawable, var container: TextView) : AsyncTask<String, Void, Drawable>() {

        override fun doInBackground(vararg params: String): Drawable? {
            val source = params[0]
            return fetchDrawable(source)
        }

        override fun onPostExecute(result: Drawable) {
            // set the correct bound according to the result from HTTP call
            urlDrawable.setBounds(0, 0, 0 + result.intrinsicWidth, 0 + result.intrinsicHeight)

            // change the reference of the current drawable to the result
            // from the HTTP call
            urlDrawable.drawable = result
            // redraw the image by invalidating the container
            if (container != null) {
                val charSeq = container?.text
                container?.text = charSeq
            }
        }

        /***
         * Get the Drawable from URL
         * @param urlString
         * @return
         */
        private fun fetchDrawable(urlString: String): Drawable? {
            try {
                val `is` = fetch(urlString)
                val drawable = Drawable.createFromStream(`is`, "src")
                drawable.setBounds(0, 0, 0 + drawable.intrinsicWidth, 0 + drawable.intrinsicHeight)
                return drawable
            } catch (e: Exception) {
                return null
            }

        }

        @Throws(MalformedURLException::class, IOException::class)
        private fun fetch(urlString: String): InputStream {
            return URL(urlString).openConnection().getInputStream()
        }
    }
}