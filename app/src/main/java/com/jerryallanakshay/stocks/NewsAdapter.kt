package com.jerryallanakshay.stocks

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONTokener

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
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.watchlist_item, viewGroup, false)

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
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = (dataSet?.size)?:0

}
