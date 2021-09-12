package com.upsa.europapress.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.prof.rssparser.Article
import com.prof.rssparser.Channel
import com.squareup.picasso.Picasso
import com.upsa.europapress.ui.detail.DetailFragment
import com.upsa.europapress.R

class SearchAdapter (val articles: MutableList<Article>, val titleChannel: Channel): RecyclerView.Adapter<SearchAdapter.ViewHolder>(){

    //去重并保持原始顺序
    val articlesRV: MutableList<Article> = LinkedHashSet(articles).toMutableList()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val itemImage: ImageView = view.findViewById(R.id.imageTopNews)
        private val itemTitle: TextView = view.findViewById(R.id.titleTopNews)
        private val itemTime: TextView = view.findViewById(R.id.timeTopNews)

        init {
            itemView.setOnClickListener {

                val touchTitle = articlesRV [absoluteAdapterPosition].title
                val touchTime = articlesRV [absoluteAdapterPosition].pubDate
                val touchChannel = titleChannel.title
                val touchImage = articlesRV [absoluteAdapterPosition].image

                val sizeText = articlesRV [absoluteAdapterPosition].description?.split("<!-- fin pie-firma -->")?.size
                val touchImageDescription : String
                val touchContentDescription : String

                if (sizeText == 1)
                {
                    touchImageDescription = ""
                    touchContentDescription = articlesRV [absoluteAdapterPosition].description?.split("<!-- fin pie-firma -->")?.get(0)
                        ?.toSpanned().toString().replace("null","")
                }
                else
                {
                    touchImageDescription = articlesRV [absoluteAdapterPosition].description?.split("<!-- fin pie-firma -->")?.get(0)
                        ?.toSpanned().toString().replace("-->","").replace("￼","").trim()

                    touchContentDescription = articlesRV [absoluteAdapterPosition].description?.split("<!-- fin pie-firma -->")?.get(1)
                        ?.toSpanned().toString().replace("￼","").trim()
                }





                val bundle = Bundle()
                bundle.putString("touchTitle",touchTitle)
                bundle.putString("touchTime",touchTime)
                bundle.putString("touchChannel",touchChannel)
                bundle.putString("touchImage",touchImage)
                bundle.putString("touchImageDescription",touchImageDescription)
                bundle.putString("touchContentDescription",touchContentDescription)

                val newFragment = DetailFragment()
                newFragment.arguments = bundle



                val transaction = (view.context as AppCompatActivity).supportFragmentManager.beginTransaction()
                transaction.add(R.id.fragmentContainerView, newFragment)
                transaction.addToBackStack("fragmentContainer")
               // transaction.addToBackStack(null)
                transaction.commit()

            }
        }


        @SuppressLint("SimpleDateFormat")
        fun bind(article: Article) {

            itemTitle.text = article.title


            val loadImage: String
            if (article.image.toString() == "null"){
                loadImage = "https://www.catedraldeburgos2021.es/wp-content/uploads/2018/07/europa-press.png"
                Picasso.get().load(loadImage).into(itemImage)
            } else {
                Picasso.get().load(article.image).into(itemImage)
            }


        }
    }





    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.search_layout, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
       // Log.d("msg4", "onBindViewHolder>>" + viewHolder.itemView.toString())
        viewHolder.bind(articlesRV[position])

    }

    override fun getItemCount(): Int {
        return articlesRV.size
    }

    fun String.toSpanned(): Spanned {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            return Html.fromHtml(this)
        }
    }
}