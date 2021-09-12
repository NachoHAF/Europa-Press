package com.upsa.europapress.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.mauker.materialsearchview.MaterialSearchView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.prof.rssparser.Article
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import com.squareup.picasso.Picasso
import com.upsa.europapress.R
import com.upsa.europapress.activities.MainActivity
import com.upsa.europapress.adapters.HorizontalRecyclerViewAdapter
import com.upsa.europapress.adapters.SearchAdapterAll
import com.upsa.europapress.adapters.VerticalRecyclerViewAdapter
import com.upsa.europapress.databinding.FragmentHomeBinding
import com.upsa.europapress.ui.profile.ProfileFragment
import www.sanju.motiontoast.MotionToast
import java.util.*


class HomeFragment : Fragment() {

    private lateinit var adapterArticleBanner: HorizontalRecyclerViewAdapter
    private lateinit var adapterArticle: VerticalRecyclerViewAdapter
    private lateinit var adapterSearchAll: SearchAdapterAll
    private lateinit var parser: Parser
    private lateinit var viewModel: HomeViewModel
    private var articlesList: MutableList<Article> = mutableListOf()
    private var channelAll: MutableList<Channel> = mutableListOf()

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        databaseReference =
            FirebaseDatabase.getInstance(
                "https://europa-press-default-rtdb.europe-west1.firebasedatabase.app/"
            )
                .getReference("Users")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 初始化
        adapterArticleBanner = HorizontalRecyclerViewAdapter(mutableListOf(), Channel())
        binding.recyclerViewHorizontal.adapter = adapterArticleBanner

        adapterArticle = VerticalRecyclerViewAdapter(mutableListOf(), Channel())
        binding.recyclerViewVertical.adapter = adapterArticle

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Search View
        val searchView: MaterialSearchView = binding.searchView

        binding.imageSearch.setOnClickListener {
            binding.imageMenu.visibility = View.INVISIBLE
            binding.imageSearch.visibility = View.INVISIBLE
            binding.imageProfile.visibility = View.INVISIBLE
            binding.swipeLayout.visibility = View.INVISIBLE
            binding.searchResult.visibility = View.VISIBLE
            searchView.openSearch()
        }

        searchView.setShouldKeepHistory(false)

        searchView.setSearchViewListener(
            object : MaterialSearchView.SearchViewListener {
                override fun onSearchViewOpened() {
                    // Do something once the view is open.
                    // closeSoftKeyboard()
                }

                override fun onSearchViewClosed() {
                    // Do something once the view is closed.
                    binding.recyclerViewVertical.visibility = View.VISIBLE
                    binding.imageMenu.visibility = View.VISIBLE
                    binding.imageSearch.visibility = View.VISIBLE
                    binding.imageProfile.visibility = View.VISIBLE
                    binding.swipeLayout.visibility = View.VISIBLE
                    binding.searchResult.visibility = View.INVISIBLE
                }
            }
        )

        // 解锁侧边导航栏
        (activity as MainActivity).binding.drawerLayout.setDrawerLockMode(
            DrawerLayout.LOCK_MODE_UNLOCKED
        )

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        parser =
            Parser.Builder()
                .context(requireContext())
                .cacheExpirationMillis(10L * 60L * 100L)
                .build()

        // 打开侧边导航栏
        binding.imageMenu.setOnClickListener { (activity as MainActivity?)?.openDrawer() }

        // 主页横条新闻 和 主页top picks 新闻

        val recyclerView = binding.recyclerViewHorizontal
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val recyclerView2 = binding.recyclerViewVertical
        recyclerView2.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        viewModel.rssChannel.observe(
            viewLifecycleOwner,
            { channel ->
                if (channel != null) {
                    val topBanner = channel.articles.slice(0 until 6)
                    binding.shimmerLayout.visibility = View.GONE
                    adapterArticleBanner =
                        HorizontalRecyclerViewAdapter(topBanner as MutableList<Article>, channel)
                    recyclerView.adapter = adapterArticleBanner

                    val topPicks = channel.articles.slice(6 until channel.articles.size)
                    binding.shimmerLayout1.visibility = View.GONE
                    adapterArticle =
                        VerticalRecyclerViewAdapter(topPicks as MutableList<Article>, channel)
                    recyclerView2.adapter = adapterArticle
                    binding.swipeLayout.isRefreshing = false
                }
            }
        )

