package com.upsa.europapress.ui.cultura

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import com.upsa.europapress.R
import com.upsa.europapress.activities.MainActivity
import com.upsa.europapress.adapters.ArticleAdapter
import com.upsa.europapress.databinding.FragmentCulturaBinding
import www.sanju.motiontoast.MotionToast


class CulturaFragment : Fragment() {

    private lateinit var viewModel: CulturaViewModel
    private lateinit var parser: Parser
    private lateinit var adapterArticle: ArticleAdapter

    private var _binding: FragmentCulturaBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCulturaBinding.inflate(inflater, container, false)
        // 初始化
        adapterArticle = ArticleAdapter(mutableListOf(), Channel())
        binding.recyclerViewCultura.adapter = adapterArticle
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化
        viewModel = ViewModelProvider(this)[CulturaViewModel::class.java]
        parser = Parser.Builder().cacheExpirationMillis(10L * 60L * 100L).build()

        // 新闻列表
        val recyclerView = binding.recyclerViewCultura
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        viewModel.rssChannel.observe(
            viewLifecycleOwner,
            { channel ->
                if (channel != null) {
                    val news = channel.articles
                    binding.shimmerLayout.visibility = View.GONE
                    adapterArticle = ArticleAdapter(news, channel)
                    recyclerView.adapter = adapterArticle
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        )

        // 解决切换fragment时由于从cache加载数据造成的卡顿
        Handler(Looper.getMainLooper()).postDelayed({ viewModel.fetchFeed(parser) }, 600)

        // 下拉刷新
        val swipeLayout = binding.swipeRefresh
        swipeLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark)
        swipeLayout.canChildScrollUp()
        swipeLayout.setOnRefreshListener {
            adapterArticle.articles.clear()
            adapterArticle.notifyDataSetChanged()
            adapterArticle.articles.clear()
            adapterArticle.notifyDataSetChanged()
            swipeLayout.isRefreshing = true
            viewModel.updateFeed(parser)
        }

        // 网络状态提示
        viewModel.status.observe(
            viewLifecycleOwner,
            { status ->
                status?.let {
                    // Reset status value at first to prevent multitriggering
                    // and to be available to trigger action again
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

        // 打开侧边导航栏
        binding.imageMenu.setOnClickListener { (activity as MainActivity?)?.openDrawer() }
    }
}