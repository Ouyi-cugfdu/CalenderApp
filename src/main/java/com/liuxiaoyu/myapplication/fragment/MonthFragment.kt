package com.liuxiaoyu.myapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.liuxiaoyu.myapplication.R
import com.liuxiaoyu.myapplication.databinding.FragmentMonthBinding
import com.liuxiaoyu.myapplication.model.Event
import com.liuxiaoyu.myapplication.model.EventManager
import java.text.SimpleDateFormat
import com.google.android.material.textfield.TextInputEditText
import com.liuxiaoyu.myapplication.utils.NotificationHelper
import com.liuxiaoyu.myapplication.model.SolarTermManager
import android.widget.CheckBox
import android.widget.LinearLayout
import com.liuxiaoyu.myapplication.model.SolarTerm
import android.widget.*
import java.util.*

class MonthFragment : Fragment() {

    private var _binding: FragmentMonthBinding? = null
    private val binding get() = _binding!!
    private val calendar = Calendar.getInstance()
    private val monthFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
    private val currentCalendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendar()
        setupClickListeners()
    }

    private fun setupCalendar() {
        updateMonthTitle()
        setupWeekDays()
        setupCalendarGrid()
        showMonthSolarTerms()
    }
    private fun showMonthSolarTerms() {
        val solarTerms = SolarTermManager.getSolarTermsForMonth(calendar.time)

        if (solarTerms.isNotEmpty()) {
            binding.layoutMonthSolarTerms.visibility = View.VISIBLE
            setupSolarTermsList(solarTerms)
        } else {
            binding.layoutMonthSolarTerms.visibility = View.GONE
        }
    }

    //设置节气列表显示
    private fun setupSolarTermsList(solarTerms: List<SolarTerm>) {
        binding.layoutSolarTermsContainer.removeAllViews()

        solarTerms.forEach { solarTerm ->
            addSolarTermView(solarTerm)
        }
    }

    //添加单个节气显示视图
    private fun addSolarTermView(solarTerm: SolarTerm) {
        val solarTermView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_solar_term, binding.layoutSolarTermsContainer, false)

        val tvSolarTermName = solarTermView.findViewById<TextView>(R.id.tvSolarTermName)
        val tvSolarTermDate = solarTermView.findViewById<TextView>(R.id.tvSolarTermDate)
        val tvSolarTermDesc = solarTermView.findViewById<TextView>(R.id.tvSolarTermDesc)

        tvSolarTermName.text = solarTerm.name
        tvSolarTermDate.text = formatSolarTermDate(solarTerm.date)
        tvSolarTermDesc.text = solarTerm.description

        // 设置季节颜色
        val seasonColor = getSeasonColor(solarTerm.name)
        solarTermView.setBackgroundColor(seasonColor)

        binding.layoutSolarTermsContainer.addView(solarTermView)
    }

    //格式化节气日期（MMdd → 月日）
    private fun formatSolarTermDate(dateStr: String): String {
        return try {
            val month = dateStr.substring(0, 2).toInt()
            val day = dateStr.substring(2, 4).toInt()
            "${month}月${day}日"
        } catch (e: Exception) {
            dateStr
        }
    }

    //根据节气名称获取季节颜色（从 DayFragment 复制过来）
    private fun getSeasonColor(solarTermName: String): Int {
        return when {
            solarTermName.contains("春") -> ContextCompat.getColor(requireContext(), R.color.spring_color)
            solarTermName.contains("夏") -> ContextCompat.getColor(requireContext(), R.color.summer_color)
            solarTermName.contains("秋") -> ContextCompat.getColor(requireContext(), R.color.autumn_color)
            solarTermName.contains("冬") -> ContextCompat.getColor(requireContext(), R.color.winter_color)
            else -> ContextCompat.getColor(requireContext(), R.color.default_season_color)
        }
    }
    private fun updateMonthTitle() {
        binding.tvCurrentMonth.text = monthFormat.format(calendar.time)
    }

    private fun setupWeekDays() {
        val weekDays = arrayOf("日", "一", "二", "三", "四", "五", "六")
        // 星期标题会在网格中一起显示
    }

    private fun setupCalendarGrid() {
        val calendarDays = getCalendarDays()
        binding.gridCalendar.adapter = CalendarAdapter(calendarDays)
    }

    private fun getCalendarDays(): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()

        // 设置日历为当前月份的第一天
        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

        // 获取当月第一天是星期几（周日=1, 周一=2, ..., 周六=7）
        val firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK)

        // 添加上个月的最后几天（填充网格前面的空白）
        val daysFromPreviousMonth = firstDayOfWeek - 1
        tempCalendar.add(Calendar.DAY_OF_MONTH, -daysFromPreviousMonth)

        // 生成6行×7列=42个日期（足够显示任何月份）
        for (i in 0 until 42) {
            val isCurrentMonth = tempCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
            val isToday = isSameDay(tempCalendar, currentCalendar)

            days.add(CalendarDay(
                day = tempCalendar.get(Calendar.DAY_OF_MONTH),
                isCurrentMonth = isCurrentMonth,
                isToday = isToday,
                date = tempCalendar.time
            ))

            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun setupClickListeners() {
        binding.btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            setupCalendar()
        }

        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            setupCalendar()
        }
    }

    // 添加日期点击处理方法
    private fun onDateClicked(day: CalendarDay) {
        showAddEventDialog(day.date)
    }

    // 添加显示对话框的方法
    private fun showAddEventDialog(selectedDate: Date) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_event, null)

        // 获取所有视图组件
        val etEventTitle = dialogView.findViewById<TextInputEditText>(R.id.etEventTitle)
        val etEventDescription = dialogView.findViewById<TextInputEditText>(R.id.etEventDescription)
        val btnStartTime = dialogView.findViewById<Button>(R.id.btnStartTime)
        val btnEndTime = dialogView.findViewById<Button>(R.id.btnEndTime)
        val cbReminder = dialogView.findViewById<CheckBox>(R.id.cbReminder)
        val layoutReminderTime = dialogView.findViewById<LinearLayout>(R.id.layoutReminderTime)
        val spinnerReminder = dialogView.findViewById<Spinner>(R.id.spinnerReminder)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        // 🆕 设置分类选项
        val categories = arrayOf("默认", "会议", "学习", "休息", "工作", "个人")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        //设置提醒时间选项
        val reminderOptions = arrayOf("5分钟", "10分钟", "15分钟", "30分钟", "1小时", "2小时")
        val reminderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, reminderOptions)
        reminderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReminder.adapter = reminderAdapter
        spinnerReminder.setSelection(2) // 默认选择15分钟

        // 初始化时间变量
        var startTime = ""
        var endTime = ""
        var reminderEnabled = false
        var reminderMinutes = 15

        // 设置提醒复选框的点击事件
        cbReminder.setOnCheckedChangeListener { _, isChecked ->
            reminderEnabled = isChecked
            layoutReminderTime.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // 设置开始时间按钮点击事件
        btnStartTime.setOnClickListener {
            showTimePickerDialog(true) { selectedTime ->
                startTime = selectedTime
                btnStartTime.text = "开始: $startTime"
            }
        }

        // 设置结束时间按钮点击事件
        btnEndTime.setOnClickListener {
            showTimePickerDialog(false) { selectedTime ->
                endTime = selectedTime
                btnEndTime.text = "结束: $endTime"
            }
        }

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val title = etEventTitle.text.toString().trim()
            if (title.isEmpty()) {
                etEventTitle.error = "请输入日程标题"
                return@setOnClickListener
            }

            val description = etEventDescription.text.toString().trim()
            val category = spinnerCategory.selectedItem as String

            //获取提醒时间
            if (reminderEnabled) {
                val selectedReminderText = spinnerReminder.selectedItem as String
                reminderMinutes = when (selectedReminderText) {
                    "5分钟" -> 5
                    "10分钟" -> 10
                    "15分钟" -> 15
                    "30分钟" -> 30
                    "1小时" -> 60
                    "2小时" -> 120
                    else -> 15
                }
            }

            // 创建新日程
            val newEvent = Event(
                title = title,
                description = description,
                date = selectedDate,
                startTime = startTime,
                endTime = endTime,
                reminderEnabled = reminderEnabled,
                reminderMinutes = if (reminderEnabled) reminderMinutes else 0,
                category = category
            )

            // 保存日程
            EventManager.addEvent(newEvent)

            //设置提醒通知
            if (reminderEnabled) {
                scheduleNotification(newEvent)
            }

            // 显示成功提示
            val reminderText = if (reminderEnabled) "，提前${reminderMinutes}分钟提醒" else ""
            Toast.makeText(requireContext(), "日程添加成功$reminderText！", Toast.LENGTH_SHORT).show()

            dialog.dismiss()
            setupCalendar()
        }

        dialog.show()
    }

    // 添加时间选择器方法
    private fun showTimePickerDialog(isStartTime: Boolean, onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = android.app.TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                // 格式化时间为 HH:mm
                val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                onTimeSelected(formattedTime)
            },
            currentHour,
            currentMinute,
            true // 24小时制
        )

        // 设置对话框标题
        val title = if (isStartTime) "选择开始时间" else "选择结束时间"
        timePickerDialog.setTitle(title)

        timePickerDialog.show()
    }
    // 日期数据类
    data class CalendarDay(
        val day: Int,
        val isCurrentMonth: Boolean,
        val isToday: Boolean,
        val date: Date
    )

    // 日历适配器
    inner class CalendarAdapter(private val days: List<CalendarDay>) : BaseAdapter() {
        override fun getCount(): Int = days.size

        override fun getItem(position: Int): CalendarDay = days[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val day = getItem(position)

            val textView = if (convertView == null) {
                TextView(requireContext()).apply {
                    setPadding(16, 16, 16, 16)
                    gravity = android.view.Gravity.CENTER
                    // 添加点击效果
                    isClickable = true
                    isFocusable = true
                }
            } else {
                convertView as TextView
            }

            // 检查该日期是否有日程
            val events = EventManager.getEventsForDate(day.date)
            val hasEvents = events.isNotEmpty()

            // 设置日期文本 - 如果有日程，在日期后加一个小圆点符号
            val dayText = if (hasEvents) {
                "${day.day} •"  // 在日期后添加圆点标记
            } else {
                day.day.toString()
            }
            textView.text = dayText

            // 设置样式
            when {
                day.isToday -> {
                    textView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
                    textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                }
                day.isCurrentMonth -> {
                    textView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                    textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                }
                else -> {
                    textView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                    textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                }
            }

            // 添加点击事件
            textView.setOnClickListener {
                onDateClicked(day)
            }

            return textView
        }

        // ViewHolder 类来缓存视图引用
        /*private class ViewHolder(view: View) {
            val tvDayNumber: TextView = view.findViewById(R.id.tvDayNumber)
            val eventDot: View = view.findViewById(R.id.eventDot)
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // 在 MonthFragment 类中添加方法
    private fun scheduleNotification(event: Event) {
        val notificationHelper = NotificationHelper(requireContext())
        notificationHelper.scheduleNotification(event)
    }

    // 测试通知方法
    private fun testNotification() {
        val notificationHelper = NotificationHelper(requireContext())
        notificationHelper.showTestNotification()
    }
}