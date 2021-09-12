package com.upsa.europapress.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.upsa.europapress.R
import com.upsa.europapress.databinding.FragmentViewPagerBinding
import com.upsa.europapress.onboarding.screens.FirstFragment
import com.upsa.europapress.onboarding.screens.SecondFragment
import com.upsa.europapress.onboarding.screens.ThirdFragment


class ViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentViewPagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.statusBarColor = activity?.getColor(R.color.viewpager)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentViewPagerBinding.inflate(inflater, container, false)

        val fragmentList = arrayListOf(FirstFragment(), SecondFragment(), ThirdFragment())

        val adapter =
            ViewPagerAdapter(fragmentList, requireActivity().supportFragmentManager, lifecycle)

        binding.viewPager.adapter = adapter

        // return view
        return binding.root
    }
}