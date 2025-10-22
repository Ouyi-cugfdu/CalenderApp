package com.liuxiaoyu.myapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.liuxiaoyu.myapplication.R
import com.liuxiaoyu.myapplication.databinding.FragmentDayBinding
import com.liuxiaoyu.myapplication.model.Event
import com.liuxiaoyu.myapplication.model.EventManager
import com.liuxiaoyu.myapplication.utils.NotificationHelper
import java.text.SimpleDateFormat
import com.liuxiaoyu.myapplication.model.SolarTermManager
import java.util.*

class DayFragment : Fragment() {

    private var _binding: FragmentDayBinding? = null
    private val binding get() = _binding!!
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDayView()
        setupClickListeners()
    }

    //显示编辑删除对话框
    private fun showEditDeleteDialog(event: Event) {
        val options = arrayOf("编辑日程", "删除日程", "取消")

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("操作日程: ${event.title}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openEditEventDialog(event) // 编辑
                    1 -> showDeleteConfirmDialog(event) // 删除
                    // 2 取消，什么都不做
                }
            }
            .show()
    }

    //显示删除确认对话框
    private fun showDeleteConfirmDialog(event: Event) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("删除日程")
            .setMessage("确定要删除日程『${event.title}』吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteEvent(event)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    //删除日程
    private fun deleteEvent(event: Event) {
        // 取消提醒
        val notificationHelper = NotificationHelper(requireContext())
        notificationHelper.cancelNotification(event.id)

        //从 EventManager 删除
        EventManager.deleteEvent(event)

        Toast.makeText(requireContext(), "日程已删除", Toast.LENGTH_SHORT).show()
        loadDayEvents() // 刷新显示
    }

    private fun setupDayView() {
        updateDayTitle()
        loadDayEvents()
        showSolarTermInfo() //添加节气显示
    }

    //添加节气信息显示
    private fun showSolarTermInfo() {
        val solarTerm = SolarTermManager.getSolarTermForDate(calendar.time)

        if (solarTerm != null) {
            binding.layoutSolarTerm.visibility = View.VISIBLE
            binding.tvSolarTermName.text = solarTerm.name
            binding.tvSolarTermDescription.text = solarTerm.description

            // 根据季节设置不同的背景颜色
            val seasonColor = getSeasonColor(solarTerm.name)
            binding.layoutSolarTerm.setBackgroundColor(seasonColor)
        } else {
            binding.layoutSolarTerm.visibility = View.GONE
        }
    }
    //根据节气名称获取季节颜色
    private fun getSeasonColor(solarTermName: String): Int {
        return when {
            solarTermName.contains("春") -> ContextCompat.getColor(requireContext(), R.color.spring_color)
            solarTermName.contains("夏") -> ContextCompat.getColor(requireContext(), R.color.summer_color)
            solarTermName.contains("秋") -> ContextCompat.getColor(requireContext(), R.color.autumn_color)
            solarTermName.contains("冬") -> ContextCompat.getColor(requireContext(), R.color.winter_color)
            else -> ContextCompat.getColor(requireContext(), R.color.default_season_color)
        }
    }
    private fun updateDayTitle() {
        binding.tvCurrentDate.text = dateFormat.format(calendar.time)

        // 检查是否是今天
        val today = Calendar.getInstance()
        if (isSameDay(calendar, today)) {
            binding.tvCurrentDate.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        } else {
            binding.tvCurrentDate.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }
    }

    private fun loadDayEvents() {
        val events = EventManager.getEventsForDate(calendar.time)
        setupEventList(events)
    }

    private fun setupEventList(events: List<Event>) {
        binding.eventsContainer.removeAllViews()

        if (events.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.tvEmptyState.visibility = View.GONE

            // 按开始时间排序
            val sortedEvents = events.sortedBy { event ->
                try {
                    val timeParts = event.startTime.split(":")
                    timeParts[0].toInt() * 60 + (timeParts.getOrNull(1)?.toInt() ?: 0)
                } catch (e: Exception) {
                    0
                }
            }

            sortedEvents.forEach { event ->
                addEventView(event)
            }
        }
    }

    //在 addEventView 方法中添加长按监听
    private fun addEventView(event: Event) {
        val eventView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_day_event, binding.eventsContainer, false)

        val tvEventTitle = eventView.findViewById<TextView>(R.id.tvEventTitle)
        val tvEventTime = eventView.findViewById<TextView>(R.id.tvEventTime)
        val tvEventDescription = eventView.findViewById<TextView>(R.id.tvEventDescription)

        tvEventTitle.text = event.title
        tvEventTime.text = formatEventTime(event)
        tvEventDescription.text = event.description

        // 设置事件颜色
        val colorRes = when (event.category) {
            "会议" -> R.color.meeting_color
            "学习" -> R.color.study_color
            "休息" -> R.color.break_color
            "工作" -> R.color.work_color
            "个人" -> R.color.personal_color
            else -> R.color.default_event_color
        }

        eventView.setBackgroundResource(colorRes)

        // 点击显示详情
        eventView.setOnClickListener {
            showEventDetails(event)
        }

        //长按编辑删除
        eventView.setOnLongClickListener {
            showEditDeleteDialog(event)
            true
        }

        binding.eventsContainer.addView(eventView)
    }

    //打开编辑对话框
    private fun openEditEventDialog(event: Event) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_event, null)

        // 获取所有组件
        val etEventTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEventTitle)
        val etEventDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEventDescription)
        val btnStartTime = dialogView.findViewById<Button>(R.id.btnStartTime)
        val btnEndTime = dialogView.findViewById<Button>(R.id.btnEndTime)
        val cbReminder = dialogView.findViewById<CheckBox>(R.id.cbReminder)
        val layoutReminderTime = dialogView.findViewById<LinearLayout>(R.id.layoutReminderTime)
        val spinnerReminder = dialogView.findViewById<Spinner>(R.id.spinnerReminder)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        //预填充数据
        etEventTitle.setText(event.title)
        etEventDescription.setText(event.description)
        btnStartTime.text = if (event.startTime.isNotEmpty()) "开始: ${event.startTime}" else "开始时间"
        btnEndTime.text = if (event.endTime.isNotEmpty()) "结束: ${event.endTime}" else "结束时间"
        cbReminder.isChecked = event.reminderEnabled

        // 设置分类选项
        val categories = arrayOf("默认", "会议", "学习", "休息", "工作", "个人")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        val categoryPosition = categories.indexOf(event.category).coerceAtLeast(0)
        spinnerCategory.setSelection(categoryPosition)

        // 设置提醒时间选项
        val reminderOptions = arrayOf("5分钟", "10分钟", "15分钟", "30分钟", "1小时", "2小时")
        val reminderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, reminderOptions)
        reminderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReminder.adapter = reminderAdapter

        // 设置提醒时间选择
        if (event.reminderEnabled) {
            val reminderPosition = when (event.reminderMinutes) {
                5 -> 0
                10 -> 1
                15 -> 2
                30 -> 3
                60 -> 4
                120 -> 5
                else -> 2
            }
            spinnerReminder.setSelection(reminderPosition)
            layoutReminderTime.visibility = View.VISIBLE
        }

        //修复：正确声明变量
        var startTime = event.startTime
        var endTime = event.endTime
        var reminderEnabled = event.reminderEnabled
        var reminderMinutes = event.reminderMinutes

        //修复：时间选择逻辑
        btnStartTime.setOnClickListener {
            showTimePickerDialog(true) { selectedTime ->
                startTime = selectedTime
                btnStartTime.text = "开始: $startTime"
            }
        }

        btnEndTime.setOnClickListener {
            showTimePickerDialog(false) { selectedTime ->
                endTime = selectedTime
                btnEndTime.text = "结束: $endTime"
            }
        }

        //提醒设置
        cbReminder.setOnCheckedChangeListener { _, isChecked ->
            reminderEnabled = isChecked
            layoutReminderTime.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("编辑日程")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val title = etEventTitle.text.toString().trim()
                if (title.isEmpty()) {
                    etEventTitle.error = "请输入日程标题"
                    return@setPositiveButton
                }

                val description = etEventDescription.text.toString().trim()
                val category = spinnerCategory.selectedItem as String

                // 获取提醒时间
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

                //取消旧提醒
                val notificationHelper = NotificationHelper(requireContext())
                notificationHelper.cancelNotification(event.id)

                //更新事件
                val updatedEvent = event.copy(
                    title = title,
                    description = description,
                    startTime = startTime,
                    endTime = endTime,
                    reminderEnabled = reminderEnabled,
                    reminderMinutes = if (reminderEnabled) reminderMinutes else 0,
                    category = category
                )

                //更新到 EventManager
                EventManager.updateEvent(updatedEvent)

                //设置新提醒
                if (reminderEnabled) {
                    notificationHelper.scheduleNotification(updatedEvent)
                }

                Toast.makeText(requireContext(), "日程更新成功！", Toast.LENGTH_SHORT).show()
                loadDayEvents() // 刷新显示
            }
            .setNegativeButton("取消", null)
            .create()

        dialog.show()
    }

    //在 DayFragment 类中添加时间选择器方法
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

    // 其他现有方法...
    private fun formatEventTime(event: Event): String {
        return if (event.hasTime) {
            "${event.startTime} - ${event.endTime}"
        } else {
            "全天"
        }
    }

    private fun showEventDetails(event: Event) {
        val message = """
            日程: ${event.title}
            描述: ${event.description}
            时间: ${formatEventTime(event)}
            提醒: ${if (event.reminderEnabled) "提前${event.reminderMinutes}分钟" else "无"}
        """.trimIndent()

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun setupClickListeners() {
        binding.btnPreviousDay.setOnClickListener {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            setupDayView() //这会同时刷新节气和日程
        }

        binding.btnNextDay.setOnClickListener {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            setupDayView() //这会同时刷新节气和日程
        }

        binding.btnToday.setOnClickListener {
            calendar.time = Date()
            setupDayView() //这会同时刷新节气和日程
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}