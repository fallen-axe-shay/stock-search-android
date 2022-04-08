package com.jerryallanakshay.stocks

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class NewsAdapter(private val dataSet: ArrayList<NewsData>?, private val context: Context) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.news_title)
        val time: TextView = view.findViewById(R.id.news_time)
        val source: TextView = view.findViewById(R.id.news_source)
        val largeImage: ImageView = view.findViewById(R.id.image_main_view)
        val smallImage: ImageView = view.findViewById(R.id.image_side_view)
        val largeImageCard: CardView = view.findViewById(R.id.image_main_view_card)
        val smallImageCard: CardView = view.findViewById(R.id.image_side_view_card)
        val newsCard: CardView = view.findViewById(R.id.news_card)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.news_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.largeImageCard.visibility = View.GONE
        viewHolder.smallImageCard.visibility = View.GONE

        when (dataSet?.get(position)?.type) {
            0 -> {
                viewHolder.largeImageCard.visibility = View.VISIBLE
                Glide.with(context)
                    .load(dataSet?.get(position).image)
                    .into(viewHolder.largeImage)
            }
            1 -> {
                viewHolder.smallImageCard.visibility = View.VISIBLE
                Glide.with(context)
                    .load(dataSet?.get(position).image)
                    .into(viewHolder.smallImage)
            }
        }

        viewHolder.title.text = dataSet?.get(position)?.title
        viewHolder.source.text = dataSet?.get(position)?.source
        viewHolder.time.text = dataSet?.get(position)?.date

        viewHolder.newsCard.setOnClickListener {
            val dialog = Dialog(viewHolder.newsCard.context)
            dialog.setContentView(R.layout.news_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // set the custom dialog components - text, image and button

            // set the custom dialog components - text, image and button
            val title = dialog.findViewById<TextView>(R.id.title)
            title.text = dataSet!![position].title
            val date = dialog.findViewById<TextView>(R.id.date)
            date.text = dataSet!![position].formattedDate
            val summary = dialog.findViewById<TextView>(R.id.summary)
            summary.text = dataSet!![position].summary
            val source = dialog.findViewById<TextView>(R.id.source)
            source.text = dataSet!![position].source
            val chrome_btn = dialog.findViewById<ImageView>(R.id.chrome_button)
            val twitter_btn = dialog.findViewById<ImageView>(R.id.twitter_button)
            val facebook_btn = dialog.findViewById<ImageView>(R.id.facebook_button)
            chrome_btn.setOnClickListener { openLinkOnBrowser(viewHolder.newsCard.context, dataSet!![position].url) }
            twitter_btn.setOnClickListener { openLinkOnBrowser(viewHolder.newsCard.context, "https://twitter.com/intent/tweet?text=Check out this Link:&url=${dataSet!![position].url}") }
            facebook_btn.setOnClickListener { openLinkOnBrowser(viewHolder.newsCard.context, "https://www.facebook.com/sharer/sharer.php?u=${dataSet!![position].url}") }
            dialog.show()
        }
    }

    fun openLinkOnBrowser(context: Context, url: String) {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(url)
        context.startActivity(openURL)
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = (dataSet?.size)?:0

    fun notifyDataSetChangedWithSort() {
        dataSet?.sortBy { item -> item.difference }
        notifyDataSetChanged()
    }

}
