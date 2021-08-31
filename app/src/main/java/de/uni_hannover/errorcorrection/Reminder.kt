package de.uni_hannover.errorcorrection

import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import java.util.*
import android.provider.CalendarContract.Reminders
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit
import kotlin.random.Random


class Reminder {
    private var reminderName: String = ""
    private var reminderDate: String = ""
    private var reminderTime: String = ""
    private var field: String = ""
    private var reminderID: Long = 0
    private val errorName = mutableListOf("lunch", "dinner", "dentist", "doctor")
    private val errorDate =
        mutableListOf("20th of October 2021")//LocalDate.of(2020, 10, 20), LocalDate.of(2021, 10, 2))
    private val errorTime =
        mutableListOf("5 a.m.")//LocalTime.of(12, 45), LocalTime.of(11, 33), LocalTime.of(15, 5), LocalTime.of(22, 45))


    /**
     * This function creates an reminder event in the calendar and sets up the reminder
     * @param mainActivity Context
     */
    internal fun createReminder(mainActivity: MainActivity) {
        println("adding reminder")

        //Initialize event
        val event = ContentValues()
        //Select default calendar
        event.put(CalendarContract.Events.CALENDAR_ID, 1)

        //var dateTime = mainActivity.appntmnt.getDateTimeFromString(reminderDate, reminderTime)
        var dateTime: LocalDateTime

        val tm =
            mainActivity.appntmnt.getDateTimeFromString(reminderDate, reminderTime)
                .toInstant(OffsetDateTime.now().offset).toEpochMilli()

        //Add data and details to event
        event.put(CalendarContract.Events.TITLE, reminderName)
        event.put(CalendarContract.Events.DTSTART, tm)
        event.put(CalendarContract.Events.DTEND, tm + 3600000) //Duration is 1 hour
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        //event.put(CalendarContract.Events.HAS_ALARM, 1)
        event.put(CalendarContract.Events.HAS_ALARM, 1)

        println(event)
        val baseUri = mainActivity.appntmnt.getCalendarUriBase()

        //Insert event into calendar
        mainActivity.contentResolver.insert(baseUri, event)

        reminderID = mainActivity.appntmnt.listSelectedCalendars(reminderName, mainActivity)

        println("added reminder")
        //Maybe redundant
        dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(tm),
            DateTimeUtils.toZoneId(TimeZone.getDefault())
        )
        //Set reminder for event
        var values = ContentValues().apply {
            put(Reminders.MINUTES, 0)
            put(Reminders.EVENT_ID, reminderID)
            put(Reminders.METHOD, Reminders.METHOD_ALERT)

        }
        //Add reminder to event
        mainActivity.contentResolver.insert(Reminders.CONTENT_URI, values)

