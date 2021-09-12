package com.upsa.europapress.ui.favorite

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.upsa.europapress.R
import com.upsa.europapress.activities.MainActivity
import com.upsa.europapress.adapters.FavoriteNewsAdapter
import com.upsa.europapress.databinding.FragmentFavoriteBinding
import com.upsa.europapress.model.News


class FavoriteFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var newsArrayList: ArrayList<News>
    private lateinit var adapterNews: FavoriteNewsAdapter

    private var _binding: FragmentFavoriteBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // recyclerView
        val recyclerView = binding.recyclerViewFavorite
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        newsArrayList = arrayListOf()

        // 打开侧边导航栏
        binding.imageMenu.setOnClickListener { (activity as MainActivity?)?.openDrawer() }

        // 下拉刷新
        adapterNews = FavoriteNewsAdapter(newsArrayList)
        val swipeLayout = binding.swipeRefresh
        swipeLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark)
        swipeLayout.canChildScrollUp()
        swipeLayout.setOnRefreshListener {
            adapterNews.newsList.clear()
            adapterNews.notifyDataSetChanged()
            swipeLayout.isRefreshing = true
            getNewsData()
        }

        // 解决切换fragment时由于从cache加载数据造成的卡顿
        Handler(Looper.getMainLooper()).postDelayed({ getNewsData() }, 600)
    }

    private fun getNewsData() {

        auth = Firebase.auth
        val currentUser = auth.currentUser?.uid
        if (currentUser != null) {
            databaseReference =
                FirebaseDatabase.getInstance(
                    "https://europa-press-default-rtdb.europe-west1.firebasedatabase.app/"
                )
                    .getReference("Users")
                    .child(currentUser)
                    .child("favorites")
            databaseReference.keepSynced(true)

            databaseReference.addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        adapterNews.newsList.clear()
                        adapterNews.notifyDataSetChanged()
                        if (snapshot.exists()) {
                            for (newsSnapshot in snapshot.children) {
                                val news = newsSnapshot.getValue(News::class.java)
                                if (news != null) {
                                    newsArrayList.add(news)
                                }
                            }
                            binding.recyclerViewFavorite.adapter =
                                FavoriteNewsAdapter(newsArrayList)
                            binding.shimmerLayout.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                        } else {
                            binding.swipeRefresh.visibility = View.GONE
                            binding.warning.visibility = View.VISIBLE
                            binding.noData.visibility = View.VISIBLE
                            binding.news.visibility = View.INVISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("DATABASE ERROR")
                    }
                }
            )
        }
    }
}