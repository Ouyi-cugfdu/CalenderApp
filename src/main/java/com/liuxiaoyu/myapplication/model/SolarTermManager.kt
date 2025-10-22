package com.liuxiaoyu.myapplication.model

import java.text.SimpleDateFormat
import java.util.*

object SolarTermManager {

    // 2024年的24节气数据（你可以后续扩展其他年份）
    private val solarTerms2024 = listOf(
        SolarTerm("立春", "0204", "立春，万物复苏之始"),
        SolarTerm("雨水", "0219", "雨水，春雨贵如油"),
        SolarTerm("惊蛰", "0305", "惊蛰，春雷惊百虫"),
        SolarTerm("春分", "0320", "春分，昼夜平分"),
        SolarTerm("清明", "0404", "清明，踏青祭祖时"),
        SolarTerm("谷雨", "0419", "谷雨，雨生百谷"),
        SolarTerm("立夏", "0505", "立夏，夏季开始"),
        SolarTerm("小满", "0520", "小满，麦类等夏熟作物籽粒开始饱满"),
        SolarTerm("芒种", "0605", "芒种，有芒的麦子快收，有芒的稻子可种"),
        SolarTerm("夏至", "0621", "夏至，白昼最长"),
        SolarTerm("小暑", "0706", "小暑，天气开始炎热"),
        SolarTerm("大暑", "0722", "大暑，一年中最热的时候"),
        SolarTerm("立秋", "0807", "立秋，秋季开始"),
        SolarTerm("处暑", "0823", "处暑，炎热的天气即将结束"),
        SolarTerm("白露", "0907", "白露，天气转凉，露凝而白"),
        SolarTerm("秋分", "0922", "秋分，昼夜平分"),
        SolarTerm("寒露", "1008", "寒露，露水已寒，将要结冰"),
        SolarTerm("霜降", "1023", "霜降，天气渐冷，开始有霜"),
        SolarTerm("立冬", "1107", "立冬，冬季开始"),
        SolarTerm("小雪", "1122", "小雪，开始下雪"),
        SolarTerm("大雪", "1207", "大雪，降雪量增多"),
        SolarTerm("冬至", "1221", "冬至，白昼最短"),
        SolarTerm("小寒", "0105", "小寒，天气寒冷但还没到最冷"),
        SolarTerm("大寒", "0120", "大寒，一年中最冷的时候")
    )

    // 获取某一天的节气信息
    fun getSolarTermForDate(date: Date): SolarTerm? {
        val dateFormat = SimpleDateFormat("MMdd", Locale.getDefault())
        val dateStr = dateFormat.format(date)

        return solarTerms2024.find { it.date == dateStr }
    }

    // 获取当前月份的节气
    fun getSolarTermsForMonth(date: Date): List<SolarTerm> {
        val calendar = Calendar.getInstance().apply { time = date }
        val month = calendar.get(Calendar.MONTH) + 1 // 月份从0开始，需要+1

        return solarTerms2024.filter {
            it.date.substring(0, 2).toInt() == month
        }
    }
}