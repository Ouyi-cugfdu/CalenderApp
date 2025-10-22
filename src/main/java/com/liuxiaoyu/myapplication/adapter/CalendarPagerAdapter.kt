package com.liuxiaoyu.myapplication.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.liuxiaoyu.myapplication.fragment.MonthFragment
import com.liuxiaoyu.myapplication.fragment.WeekFragment
import com.liuxiaoyu.myapplication.fragment.DayFragment

class CalendarPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MonthFragment()
            1 -> WeekFragment()
            2 -> DayFragment()
            else -> MonthFragment()
        }
    }
}