        // 下拉刷新
        val swipeLayout = binding.swipeLayout
        swipeLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark)
        swipeLayout.canChildScrollUp()
        swipeLayout.setOnRefreshListener {
            adapterArticleBanner.articles.clear()
            adapterArticleBanner.notifyDataSetChanged()
            adapterArticle.articles.clear()
            adapterArticle.notifyDataSetChanged()
            swipeLayout.isRefreshing = true
            viewModel.updateFeed(parser)
            // swipeLayout.isRefreshing = false
        }

        // 搜索框新闻
        val recyclerView3 = binding.searchList
        recyclerView3.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        viewModel.allChannel.observe(
            viewLifecycleOwner,
            { channel ->
                if (channel != null) {
                    channelAll = channel.allChannel
                    channel.allChannel.forEach { articlesList.addAll(it.articles) }
                }
            }
        )

        searchView.setOnQueryTextListener(
            object : MaterialSearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    closeSoftKeyboard()

                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    val query = newText.lowercase(Locale.getDefault())
                    filterWithQuery(query)
                    return true
                }

                private fun filterWithQuery(query: String) {
                    if (query.isNotEmpty()) {
                        val filteredList: MutableList<Article> = onFilterChanged(query)

                        adapterSearchAll =
                            SearchAdapterAll(
                                filteredList,
                                channelAll
                            ) // --> Envia todos los canales

                        recyclerView3.adapter = adapterSearchAll
                        recyclerView3.visibility = View.VISIBLE
                    } else if (query.isEmpty()) {

                        recyclerView3.visibility = View.INVISIBLE
                    }
                }

                private fun onFilterChanged(filterQuery: String): MutableList<Article> {
                    val filteredList = ArrayList<Article>()
                    for (currentArticle in articlesList) {
                        if (currentArticle
                                .title
                                ?.lowercase(Locale.getDefault())
                                ?.contains(filterQuery) == true
                        ) {
                            filteredList.add(currentArticle)
                        }
                    }
                    return filteredList
                }
            }
        )

        /** 自定义返回按钮 搜索界面 */
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    if (searchView.isOpen) {
                        // Close the search on the back button press.
                        searchView.closeSearch()
                    } else {

                        requireActivity().finish()
                    }
                }
            }
        )

        // 网络状态提示
        viewModel.status.observe(
            viewLifecycleOwner,
            { status ->
                status?.let {
                    // 首先重置状态值以防止多重触发
                    // 并且可以再次触发动作
                    viewModel.status.value = null

                    // 判断当前的Fragment是否已经添加到Activity中
                    if (isAdded) {
                        MotionToast.createColorToast(
                            requireActivity(),
                            "Notice",
                            "There seems to be some network problems, please refresh.",
                            MotionToast.TOAST_NO_INTERNET,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                        )
                        swipeLayout.isRefreshing = false
                    }
                }
            }
        )

        // 个人主页
        binding.imageProfile.setOnClickListener {
            val newFragment = ProfileFragment()
            val transaction =
                (view.context as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.in_from_right, 0, 0, R.anim.out_to_right)
            transaction.add(R.id.fragmentContainerView, newFragment)
            transaction.addToBackStack("fragmentContainer")
            transaction.commit()
        }

        // 首页用户名
        val currentUser = auth.currentUser?.uid

        if (currentUser != null) {
            databaseReference.child(currentUser).get().addOnSuccessListener {
                if (it.exists()) {
                    val username = it.child("username").value
                    if (it.child("firstTime").value != null) {
                        binding.textWelcome.text = "Welcome back, " + username.toString()
                    } else {
                        databaseReference.child(currentUser).child("firstTime").setValue("false")
                        binding.textWelcome.text =
                            "Thank you for your choice, " + username.toString()
                    }
                }
            }
        }

        // 首页用户图片
        val imageUrl = auth.currentUser?.photoUrl
        Picasso.get()
            .load(imageUrl)
            .placeholder(R.drawable.userplaceholder)
            .centerCrop()
            .fit()
            .into(binding.imageProfile)

        viewModel.fetchFeed(parser)

        viewModel.searchFeed(parser)
    }
    private fun closeSoftKeyboard() {
        val iMm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        iMm.hideSoftInputFromWindow(view?.windowToken, 0)
        view?.clearFocus()
    }
}
