package com.liuxiaoyu.myapplication.model

import java.util.*

object EventManager {
    private val events = mutableListOf<Event>()

    fun addEvent(event: Event) {
        events.add(event)
    }

    fun getEventsForDate(date: Date): List<Event> {
        val calendar = Calendar.getInstance().apply { time = date }
        return events.filter { event ->
            val eventCalendar = Calendar.getInstance().apply { time = event.date }
            eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                    eventCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
        }
    }

    fun getAllEvents(): List<Event> = events.toList()

    // 🆕 添加更新方法
    fun updateEvent(updatedEvent: Event) {
        val index = events.indexOfFirst { it.id == updatedEvent.id }
        if (index != -1) {
            events[index] = updatedEvent
            println("事件更新成功: ${updatedEvent.title}")
        } else {
            println("未找到要更新的事件: ${updatedEvent.title}")
        }
    }

    // 🆕 添加删除方法
    fun deleteEvent(event: Event) {
        val removed = events.remove(event)
        if (removed) {
            println("事件删除成功: ${event.title}")
        } else {
            println("未找到要删除的事件: ${event.title}")
        }
    }

    // 🆕 根据ID查找事件
    fun getEventById(id: Long): Event? {
        return events.find { it.id == id }
    }
}