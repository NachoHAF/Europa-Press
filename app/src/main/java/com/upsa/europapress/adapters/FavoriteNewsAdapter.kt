package com.upsa.europapress.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.squareup.picasso.Picasso
import com.upsa.europapress.ui.detail.DetailFragment
import com.upsa.europapress.model.News
import com.upsa.europapress.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FavoriteNewsAdapter(val newsList: ArrayList<News>) : RecyclerView.Adapter<FavoriteNewsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemImage: ImageView = itemView.findViewById(R.id.imageTopNews)
        private val itemTitle: TextView = itemView.findViewById(R.id.titleTopNews)
        private val itemTime: TextView = itemView.findViewById(R.id.timeTopNews)

        init {
            itemView.setOnClickListener {
                val touchTitle = newsList[absoluteAdapterPosition].title
                val touchTime = newsList[absoluteAdapterPosition].time
                val touchChannel = newsList[absoluteAdapterPosition].channelName
                val touchImage = newsList[absoluteAdapterPosition].image
                val touchImageDescription = newsList[absoluteAdapterPosition].imgDescription.toString()
                val touchContentDescription = newsList[absoluteAdapterPosition].contentDescription
                val touchLink = newsList[absoluteAdapterPosition].link

                val bundle = Bundle()
                bundle.putString("touchTitle", touchTitle)
                bundle.putString("touchTime", touchTime)
                bundle.putString("touchChannel", touchChannel)
                bundle.putString("touchImage", touchImage)
                bundle.putString("touchImageDescription", touchImageDescription)
                bundle.putString("touchContentDescription", touchContentDescription)
                bundle.putString("touchLink", touchLink)

                val newFragment = DetailFragment()
                newFragment.arguments = bundle

                val transaction = (itemView.context as AppCompatActivity).supportFragmentManager.beginTransaction()
                transaction.add(R.id.fragmentContainerView, newFragment)
                transaction.addToBackStack("fragmentContainer")
                transaction.commit()
            }
        }

        fun bind(news: News) {

            itemTitle.text = news.title

            val loadImage: String
            if (news.image.toString() == "null") {
                loadImage = "https://www.catedraldeburgos2021.es/wp-content/uploads/2018/07/europa-press.png"
                Picasso.get().load(loadImage).into(itemImage)
            } else {
                Picasso.get().load(news.image).into(itemImage)
            }

            val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
            val time = format.parse(news.time ?: "Sun, 22 Aug 2021 16:17:08 GMT")
            itemTime.text = time?.time?.let { TimeAgo.using(it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.articles_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(newsList[position])
    }

    override fun getItemCount(): Int {
        return newsList.size
    }
}
