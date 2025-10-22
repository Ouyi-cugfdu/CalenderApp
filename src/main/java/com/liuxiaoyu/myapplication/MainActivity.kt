package com.liuxiaoyu.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.liuxiaoyu.myapplication.adapter.CalendarPagerAdapter
class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
    }

    private fun setupViews() {
        // 初始化视图
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // 设置ViewPager适配器
        viewPager.adapter = CalendarPagerAdapter(this)

        // 连接TabLayout和ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "月视图"
                1 -> "周视图"
                2 -> "日视图"
                else -> "未知"
            }
        }.attach()

        // 设置添加按钮点击事件
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddEvent).setOnClickListener {
            // 打开添加日程对话框（稍后实现）
        }
    }
}