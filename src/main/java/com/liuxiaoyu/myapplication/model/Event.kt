package com.liuxiaoyu.myapplication.model

import java.util.*

data class Event(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val description: String = "",
    val date: Date = Date(),  // 改为 Date 类型
    val startTime: String = "",
    val endTime: String = "",
    val reminderEnabled: Boolean = false,
    val reminderMinutes: Int = 0,
    val category: String = "默认" // 新增分类字段
) {
    val hasTime: Boolean
        get() = startTime.isNotEmpty() && endTime.isNotEmpty()
}