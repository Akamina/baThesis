package de.test.errorcorrection

import android.content.ContentValues
import android.net.Uri
import android.provider.CalendarContract
import org.threeten.bp.format.DateTimeFormatter
import android.os.Build
import org.threeten.bp.*
import java.util.*


class Appointment {

    lateinit private var name: String
    lateinit private var date: String
    lateinit private var time: String
    lateinit private var location: String




    /**
     * This function creates an event and adds it to the default calendar
     * @param mainActivity context for inserting event
     */
    internal fun createAppointment(mainActivity: MainActivity) {


        println("adding event")

        //Initialize event
        val event = ContentValues()

        //Select default calendar
        event.put(CalendarContract.Events.CALENDAR_ID, 1)
        val tm = getDateTimeFromString(date, time).toInstant(OffsetDateTime.now().offset).toEpochMilli()

        //Add data and details to event
        event.put(CalendarContract.Events.TITLE, name)
        event.put(CalendarContract.Events.DTSTART,tm)
        event.put(CalendarContract.Events.DTEND, tm + 3600000) //Duration is 1 hour
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        event.put(CalendarContract.Events.EVENT_LOCATION, location)

        val baseUri: Uri
        baseUri = if (Build.VERSION.SDK_INT >= 8) {
            Uri.parse("content://com.android.calendar/events")
        } else {
            Uri.parse("content://calendar/events")
        }

        //Insert event into calendar
        mainActivity.contentResolver.insert(baseUri, event)

        println("added event")
        //TODO notify user that the given appointment was created

    }

    /**
     * TODO
     * This function starts the creation dialogue for an appointment
     * @param mainActivity context to call function
     */
    internal fun askName(mainActivity: MainActivity) {
        println("Erstelle Termin")
        val s = "Wie lautet der Name des Termins?"
        mainActivity.askUser(s, mainActivity, MainActivity.REQUEST_CODE_STT_NAME)
        Thread.sleep(3000) //do i need this?
    }

    /**
     * This function parses a date and a time from two given strings
     * @param dt String that includes the date
     * @param time String that includes the time
     * @return LocalDateTime
     */
    //private fun getDateTimeFromString (date: String, time: String): Long {
    private fun getDateTimeFromString (dt: String, time: String): LocalDateTime {
        //TODO Fix millis bug so the appointment will be created properly
        var formatter = DateTimeFormatter.ofPattern("dd.MM yyyy")
        //TODO create method to check time format and fix it if it does not match the pattern
        var t = "0"
        //Checking for a missing 0 in front of the time string, if it is missing, add it
        if (time.split(" ")[0][0] =='0') {
            t += time.split(" ")[0]
        } else {
            t = time.split(" ")[0]
        }
        //Checking for correct length
        if (t.length <= 2) {
            t += ":00"
        }

        var localTime = LocalTime.parse(t)
        var dateTime = LocalDateTime.now()

        //Checking for keywords to determine the correct date and returns it
        if (dt.contains("morgen")) {
            //dateTime = dateTime.withDayOfMonth(dateTime.dayOfMonth + 1)
            dateTime = dateTime.plusDays(1)
            dateTime = dateTime.withHour(localTime.hour)
            dateTime = dateTime.withMinute(localTime.minute)
            dateTime = dateTime.withSecond(0)

            return dateTime
        }
        if (dt.contains("heute")) {
            dateTime = dateTime.withHour(localTime.hour)
            dateTime = dateTime.withMinute(localTime.minute)

            return dateTime
        }
        if (dt.contains("übermorgen")) {
            dateTime = dateTime.plusDays(2)
            dateTime = dateTime.withHour(localTime.hour)
            dateTime = dateTime.withMinute(localTime.minute)

            return dateTime
        }

        var dte = dt
        if (dt.contains("am")) dte = dt.split("am ")[1]
        if (dt.contains("an dem")) dte = dt.split("an dem ")[1]

        if (dte.indexOf('.') <= 1 ) dte = "0$dte"

        //TODO insert missing 0 after first '.'
        println(dte)

        //Normal date was named, parsing it into LocalDateTime

        //TODO dtermine which formatter to use, "dd.MM yyyy" or "dd. monat yyyy"
        var d = LocalDate.parse(dte, formatter)
        dateTime = dateTime.withYear(d.year)
        dateTime = dateTime.withMonth(d.monthValue)
        dateTime = dateTime.withDayOfMonth(d.dayOfMonth)
        dateTime = dateTime.withHour(localTime.hour)
        dateTime = dateTime.withMinute(localTime.minute)

        return dateTime
    }

    internal fun setName(name: String) {
        this.name = name
    }

    internal fun setDate(date: String) {
        this.date = date
    }

    internal fun setTime(time: String) {
        this.time = time
    }

    internal fun setLocation(location: String) {
        this.location = location
    }


}