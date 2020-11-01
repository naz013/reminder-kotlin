package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import java.util.*
import java.util.regex.Pattern

internal class UkWorker : Worker() {
    protected override val weekdays: Array<String>
        protected get() = arrayOf(" неділ", "понеділ", "вівтор", "середу?а?и?", "четвер", "п'ятниц", "субот")

    override fun hasCalendar(input: String): Boolean {
        return input.matches(".*календар.*")
    }

    override fun clearCalendar(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (part!!.matches(".*календар.*")) {
                parts[i] = ""
                if (i > 0 && parts[i - 1]!!.toLowerCase().equals("до", ignoreCase = true)) {
                    parts[i - 1] = ""
                }
                break
            }
        }
        return clipStrings(parts)
    }

    override fun getWeekDays(input: String): List<Int> {
        val array = IntArray(7)
        val parts: Array<String> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        val weekDays = weekdays
        for (part in parts) {
            for (i in weekDays.indices) {
                val day = ".*" + weekDays[i] + ".*"
                if (part.matches(day)) {
                    array[i] = 1
                    break
                }
            }
        }
        val list: MutableList<Int> = ArrayList()
        for (anArray in array) {
            list.add(anArray)
        }
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
            if (!part.matches("в")) {
                sb.append(" ").append(part)
            }
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
                } catch (e: NumberFormatException) {
                }
                parts[i] = ""
                break
            }
        }
        return clipStrings(parts)
    }

    override fun hasRepeat(input: String?): Boolean {
        return input!!.matches(".*кожн.*") || hasEveryDay(input)
    }

    override fun hasEveryDay(input: String?): Boolean {
        return input!!.matches(".*щоден.*") || input.matches(".*щодня.*")
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
            if (isStart) {
                sb.append(" ").append(part)
            }
            if (part.matches("текст(ом)?")) {
                isStart = true
            }
        }
        return sb.toString().trim { it <= ' ' }
    }

    override fun clearMessage(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (part!!.matches("текст(ом)?")) {
                try {
                    if (parts[i - 1]!!.matches("з")) {
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
        if (input!!.matches(".*повідомлення.*")) {
            return Action.MESSAGE
        }
        return if (input.matches(".*листа?.*")) {
            Action.MAIL
        } else null
    }

    override fun clearMessageType(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val type = getMessageType(parts[i])
            if (type != null) {
                parts[i] = ""
                break
            }
        }
        return clipStrings(parts)
    }

    override fun getAmpm(input: String?): Ampm? {
        if (input!!.matches(".*з?ран(ку|о)?.*") || input.matches(".*вранці.*")) {
            return Ampm.MORNING
        }
        if (input.matches(".*в?веч(о|е)р.*")) {
            return Ampm.EVENING
        }
        if (input.matches(".*в?день.*")) {
            return Ampm.NOON
        }
        return if (input.matches(".*в?ночі.*")) {
            Ampm.NIGHT
        } else null
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
                    if (date != null) {
                        return date
                    }
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
                } catch (ignored: Exception) {
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
            if (!part.matches("об?")) {
                sb.append(" ").append(part)
            }
        }
        return sb.toString().trim { it <= ' ' }
    }

    override fun getMonth(input: String?): Int {
        var res = -1
        if (input!!.contains("січень") || input.contains("січня")) {
            res = 0
        }
        if (input.contains("лютий") || input.contains("лютого")) {
            res = 1
        }
        if (input.contains("березень") || input.contains("березня")) {
            res = 2
        }
        if (input.contains("квітень") || input.contains("квітня")) {
            res = 3
        }
        if (input.contains("травень") || input.contains("травня")) {
            res = 4
        }
        if (input.contains("червень") || input.contains("червня")) {
            res = 5
        }
        if (input.contains("липень") || input.contains("липня")) {
            res = 6
        }
        if (input.contains("серпень") || input.contains("серпня")) {
            res = 7
        }
        if (input.contains("вересень") || input.contains("вересня")) {
            res = 8
        }
        if (input.contains("жовтень") || input.contains("жовтня")) {
            res = 9
        }
        if (input.contains("листопад") || input.contains("листопада")) {
            res = 10
        }
        if (input.contains("грудень") || input.contains("грудня")) {
            res = 11
        }
        return res
    }

    override fun hasCall(input: String?): Boolean {
        return input!!.matches(".*дзвонити.*")
    }

    override fun clearCall(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (part!!.matches(".*дзвонити.*")) {
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
        return input!!.matches(".*надісл.*")
    }

    override fun clearSender(input: String): String? {
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val part = parts[i]
            if (part!!.matches(".*надісл.*")) {
                parts[i] = ""
                break
            }
        }
        return clipStrings(parts)
    }

    override fun hasNote(input: String): Boolean {
        return input.contains("нотатка")
    }

    override fun clearNote(input: String): String {
        var input = input
        input = input.replace("нотатка", "")
        return input.trim { it <= ' ' }
    }

    override fun hasAction(input: String): Boolean {
        return (input.startsWith("відкрити") || input.matches(".*допом.*")
                || input.matches(".*гучніст.*") || input.matches(".*налаштув.*")
                || input.matches(".*повідомити.*"))
    }

    override fun getAction(input: String): Action? {
        return if (input.matches(".*допомог.*")) {
            Action.HELP
        } else if (input.matches(".*гучніст.*")) {
            Action.VOLUME
        } else if (input.matches(".*налаштування.*")) {
            Action.SETTINGS
        } else if (input.matches(".*повідомити.*")) {
            Action.REPORT
        } else Action.APP
    }

    override fun hasEvent(input: String): Boolean {
        return input.startsWith("додати") || input.matches("нове?и?й?.*")
    }

    override fun hasEmptyTrash(input: String): Boolean {
        return input.matches(".*очисти(ти)? кошик.*")
    }

    override fun hasDisableReminders(input: String): Boolean {
        return input.matches(".*вимкн(и|ути)? (всі)? ?нагадування.*")
    }

    override fun hasGroup(input: String): Boolean {
        return input.matches(".*дода(ти|й)? групу.*")
    }

    override fun clearGroup(input: String): String {
        val sb = StringBuilder()
        val parts: Array<String> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        var st = false
        for (s in parts) {
            if (s.matches(".*групу.*")) {
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

    override fun getEvent(input: String): Action? {
        return if (input.matches(".*день народжен.*")) {
            Action.BIRTHDAY
        } else if (input.matches(".*нагадуван.*")) {
            Action.REMINDER
        } else {
            Action.NO_EVENT
        }
    }

    override fun hasToday(input: String): Boolean {
        return input.matches(".*сьогодн.*")
    }

    override fun hasAfterTomorrow(input: String): Boolean {
        return input.matches(".*післязавтр.*")
    }

    protected override val afterTomorrow: String
        protected get() = "післязавтра"

    override fun hasHours(input: String?): Int {
        return if (input!!.matches(".*годині?у?.*")) {
            1
        } else -1
    }

    override fun hasMinutes(input: String?): Int {
        return if (input!!.matches(".*хвилин.*")) {
            1
        } else -1
    }

    override fun hasSeconds(input: String?): Boolean {
        return input!!.matches(".*секунд.*")
    }

    override fun hasDays(input: String?): Boolean {
        return input!!.matches(".*дні.*") || input.matches(".*день.*") || input.matches(".*дня.*")
    }

    override fun hasWeeks(input: String?): Boolean {
        return input!!.matches(".*тиждень.*") || input.matches(".*тижні.*")
    }

    override fun hasMonth(input: String?): Boolean {
        return input!!.matches(".*місяц.*")
    }

    override fun hasAnswer(input: String): Boolean {
        var input = input
        input = " $input "
        return input.matches(".* (так|ні) .*")
    }

    override fun getAnswer(input: String): Action? {
        return if (input.matches(".* ?так ?.*")) {
            Action.YES
        } else Action.NO
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

    override fun findFloat(input: String?): Float {
        if (input!!.contains("півтор")) {
            return 1.5f
        }
        return if (input.contains("половин") || input.contains("пів")) {
            0.5f
        } else (-1).toFloat()
    }

    override fun clearFloats(input: String?): String? {
        var input = input
        if (input!!.contains("з половиною")) {
            input = input.replace("з половиною", "")
        }
        val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
        for (i in parts.indices) {
            val s = parts[i]
            if (s!!.contains("півтор") || s.matches("половин*.")) {
                parts[i] = ""
            }
        }
        input = clipStrings(parts)
        if (input.contains(" пів")) {
            input = input.replace("пів", "")
        }
        return input
    }

    override fun findNumber(input: String?): Float {
        var number = -1
        if (input!!.matches("нуль")) {
            number = 0
        }
        if (input.matches("один") || input.matches("одну") || input.matches("одна")) {
            number = 1
        }
        if (input.matches("два") || input.matches("дві")) {
            number = 2
        }
        if (input.matches("три")) {
            number = 3
        }
        if (input.matches("чотири")) {
            number = 4
        }
        if (input.matches("п'ять")) {
            number = 5
        }
        if (input.matches("шість")) {
            number = 6
        }
        if (input.matches("сім")) {
            number = 7
        }
        if (input.matches("вісім")) {
            number = 8
        }
        if (input.matches("дев'ять")) {
            number = 9
        }
        if (input.matches("десять")) {
            number = 10
        }
        if (input.matches("одинадцять")) {
            number = 11
        }
        if (input.matches("дванадцять")) {
            number = 12
        }
        if (input.matches("тринадцять")) {
            number = 13
        }
        if (input.matches("чотирнадцять")) {
            number = 14
        }
        if (input.matches("п'ятнадцять")) {
            number = 15
        }
        if (input.matches("шістнадцять")) {
            number = 16
        }
        if (input.matches("сімнадцять")) {
            number = 17
        }
        if (input.matches("вісімнадцять")) {
            number = 18
        }
        if (input.matches("дев'ятнадцять")) {
            number = 19
        }
        if (input.matches("двадцять")) {
            number = 20
        }
        if (input.matches("тридцять")) {
            number = 30
        }
        if (input.matches("сорок")) {
            number = 40
        }
        if (input.matches("п'ятдесят")) {
            number = 50
        }
        if (input.matches("шістдесят")) {
            number = 60
        }
        if (input.matches("сімдесят")) {
            number = 70
        }
        if (input.matches("вісімдесят")) {
            number = 80
        }
        if (input.matches("дев'яносто")) {
            number = 90
        }
        if (input.matches("першого") || input.matches("першій")) {
            number = 1
        }
        if (input.matches("другого") || input.matches("другій")) {
            number = 2
        }
        if (input.matches("третього") || input.matches("третій")) {
            number = 3
        }
        if (input.matches("четвертого") || input.matches("четвертій")) {
            number = 4
        }
        if (input.matches("п'ятого") || input.matches("п'ятій")) {
            number = 5
        }
        if (input.matches("шостого") || input.matches("шостій")) {
            number = 6
        }
        if (input.matches("сьомого") || input.matches("сьомій")) {
            number = 7
        }
        if (input.matches("восьмого") || input.matches("восьмій")) {
            number = 8
        }
        if (input.matches("дев'ятого") || input.matches("дев'ятій")) {
            number = 9
        }
        if (input.matches("десятого") || input.matches("десятій")) {
            number = 10
        }
        if (input.matches("одинадцятого") || input.matches("одинадцятій")) {
            number = 11
        }
        if (input.matches("дванадцятого") || input.matches("дванадцятій")) {
            number = 12
        }
        if (input.matches("тринадцятого") || input.matches("тринадцятій")) {
            number = 13
        }
        if (input.matches("чотирнадцятого") || input.matches("чотирнадцятій")) {
            number = 14
        }
        if (input.matches("п'ятнадцятого") || input.matches("п'ятнадцятій")) {
            number = 15
        }
        if (input.matches("шістнадцятого") || input.matches("шістнадцятій")) {
            number = 16
        }
        if (input.matches("сімнадцятого") || input.matches("сімнадцятій")) {
            number = 17
        }
        if (input.matches("вісімнадцятого") || input.matches("вісімнадцятій")) {
            number = 18
        }
        if (input.matches("дев'ятнадцятого") || input.matches("дев'ятнадцятій")) {
            number = 19
        }
        if (input.matches("двадцятого") || input.matches("двадцятій")) {
            number = 20
        }
        if (input.matches("тридцятого")) {
            number = 30
        }
        if (input.matches("сорокового")) {
            number = 40
        }
        if (input.matches("п'ятдесятого")) {
            number = 50
        }
        if (input.matches("шістдесятого")) {
            number = 60
        }
        if (input.matches("сімдесятого")) {
            number = 70
        }
        if (input.matches("вісімдесятого")) {
            number = 80
        }
        if (input.matches("дев'яностого")) {
            number = 90
        }
        return number.toFloat()
    }

    override fun hasShowAction(input: String): Boolean {
        return input.matches(".*пока(зати|жи)?.*")
    }

    override fun getShowAction(input: String): Action? {
        if (input.matches(".*д?е?н?і?ь? народжен.*")) {
            return Action.BIRTHDAYS
        } else if (input.matches(".*активні нагадуван.*")) {
            return Action.ACTIVE_REMINDERS
        } else if (input.matches(".*нагадуван.*")) {
            return Action.REMINDERS
        } else if (input.matches(".*події.*")) {
            return Action.EVENTS
        } else if (input.matches(".*нотатки.*")) {
            return Action.NOTES
        } else if (input.matches(".*групи.*")) {
            return Action.GROUPS
        } else if (input.matches(".*списо?ки? покупок.*")) {
            return Action.SHOP_LISTS
        }
        return null
    }

    override fun hasNextModifier(input: String): Boolean {
        return input.matches(".*наступн.*")
    }
}