        //Notify user about creation
        mainActivity.askUser(
            //"Die Erinnerung $reminderName am ${dateTime.dayOfMonth}.${dateTime.month} ${dateTime.year} um ${dateTime.hour}:${dateTime.minute} Uhr wurde erstellt.",
            //  "Die Erinnerung $reminderName am ${dateTime.dayOfMonth}.${dateTime.month} ${dateTime.year} um ${
            //    dateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES)
            //} wurde erstellt.",
            "The reminder $reminderName on ${dateTime.dayOfMonth}. ${dateTime.month} ${dateTime.year} at ${
                dateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES)
            } was created.",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_NOTIFY
        )
        resetParameters()
    }

    /**
     * This function checks if the reminder exists and if it exists, this function deletes it.
     * @param mainActivity Context
     * @param recognizedText Name of the reminder that has to be deleted
     */
    fun deleteReminder(mainActivity: MainActivity, recognizedText: String) {
        var eventID = mainActivity.appntmnt.listSelectedCalendars(recognizedText, mainActivity)

        var tmp = 0
        //Check for valid eventID
        if (eventID == tmp.toLong()) {
            mainActivity.askUser(
                //"Die Erinnerung $recognizedText konnte nicht gefunden werden.",
                "The $recognizedText reminder could not be found.",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
            return
        }

        var iNumRowsDeleted: Int

        val eventUri = ContentUris
            .withAppendedId(mainActivity.appntmnt.getCalendarUriBase(), eventID)
        iNumRowsDeleted = mainActivity.contentResolver.delete(eventUri, null, null)

        println("Rows deleted: $iNumRowsDeleted")
        mainActivity.askUser(
            //"Die Erinnerung $recognizedText wurde entfernt",
            "The $recognizedText reminder has been removed",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_NOTIFY
        )
    }

    /**
     * This function checks if the reminder exists and reads it out.
     * @param mainActivity Context
     * @param text Name of the reminder
     */
    fun readReminder(mainActivity: MainActivity, text: String) {
        if (reminderID == 0.toLong()) {
            mainActivity.askUser(
                //"Die Erinnerung $text konnte nicht gefunden werden",
                "The reminder $text could not be found",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
        } else {
            val eventUri: Uri = mainActivity.appntmnt.getCalendarUriBase()
            //Create array of id, title, start time and location
            val projection = arrayOf("_id", "title", "dtstart")
            val cursor: Cursor? = mainActivity.contentResolver.query(
                eventUri, null, null, null,
                null
            )
            if (cursor!!.moveToFirst()) {
                var calName: String
                var calID: String
                var calDate: String
                val nameCol: Int = cursor.getColumnIndex(projection[1])
                val idCol: Int = cursor.getColumnIndex(projection[0])
                val dateCol: Int = cursor.getColumnIndex(projection[2])

                //Get all events with matching name
                do {
                    //TODO write this into global variables so it can be moved into a seperate function

                    calName = cursor.getString(nameCol)
                    calID = cursor.getString(idCol)
                    calDate = cursor.getString(dateCol)

                    if (calID != null && calID.toLong() == reminderID) {
                        break
                        //result = calID.toLong()
                    }
                } while (cursor.moveToNext())
                cursor.close()
                val localDate =
                    LocalDateTime.ofEpochSecond(
                        calDate.toLong() / 1000,
                        0,
                        OffsetDateTime.now().offset
                    )
                //mainActivity.askUser("Der Name des Termins lautet $calName und er findet am ${localDate.toLocalDate().dayOfMonth}. ${localDate.toLocalDate().month} ${localDate.toLocalDate().year} um ${localDate.toLocalTime().hour}:${localDate.toLocalTime().minute} Uhr $calLocation statt", mainActivity, MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD)
                mainActivity.askUser(
                    //"Ich erinnere dich am ${localDate.toLocalDate()} um ${localDate.toLocalTime()} an $calName.",
                    "I remind you of ${localDate.toLocalDate()} at ${localDate.toLocalTime()} of $calName.",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_NOTIFY
                )
            }
        }
    }

    /**
     * This function checks if the reminder exists and if it exists, this function continues the dialogue.
     * @param recognizedText Name of the reminder
     * @param mainActivity Context
     */
    fun startEdit(recognizedText: String, mainActivity: MainActivity) {
        reminderID = mainActivity.appntmnt.listSelectedCalendars(recognizedText, mainActivity)
        println("Event ID: $reminderID")
        var tmp = 0
        if (reminderID == tmp.toLong()) {
            mainActivity.askUser(
                //"Die Erinnerung $recognizedText konnte nicht gefunden werden.",
                "The $recognizedText reminder could not be found.",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
        } else {
            //Ask for field
            mainActivity.askUser(
                //"Was soll geändert werden?",
                "What should be changed?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_FIELD
            )
        }
    }

    /**
     * This function determines if the reminder has to be edited or read out
     * @param recognizedText Field that has to be edited/ read the reminder
     * @param mainActivity Context
     */
    fun continueEdit(recognizedText: String, mainActivity: MainActivity) {
        field = mainActivity.handler.getField(recognizedText, mainActivity)
        when (field) {
            "error" -> {
                mainActivity.askUser(
                    //"Was soll geändert werden?",
                    "What should be changed?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_FIELD
                )
            }
            "read" -> {
                readReminderEdit(mainActivity)
            }
            else -> {
                mainActivity.askUser(
                    //"Wie lautet die Änderung?",
                    "What's the change?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_NEW
                )
            }
        }
    }

    /**
     * This function edits given reminder with the new content
     * @param text New content for field
     * @param mainActivity Context
     */
    fun editReminder(text: String, mainActivity: MainActivity) {
        //TODO check name, time and date in console maybe have to fix some bugs
        var event = mainActivity.appntmnt.getCalendarUriBase()
        var newEvent = ContentValues()
        var updateUri = ContentUris.withAppendedId(event, reminderID)
        var date: Long
        var localDateTime: LocalDateTime
        when (field) {
            "name" -> {
                newEvent.put(CalendarContract.Events.TITLE, text)
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    //"Ich habe den Namen auf $text geändert. Möchtest du noch etwas ändern?",
                    "I changed the name to $text. Would you like to change anything else?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_ASK
                )
            }
            "date" -> {
                //TODO add time from date here to localDateTime
                //get date from event
                date = mainActivity.appntmnt.getDateFromEvent(mainActivity, event)
                //Calculate into LocalDateTime from millis
                localDateTime =
                    LocalDateTime.ofEpochSecond(date / 1000, 0, OffsetDateTime.now().offset)
                var localDate = mainActivity.appntmnt.parseLocalDate(text)
                //Update date
                localDateTime = localDateTime.withYear(localDate.year)
                localDateTime = localDateTime.withMonth(localDate.monthValue)
                localDateTime = localDateTime.withDayOfMonth(localDate.dayOfMonth)

                newEvent.put(
                    CalendarContract.Events.DTSTART,
                    localDateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli()
                )
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    //"Ich habe das Datum auf $localDate geändert. Möchtest du noch etwas ändern?",
                    "I changed the date to $localDate. Would you like to change anything else?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_ASK
                )

            }
            "time" -> {
                //TODO wenn zeit gesetzt wird, wird das datum auf 1970 gesetzt
                //get time from event
                date = mainActivity.appntmnt.getDateFromEvent(mainActivity, event)

                //Calculate into LocalDateTime from Millis
                localDateTime = //maybe ignore this here and edit it directly in date
                    LocalDateTime.ofEpochSecond(date / 1000, 0, OffsetDateTime.now().offset)
                var localTime = mainActivity.appntmnt.parseLocalTime(text)
                //Update time
                localDateTime = localDateTime.withHour(localTime.hour)
                localDateTime = localDateTime.withMinute(localTime.minute)

                newEvent.put(
                    CalendarContract.Events.DTSTART,
                    localDateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli()
                )
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    //"Ich habe die Zeit auf $localTime geändert. Möchtest du noch etwas ändern?",
                    "I changed the time to $localTime. Would you like to change anything?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_ASK
                )

            }
            else -> {
                //TODO add notify for invalid field

            }
        }
    }

    /**
     * This function reads given reminder during edit dialogue
     * @param mainActivity Context
     */
    fun readReminderEdit(mainActivity: MainActivity) {
        println("lies die erinnerung beim bearbeiten")
        if (reminderID == 0.toLong()) {
            mainActivity.askUser(
                //"Wie lautet der Name der Erinnerung den ich vorlesen soll?",
                "What is the name of the memory that I should read?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_READ
            )
        } else {
            val eventUri: Uri = mainActivity.appntmnt.getCalendarUriBase()
            //Create array of id, title, start time and location
            val projection = arrayOf("_id", "title", "dtstart")
            val cursor: Cursor? = mainActivity.contentResolver.query(
                eventUri, null, null, null,
                null
            )
            if (cursor!!.moveToFirst()) {
                var calName: String
                var calID: String
                var calDate: String
                val nameCol: Int = cursor.getColumnIndex(projection[1])
                val idCol: Int = cursor.getColumnIndex(projection[0])
                val dateCol: Int = cursor.getColumnIndex(projection[2])

                //Get all events with matching name
                do {
                    //TODO write this into global variables so it can be moved into a seperate function

                    calName = cursor.getString(nameCol)
                    calID = cursor.getString(idCol)
                    calDate = cursor.getString(dateCol)

                    if (calID != null && calID.toLong() == reminderID) {
                        break
                    }
                } while (cursor.moveToNext())
                cursor.close()
                val localDate =
                    LocalDateTime.ofEpochSecond(
                        calDate.toLong() / 1000,
                        0,
                        OffsetDateTime.now().offset
                    )
                mainActivity.askUser(
                    //"Ich erinnere dich am ${localDate.toLocalDate()} um ${localDate.toLocalTime()} an $calName. Möchtest du noch etwas ändern?",
                    "I remind you of ${localDate.toLocalDate()} at ${localDate.toLocalTime()} of $calName. Would you like to change something?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_FIELD
                )
            }
        }
    }

    internal fun updateParameter(mainActivity: MainActivity, text: String) {
        when (field) {
            "name" -> {
                this.reminderName = text
            }
            "date" -> {
                this.reminderDate = text
            }
            "time" -> {
                this.reminderTime = text
            }
            "location" -> {
                mainActivity.askUser("There is no field named location for a reminder. Do you want do edit something?", mainActivity, MainActivity.REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION_END)
                return
            }
            "read" -> {
                readData(mainActivity)
                return
            }
        }
        if (this.reminderDate == "") {
            mainActivity.askUser(
                "What date is the appointment?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_REMINDER_DATE
            )
            return
        }
        if (this.reminderTime == "") {
            mainActivity.askUser(
                "What time is the appointment?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_REMINDER_TIME
            )
            return
        }
        mainActivity.askUser("I have updated $field to $text. Do you wish to edit something else?", mainActivity, MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_CREATION_END)
    }


    fun setName(recognizedText: String) {
        this.reminderName = recognizedText
    }

    fun setTime(recognizedText: String) {
        this.reminderTime = recognizedText
    }

    fun setDate(recognizedText: String) {
        this.reminderDate = recognizedText
    }

    fun setEvent(listSelectedCalendars: Long) {
        this.reminderID = listSelectedCalendars
    }

    fun getField(): String {
        return this.field
    }

    fun setField(text: String) {
        this.field = text
    }

    /**
     * TODO
     *
     * @param mainActivity
     */
    fun readData(mainActivity: MainActivity) {
        mainActivity.askUser(
            "The name is $reminderName, the date is $reminderDate and the time is $reminderTime. Do you wish to edit something?",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_CREATION_END
        )

    }

    /**
     * TODO
     *
     */
    private fun resetParameters() {
        this.reminderName = ""
        this.reminderDate = ""
        this.reminderTime = ""
    }

    /**
     * TODO
     *
     */
    internal fun addError() {
        var rng = Random
        println(rng.nextInt(1))
        var index = rng.nextInt(4)
        when (index) {
            0 -> {
                reminderName = errorName[rng.nextInt(errorName.size)]
            }
            1 -> {
                reminderDate = errorDate[rng.nextInt(errorDate.size)]
            }
            2 -> {
                reminderTime = errorTime[rng.nextInt(errorTime.size)]

            }
        }
    }


}