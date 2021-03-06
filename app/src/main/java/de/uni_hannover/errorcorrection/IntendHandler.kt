package de.uni_hannover.errorcorrection

class IntendHandler {

    /**
     * This function determines the command that will be performed e.g. create or edit etc.
     * @param text user input to get command from
     * @return type of command (create, edit, delete, read)
     */
    private fun getCommand(text: String): String {
        if (text.contains("erstell") || text.contains("create")) {
            //erinnere mich soll auch diesen Fall auslösen
            return "create"
        }
        if (text.contains("lösche") || text.contains("delete")) {
            return "delete"
        }
        if (text.contains("bearbeite") || text.contains("edit")) {
            return "edit"
        }
        if (text.contains("lies") || text.contains("read")) {
            return "read"
        }
        return "error"
    }


    /**
     * This function determines the target of a command. Appointment or reminder or list
     * @param text user input to get target from
     * @return target type (appointment, reminder, list)
     */
    private fun getTarget(text: String): String {
        if (text.contains("Liste") || text.contains("list") || text.contains("List")) {
            return "list"
        }
        if (text.contains("Erinnerung") || text.contains("reminder") || text.contains("Reminder")) {
            return "reminder"
        }
        if (text.contains("Termin") || text.contains("termin") || text.contains("appointment") || text.contains(
                "Appointment"
            )
        ) {
            return "appointment"
        }
        return "error"
    }

    /**
     * This function handles user input and calls functions to perform the users intend
     * @param text user input to handle
     * @param mainActivity activity for context
     */

    internal fun handleInput(text: String, mainActivity: MainActivity) {
        val command = getCommand(text)
        val target = getTarget(text)
        val askUsr = "Unfortunately I didn't understand that"

        //first check for task then for object type
        when (command) {
            "create" -> when (target) {
                "appointment" -> mainActivity.appntmnt.askName(mainActivity)
                "reminder" -> mainActivity.askUser(
                    "What is the name of the reminder?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_NAME
                )
                "list" -> mainActivity.askUser(
                    "What's the name of the list?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_LIST_NAME
                )
                else -> {
                    println("Ziel nicht verstanden")
                    mainActivity.askUser(
                        askUsr,
                        mainActivity,
                        MainActivity.REQUEST_CODE_STT_NOTIFY
                    )
                }
            }
            "edit" -> when (target) {
                "appointment" -> mainActivity.appntmnt.askAppointmentEdit(mainActivity)
                "reminder" -> mainActivity.askUser(
                    "What is the name of the reminder you want to edit?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_EDIT
                )
                "list" -> mainActivity.askUser(
                    "Which list do you want to edit?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_LIST_EDIT
                )
                else -> {
                    println("Ziel nicht verstanden")
                    mainActivity.askUser(
                        askUsr,
                        mainActivity,
                        MainActivity.REQUEST_CODE_STT_NOTIFY
                    )
                }
            }
            "delete" -> when (target) {
                "appointment" -> mainActivity.appntmnt.askAppointmentDelete(mainActivity)
                "reminder" -> mainActivity.askUser(
                    "Which reminder should be deleted?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_DELETE
                )
                "list" -> mainActivity.askUser(
                    "Which list do you want to delete?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_LIST_DELETE
                )
                else -> {
                    println("Ziel nicht verstanden")
                    mainActivity.askUser(
                        askUsr,
                        mainActivity,
                        MainActivity.REQUEST_CODE_STT_NOTIFY
                    )
                }
            }
            "read" -> when (target) {
                "appointment" -> mainActivity.askUser(
                    "What is the name of the appointment I should read out?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_READ_APPOINTMENT_NO_NAME
                )
                "reminder" -> mainActivity.askUser(
                    "What is the name of the reminder that I should read?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_READ
                )
                "list" -> mainActivity.askUser(
                    "Which list should I read out?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_LIST_READ
                )
                else -> {
                    println("Ziel nicht verstanden")
                    mainActivity.askUser(
                        askUsr,
                        mainActivity,
                        MainActivity.REQUEST_CODE_STT_NOTIFY
                    )
                }
            }
            else -> {
                println("Kommando nicht verstanden")
                mainActivity.askUser(
                    askUsr,
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_NOTIFY
                )
            }
        }
    }

    /**
     * This function determines which field has to be edited
     * @param text User input
     * @param mainActivity Context
     */
    internal fun getField(text: String, mainActivity: MainActivity): String {
        //Check name
        if (isName(text)) return "name"
        //Check date
        if (isDate(text)) return "date"
        //Check time
        if (isTime(text)) return "time"
        //Check location
        if (isLocation(text)) return "location"
        //Check for read out
        if (text.contains("lies") || text.contains("read") || text.contains("Read")) return "read"

        return "error"
    }

    /**
     * This function determines the intend for lists
     * @param text User input
     * @return intent
     */
    internal fun getListIntend(text: String): String {
        if (text.contains("lies") || text.contains("read") || text.contains("Read")) return "read"
        if (text.contains("hinzufügen") || text.contains("füge") || text.contains("add") || text.contains(
                "Add"
            ) || text.contains("insert") || text.contains("Insert")
        ) return "add"
        if (text.contains("änder") || text.contains("ersetz") || text.contains("replace") || text.contains(
                "Replace"
            )
        ) return "replace"
        if (text.contains("entfern") || text.contains("lösch") || text.contains("delete") || text.contains(
                "Delete"
            )
        ) return "delete"
        if (text.contains("name") || text.contains("Name")) return "name"
        return "error"
    }

    /**
     * This function determines if the field to change is "location"
     * @param text User input
     * @return
     */
    private fun isLocation(text: String): Boolean {
        if (text.contains("Ort") || text.contains("ort") || text.contains("location") || text.contains(
                "Location"
            ) || text.contains(
                "örtlichkeit"
            ) || text.contains("Örtlichkeit")
            || text.contains("place")
        ) return true

        return false
    }

    /**
     * This function determines if the field to change is "time"
     * @param text User input
     * @return
     */
    private fun isTime(text: String): Boolean {
        if (text.contains("zeit") || text.contains("Zeit") || text.contains("Uhrzeit") || text.contains(
                "uhrzeit"
            ) || text.contains("time") || text.contains("Time")
        ) return true

        return false
    }

    /**
     * This function determines if the field to change is "date"
     * @param text User input
     * @return
     */
    private fun isDate(text: String): Boolean {
        if (text.contains("Date") || text.contains("date")) return true

        return false
    }

    /**
     * This function determines if the field to change is "name"
     * @param text User input
     * @return
     */
    private fun isName(text: String): Boolean {
        if (text.contains("Name") || text.contains("name") || text.contains("Title") || text.contains(
                "title"
            ) || text.contains("Bezeichnung") || text.contains("bezeichnung")
        ) return true

        return false
    }

    /**
     * This function checks if the user wants to edit a field and if the field exists
     * @param text User input
     * @param mainActivity Context
     * @return "error" for no valid field, the name of the field or "continue" if the user does not want to edit
     */
    internal fun editCheck(text: String, mainActivity: MainActivity): String {
        if (text.contains("edit") || text.contains("Edit")) {
            return getField(text, mainActivity)
        }
        return "continue"
    }

    /**
     * This function checks if the user said a field, if no continue is returned
     * @param text User input
     * @param mainActivity Context
     * @return Field name or continue if no field was in the user input
     */
    internal fun editCheckEnd(text: String, mainActivity: MainActivity): String {
        val field = getField(text, mainActivity)
        return if (field == "error") "continue" else field
    }
}