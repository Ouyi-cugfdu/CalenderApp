package com.liuxiaoyu.myapplication.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.liuxiaoyu.myapplication.R
import com.liuxiaoyu.myapplication.databinding.FragmentWeekBinding
import com.liuxiaoyu.myapplication.databinding.ItemWeekEventBinding
import com.liuxiaoyu.myapplication.model.Event
import com.liuxiaoyu.myapplication.model.EventManager
import java.text.SimpleDateFormat
import java.util.*

class WeekFragment : Fragment() {

    private var _binding: FragmentWeekBinding? = null
    private val binding get() = _binding!!
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy年 MM月dd日", Locale.getDefault())
    private val weekFormat = SimpleDateFormat("yyyy年 第w周", Locale.getDefault())

    // 存储每个单元格的日程信息
    private val cellEvents = mutableMapOf<String, MutableList<Event>>() // key: "day_hour"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeekBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWeekView()
        setupClickListeners()
    }

    private fun setupWeekView() {
        cellEvents.clear()

        val weekDays = getWeekDays()
        println("刷新周视图，日期范围: ${dateFormat.format(weekDays.first())} - ${dateFormat.format(weekDays.last())}")

        updateWeekTitle()
        setupWeekHeader()
        setupTimeAndScheduleGrid()
    }

    private fun updateWeekTitle() {
        binding.tvCurrentWeek.text = weekFormat.format(calendar.time)
    }

    private fun setupWeekHeader() {
        val weekDays = getWeekDays()
        val daysOfWeek = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")

        binding.gridWeekHeader.adapter = object : BaseAdapter() {
            override fun getCount(): Int = 7
            override fun getItem(position: Int): Any = weekDays[position]
            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = if (convertView == null) {
                    TextView(requireContext()).apply {
                        setPadding(16, 16, 16, 16)
                        gravity = Gravity.CENTER
                    }
                } else {
                    convertView as TextView
                }

                val date = weekDays[position]
                val dayCalendar = Calendar.getInstance().apply { time = date }
                val dayOfMonth = dayCalendar.get(Calendar.DAY_OF_MONTH)

                val todayCalendar = Calendar.getInstance()
                if (isSameDay(dayCalendar, todayCalendar)) {
                    textView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
                    textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                } else {
                    textView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                    textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                }

                textView.text = "${daysOfWeek[position]}\n$dayOfMonth"
                textView.gravity = Gravity.CENTER

                return textView
            }
        }
    }

    private fun setupTimeAndScheduleGrid() {
        val weekDays = getWeekDays()
        val weekEvents = getWeekEvents(weekDays)

        // 预处理日程数据
        preprocessEvents(weekEvents)

        // 清除之前的视图
        binding.layoutTimeSlots.removeAllViews()
        binding.tableWeekDays.removeAllViews()

        val timeSlots = generateTimeSlots()
        val displayMetrics = resources.displayMetrics
        val cellHeight = (60 * displayMetrics.density).toInt()
        val dayColumnWidth = (resources.displayMetrics.widthPixels - 60) / 7

        // 创建时间列和日程表格
        for (hour in 0 until 24) {
            // 创建表格行
            val tableRow = TableRow(requireContext())
            tableRow.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                cellHeight
            )

            // 创建时间槽
            // 创建时间槽
            val timeTextView = TextView(requireContext()).apply {
                text = timeSlots[hour]
                setPadding(1, 8, 1, 8)  // 最小边距
                gravity = Gravity.CENTER
                textSize = 9f  // 更小的字体
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                setSingleLine(true)
                setLines(1)
                setMaxLines(1)
                ellipsize = null
                // 强制测量
                setMinWidth(60)
                setMaxWidth(60)
            }
            timeTextView.layoutParams = LinearLayout.LayoutParams(
                60,
                cellHeight
            )
            binding.layoutTimeSlots.addView(timeTextView)

            // 创建7天的日程单元格
            for (day in 0 until 7) {
                val cellKey = "${day}_${hour}"
                val eventsInCell = cellEvents[cellKey] ?: emptyList()

                val cellView = if (eventsInCell.isNotEmpty()) {
                    createEventTableCell(eventsInCell.first(), day, hour, eventsInCell.size)
                } else {
                    createEmptyTableCell(day, hour)
                }

                cellView.layoutParams = TableRow.LayoutParams(
                    dayColumnWidth,
                    cellHeight
                ).apply {
                    setMargins(1, 1, 1, 1)
                }

                tableRow.addView(cellView)
            }

            binding.tableWeekDays.addView(tableRow)
        }
    }

    private fun preprocessEvents(weekEvents: Map<Int, List<Event>>) {
        weekEvents.forEach { (dayIndex, events) ->
            events.forEach { event ->
                if (event.hasTime) {
                    val startHour = getHourFromTime(event.startTime)
                    val endHour = getHourFromTime(event.endTime)

                    val durationHours = if (endHour >= startHour) {
                        endHour - startHour
                    } else {
                        24 - startHour + endHour
                    }

                    for (hourOffset in 0 until durationHours.coerceAtLeast(1)) {
                        val hourIndex = startHour + hourOffset
                        if (hourIndex < 24) {
                            val cellKey = "${dayIndex}_${hourIndex}"

                            if (!cellEvents.containsKey(cellKey)) {
                                cellEvents[cellKey] = mutableListOf()
                            }
                            cellEvents[cellKey]?.add(event)

                            println("日程 '${event.title}' 分配到位置(天$dayIndex 时$hourIndex)")
                        }
                    }
                }
            }
        }
    }

    private fun createEventTableCell(event: Event, dayIndex: Int, hourIndex: Int, eventCount: Int): View {
        val binding = ItemWeekEventBinding.inflate(LayoutInflater.from(requireContext()), null, false)

        binding.tvEventTitle.text = event.title
        binding.tvEventTime.text = "${event.startTime}-${event.endTime}"

        // 🆕 添加调试日志
        println("周视图 - 日程: ${event.title}, 分类: ${event.category}")

        val colorRes = when (event.category) {
            "会议" -> R.color.meeting_color
            "学习" -> R.color.study_color
            "休息" -> R.color.break_color
            "工作" -> R.color.work_color
            "个人" -> R.color.personal_color
            else -> R.color.default_event_color
        }

        // 🆕 添加颜色调试
        println("周视图 - 颜色资源ID: $colorRes")

        binding.root.setBackgroundResource(colorRes)

        binding.root.setOnClickListener {
            showEventDetails(event)
        }

        return binding.root
    }

    private fun createEmptyTableCell(dayIndex: Int, hourIndex: Int): View {
        val textView = TextView(requireContext()).apply {
            setPadding(4, 4, 4, 4)
            gravity = Gravity.CENTER
            textSize = 10f
        }

        textView.text = ""

        val backgroundColor = if (hourIndex % 2 == 0) {
            android.R.color.white
        } else {
            R.color.background_light
        }

        textView.setBackgroundColor(ContextCompat.getColor(requireContext(), backgroundColor))

        textView.setOnClickListener {
            val weekDays = getWeekDays()
            val selectedDate = weekDays[dayIndex]
            showAddEventDialog(selectedDate, hourIndex)
        }

        return textView
    }

    private fun getWeekEvents(weekDays: List<Date>): Map<Int, List<Event>> {
        val weekEvents = mutableMapOf<Int, List<Event>>()

        weekDays.forEachIndexed { index, date ->
            val events = EventManager.getEventsForDate(date)
            weekEvents[index] = events
        }

        return weekEvents
    }

    private fun getHourFromTime(timeString: String): Int {
        return try {
            val parts = timeString.split(":")
            if (parts.size >= 1) {
                parts[0].toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            println("时间解析错误: $timeString, ${e.message}")
            0
        }
    }

    private fun showEventDetails(event: Event) {
        val message = """
            日程: ${event.title}
            描述: ${event.description}
            时间: ${event.startTime} - ${event.endTime}
            日期: ${dateFormat.format(event.date)}
        """.trimIndent()

        activity?.runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showAddEventDialog(selectedDate: Date, defaultHour: Int) {
        val dateStr = dateFormat.format(selectedDate)
        val message = "添加日程到 $dateStr 的 $defaultHour:00"

        activity?.runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getWeekDays(): List<Date> {
        val weekDays = mutableListOf<Date>()
        val tempCalendar = calendar.clone() as Calendar

        tempCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        for (i in 0 until 7) {
            weekDays.add(tempCalendar.time)
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return weekDays
    }

    private fun generateTimeSlots(): List<String> {
        val timeSlots = mutableListOf<String>()
        for (hour in 0 until 24) {
            timeSlots.add(String.format("%02d:00", hour))
        }
        return timeSlots
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun setupClickListeners() {
        binding.btnPrevWeek.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
            setupWeekView()
        }

        binding.btnNextWeek.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            setupWeekView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // 在 DayFragment 和 WeekFragment 中更新颜色识别
    private fun getEventColor(event: Event): Int {
        return when (event.category) {
            "会议" -> ContextCompat.getColor(requireContext(), R.color.meeting_color)
            "学习" -> ContextCompat.getColor(requireContext(), R.color.study_color)
            "休息" -> ContextCompat.getColor(requireContext(), R.color.break_color)
            "工作" -> ContextCompat.getColor(requireContext(), R.color.work_color)
            "个人" -> ContextCompat.getColor(requireContext(), R.color.personal_color)
            else -> ContextCompat.getColor(requireContext(), R.color.default_event_color)
        }
    }

}