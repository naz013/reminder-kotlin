package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import java.util.*
import java.util.regex.Pattern

internal class RuWorker : Worker() {
    override val weekdays: Array<String>
        protected get() = arrayOf("воскресен", "понедельн", "вторн", "среду?", "червер", "пятниц", "суббот")

    override fun hasCalendar(input: String): Boolean {
        return input.matches(".*календарь.*")
    }

    override fun clearCalendar(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (part!!.matches(".*календарь.*")) {
                parts[i] = ""
                break
            }
        }
        return clipStrings(parts)
    }

    override fun getWeekDays(input: String): List<Int> {
        val array = intArrayOf(0, 0, 0, 0, 0, 0, 0)
        val parts: Array<String> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        val weekDays = weekdays
        for (part in parts) {
            for (i in weekDays.indices) {
                val day = weekDays[i]
                if (part.matches(".*$day.*")) {
                    array[i] = 1
                    break
                }
            }
        }
        val list: MutableList<Int> = ArrayList()
        for (anArray in array) list.add(anArray)
        return list
    }

    override fun clearWeekDays(input: String): String {
        var parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        val weekDays = weekdays
        for (i in parts.indices) {
            val part = parts[i]
            for (day in weekDays) {
                if (part!!.matches(".*$day.*")) {
                    parts[i] = ""
                    break
                }
            }
        }
        parts = clipStrings(parts).split(Worker.Companion.WHITESPACES).toTypedArray()
        val sb = StringBuilder()
        for (i in parts.indices) {
            val part = parts[i]!!.trim { it <= ' ' }
            if (!part.matches("в")) sb.append(" ").append(part)
        }
        return sb.toString().trim { it <= ' ' }
    }

    override fun getDaysRepeat(input: String): Long {
        val parts: Array<String> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (hasDays(part)) {
                val integer: Int
                integer = try {
                    parts[i - 1].toInt()
                } catch (e: NumberFormatException) {
                    1
                } catch (e: ArrayIndexOutOfBoundsException) {
                    0
                }
                return integer * Worker.Companion.DAY
            }
        }
        return 0
    }

    override fun clearDaysRepeat(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (hasDays(part)) {
                try {
                    parts[i - 1]!!.toInt()
                    parts[i - 1] = ""
                } catch (ignored: NumberFormatException) {
                }
                parts[i] = ""
                break
            }
        }
        return clipStrings(parts)
    }

    override fun hasRepeat(input: String?): Boolean {
        return input!!.matches(".*кажд.*") || hasEveryDay(input)
    }

    override fun hasEveryDay(input: String?): Boolean {
        return input!!.matches(".*ежедневн.*")
    }

    override fun clearRepeat(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (hasRepeat(part)) {
                parts[i] = ""
                break
            }
        }
        return clipStrings(parts)
    }

    override fun hasTomorrow(input: String): Boolean {
        return input.matches(".*завтра.*")
    }

    override fun clearTomorrow(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (part!!.matches(".*завтра.*")) {
                parts[i] = ""
                break
            }
        }
        return clipStrings(parts)
    }

    override fun getMessage(input: String): String {
        val parts: Array<String> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        val sb = StringBuilder()
        var isStart = false
        for (part in parts) {
            if (isStart) sb.append(" ").append(part)
            if (part.matches("текст(ом)?")) isStart = true
        }
        return sb.toString().trim { it <= ' ' }
    }

    override fun clearMessage(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (part!!.matches("текст(ом)?")) {
                try {
                    if (parts[i - 1]!!.matches("с")) {
                        parts[i - 1] = ""
                    }
                } catch (ignored: IndexOutOfBoundsException) {
                }
                parts[i] = ""
            }
        }
        return clipStrings(parts)
    }

    override fun getMessageType(input: String?): Action? {
        if (input!!.matches(".*сообщение.*")) return Action.MESSAGE
        return if (input.matches(".*письмо?.*")) Action.MAIL else null
    }

    override fun clearMessageType(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            val type = getMessageType(part)
            if (type != null) {
                parts[i] = ""
                break
            }
        }
        return clipStrings(parts)
    }

    override fun getAmpm(input: String?): Ampm? {
        if (input!!.matches(".*утр(а|ом)?.*")) return Ampm.MORNING
        if (input.matches(".*вечер.*")) return Ampm.EVENING
        if (input.matches(".*днем.*")) return Ampm.NOON
        return if (input.matches(".*ночью.*")) Ampm.NIGHT else null
    }

    override fun clearAmpm(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (getAmpm(part) != null) {
                parts[i] = ""
                break
            }
        }
        return clipStrings(parts)
    }

    override fun getShortTime(input: String?): Date? {
        val pattern = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]")
        val matcher = pattern.matcher(input)
        if (matcher.find()) {
            val time = matcher.group().trim { it <= ' ' }
            for (format in hourFormats) {
                var date: Date?
                try {
                    date = format!!.parse(time)
                    if (date != null) return date
                } catch (ignored: Exception) {
                }
            }
        }
        return null
    }

    override fun clearTime(input: String?): String {
        var input = input
        var parts: Array<String?> = input!!.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (hasHours(part) != -1) {
                val index = hasHours(part)
                parts[i] = ""
                try {
                    parts[i - index]!!.toInt()
                    parts[i - index] = ""
                } catch (ignored: Exception) {
                }
            }
            if (hasMinutes(part) != -1) {
                val index = hasMinutes(part)
                try {
                    parts[i - index]!!.toInt()
                    parts[i - index] = ""
                } catch (e: Exception) {
                }
                parts[i] = ""
            }
        }
        val pattern = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]")
        input = clipStrings(parts)
        val matcher = pattern.matcher(input)
        if (matcher.find()) {
            val time = matcher.group().trim { it <= ' ' }
            input = input.replace(time, "")
        }
        parts = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        val sb = StringBuilder()
        for (i in parts.indices) {
            val part = parts[i]!!.trim { it <= ' ' }
            if (!part.matches("в")) sb.append(" ").append(part)
        }
        return sb.toString().trim { it <= ' ' }
    }

    override fun getMonth(input: String?): Int {
        var res = -1
        if (input!!.contains("январь") || input.contains("января")) res = 0
        if (input.contains("февраль") || input.contains("февраля")) res = 1
        if (input.contains("март") || input.contains("марта")) res = 2
        if (input.contains("апрель") || input.contains("апреля")) res = 3
        if (input.contains("май") || input.contains("мая")) res = 4
        if (input.contains("июнь") || input.contains("июня")) res = 5
        if (input.contains("июль") || input.contains("июля")) res = 6
        if (input.contains("август") || input.contains("августа")) res = 7
        if (input.contains("сентябрь") || input.contains("сентября")) res = 8
        if (input.contains("октябрь") || input.contains("октября")) res = 9
        if (input.contains("ноябрь") || input.contains("ноября")) res = 10
        if (input.contains("декабрь") || input.contains("декабря")) res = 11
        return res
    }

    override fun hasCall(input: String?): Boolean {
        return input!!.matches(".*звонить.*")
    }

    override fun clearCall(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (part!!.matches(".*звонить.*")) {
                parts[i] = ""
                break
            }
        }
        return clipStrings(parts)
    }

    override fun isTimer(input: String?): Boolean {
        return input!!.matches(".*через.*")
    }

    override fun cleanTimer(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (isTimer(part)) {
                parts[i] = ""
                break
            }
        }
        return clipStrings(parts)
    }

    override fun hasSender(input: String?): Boolean {
        return input!!.matches(".*отправ.*")
    }

    override fun clearSender(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (part!!.matches(".*отправ.*")) {
                parts[i] = ""
                break
            }
        }
        return clipStrings(parts)
    }

    override fun hasNote(input: String): Boolean {
        return input.contains("заметка")
    }

    override fun clearNote(input: String): String {
        var input = input
        input = input.replace("заметка", "")
        return input.trim { it <= ' ' }
    }

    override fun hasAction(input: String): Boolean {
        return (input.startsWith("открыть") || input.matches(".*помощь.*") ||
                input.matches(".*настро.*") || input.matches(".*громкость.*")
                || input.matches(".*сообщить.*"))
    }

    override fun getAction(input: String): Action? {
        return if (input.matches(".*помощь.*")) {
            Action.HELP
        } else if (input.matches(".*громкость.*")) {
            Action.VOLUME
        } else if (input.matches(".*настройки.*")) {
            Action.SETTINGS
        } else if (input.matches(".*сообщить.*")) {
            Action.REPORT
        } else {
            Action.APP
        }
    }

    override fun hasEvent(input: String): Boolean {
        return input.startsWith("добавить") || input.matches("ново?е?ы?й?.*")
    }

    override fun getEvent(input: String): Action? {
        return if (input.matches(".*день рождения.*")) {
            Action.BIRTHDAY
        } else if (input.matches(".*напоминан.*")) {
            Action.REMINDER
        } else Action.NO_EVENT
    }

    override fun hasEmptyTrash(input: String): Boolean {
        return input.matches(".*очисти(ть)? корзин.*")
    }

    override fun hasDisableReminders(input: String): Boolean {
        return input.matches(".*выключи (все)? ?напоминания.*") || input.matches(".*отключи(ть)? (все)? ?напоминания.*")
    }

    override fun hasGroup(input: String): Boolean {
        return input.matches(".*добавь группу.*")
    }

    override fun clearGroup(input: String): String {
        val sb = StringBuilder()
        val parts: Array<String> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        var st = false
        for (s in parts) {
            if (s.matches(".*групп.*")) {
                st = true
                continue
            }
            if (st) {
                sb.append(s)
                sb.append(" ")
            }
        }
        return sb.toString().trim { it <= ' ' }
    }

    override fun hasToday(input: String): Boolean {
        return input.matches(".*сегодн.*")
    }

    override fun hasAfterTomorrow(input: String): Boolean {
        return input.matches(".*послезавтр.*")
    }

    protected override val afterTomorrow: String
        protected get() = "послезавтра"

    override fun hasHours(input: String?): Int {
        return if (input!!.matches(".*час.*")) 1 else -1
    }

    override fun hasMinutes(input: String?): Int {
        return if (input!!.matches(".*минуту?.*")) 1 else -1
    }

    override fun hasSeconds(input: String?): Boolean {
        return input!!.matches(".*секунд.*")
    }

    override fun hasDays(input: String?): Boolean {
        return input!!.matches(".*дня.*") || input.matches(".*дней.*") || input.matches(".*день.*")
    }

    override fun hasWeeks(input: String?): Boolean {
        return input!!.matches(".*недел.*")
    }

    override fun hasMonth(input: String?): Boolean {
        return input!!.matches(".*месяц.*")
    }

    override fun hasAnswer(input: String): Boolean {
        var input = input
        input = " $input "
        return input.matches(".* (да|нет) .*")
    }

    override fun getDate(input: String, res: com.backdoor.engine.misc.LongInternal): String? {
        var mills: Long = 0
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            val month = getMonth(part)
            if (month != -1) {
                var integer: Int
                try {
                    integer = parts[i - 1]!!.toInt()
                    parts[i - 1] = ""
                } catch (e: NumberFormatException) {
                    integer = 1
                } catch (e: IndexOutOfBoundsException) {
                    integer = 1
                }
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                calendar[Calendar.MONTH] = month
                calendar[Calendar.DAY_OF_MONTH] = integer
                mills = calendar.timeInMillis
                parts[i] = ""
                break
            }
        }
        res.set(mills)
        return clipStrings(parts)
    }

    override fun getAnswer(input: String): Action? {
        return if (input.matches(".* ?да ?.*")) {
            Action.YES
        } else Action.NO
    }

    override fun findFloat(input: String?): Float {
        if (input!!.contains("полтор")) {
            return 1.5f
        }
        if (input.contains("половин") || input.contains("пол")) {
            println("findFloat: $input")
            return 0.5f
        }
        return (-1).toFloat()
    }

    override fun clearFloats(input: String?): String? {
        var input = input
        if (input!!.contains("с половиной")) {
            input = input.replace("с половиной", "")
        }
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val s = parts[i]
            if (s!!.contains("полтор") || s.matches("половин*.")) {
                parts[i] = ""
            }
        }
        input = clipStrings(parts)
        if (input.contains(" пол")) {
            input = input.replace("пол", "")
        }
        return input
    }

    override fun findNumber(input: String?): Float {
        var number = -1f
        if (input!!.matches("ноль")) number = 0f
        if (input.matches("один") || input.matches("одну") || input.matches("одна")) number = 1f
        if (input.matches("два") || input.matches("две")) number = 2f
        if (input.matches("три")) number = 3f
        if (input.matches("четыре")) number = 4f
        if (input.matches("пять")) number = 5f
        if (input.matches("шесть")) number = 6f
        if (input.matches("семь")) number = 7f
        if (input.matches("восемь")) number = 8f
        if (input.matches("девять")) number = 9f
        if (input.matches("десять")) number = 10f
        if (input.matches("одиннадцать")) number = 11f
        if (input.matches("двенадцать")) number = 12f
        if (input.matches("тринадцать")) number = 13f
        if (input.matches("четырнадцать")) number = 14f
        if (input.matches("пятнадцать")) number = 15f
        if (input.matches("шестнадцать")) number = 16f
        if (input.matches("семнадцать")) number = 17f
        if (input.matches("восемнадцать")) number = 18f
        if (input.matches("девятнадцать")) number = 19f
        if (input.matches("двадцать")) number = 20f
        if (input.matches("тридцать")) number = 30f
        if (input.matches("сорок")) number = 40f
        if (input.matches("пятьдесят")) number = 50f
        if (input.matches("шестьдесят")) number = 60f
        if (input.matches("семьдесят")) number = 70f
        if (input.matches("восемьдесят")) number = 80f
        if (input.matches("девяносто")) number = 90f
        if (input.matches("первого")) number = 1f
        if (input.matches("второго")) number = 2f
        if (input.matches("третьего")) number = 3f
        if (input.matches("четвертого")) number = 4f
        if (input.matches("пятого")) number = 5f
        if (input.matches("шестого")) number = 6f
        if (input.matches("седьмого")) number = 7f
        if (input.matches("восьмого")) number = 8f
        if (input.matches("девятого")) number = 9f
        if (input.matches("десятого")) number = 10f
        if (input.matches("одиннадцатого")) number = 11f
        if (input.matches("двенадцатого")) number = 12f
        if (input.matches("тринадцатого")) number = 13f
        if (input.matches("четырнадцатого")) number = 14f
        if (input.matches("пятнадцатого")) number = 15f
        if (input.matches("шестнадцатого")) number = 16f
        if (input.matches("семнадцатого")) number = 17f
        if (input.matches("восемнадцатого")) number = 18f
        if (input.matches("девятнадцатого")) number = 19f
        if (input.matches("двадцатого")) number = 20f
        if (input.matches("тридцатого")) number = 30f
        if (input.matches("сорокового")) number = 40f
        if (input.matches("пятидесятого")) number = 50f
        if (input.matches("шестидесятого")) number = 60f
        if (input.matches("семидесятого")) number = 70f
        if (input.matches("восьмидесятого")) number = 80f
        if (input.matches("девяностого")) number = 90f
        return number
    }

    override fun hasShowAction(input: String): Boolean {
        return input.matches(".*пока(зать|жы?)?.*")
    }

    override fun getShowAction(input: String): Action? {
        if (input.matches(".*рожден.*")) {
            return Action.BIRTHDAYS
        } else if (input.matches(".*активные напомин.*")) {
            return Action.ACTIVE_REMINDERS
        } else if (input.matches(".*напомин.*")) {
            return Action.REMINDERS
        } else if (input.matches(".*события.*")) {
            return Action.EVENTS
        } else if (input.matches(".*заметки.*")) {
            return Action.NOTES
        } else if (input.matches(".*группы.*")) {
            return Action.GROUPS
        } else if (input.matches(".*списо?ки? покуп.*")) {
            return Action.SHOP_LISTS
        }
        return null
    }

    override fun hasNextModifier(input: String): Boolean {
        return input.matches(".*следу.*")
    }
}