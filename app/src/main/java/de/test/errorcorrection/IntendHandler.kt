package de.test.errorcorrection

class IntendHandler {

    /**
     * This function determines the command that will be performed e.g. create or edit etc.
     * @param text user input to get command from
     * @return type of command (create, edit, delete, read)
     */
    private fun getCommand(text: String): String {
        //TODO: add lists of words and synonyms to compare here
        if (text.contains("erstell")) {
            //erinnere mich soll auch diesen Fall auslösen
            return "create"
        }
        if (text.contains("lösche")) {
            return "delete"
        }
        if (text.contains("bearbeite")) {
            return "edit"
        }
        if (text.contains("lies")) {
            return "read"
        }
        return "error"
    }


    /**
     * This function determines the target of a command. Appointment or reminder or list
     * @param text user input to get target from
     * @return target type (appointment, reminder, list)
     */
    //TODO: compare index of reminder and appointment so create an appointment with the name reminder is still an appointment and not a reminder
    private fun getTarget(text: String): String {
        if (text.contains("Liste")) {
            return "list"
        }
        if (text.contains("Erinnerung")) {
            return "reminder"
        }
        if (text.contains("Termin") || text.contains("termin")) {
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
        val tmp = Appointment()

        when (command) {
            "create" -> when (target) {
                //"appointment" -> tmp.createAppointment(mainActivity)//println("$command $target")
                "appointment" -> mainActivity.appntmnt.askName(mainActivity)//println("$command $target")
                "reminder" -> println("$command $target")
                "list" -> println("$command $target")
                else -> {
                    println("Ziel nicht verstanden")
                    mainActivity.askUser("Das habe ich leider nicht verstanden", mainActivity, MainActivity.REQUEST_CODE_STT_NOTIFY)
                }
            }
            "edit" -> when (target) {
                //"appointment" -> println("$command $target")
                "appointment" -> mainActivity.appntmnt.askAppointmentEdit(mainActivity)//println("$command $target")
                "reminder" -> println("$command $target")
                "list" -> println("$command $target")
                else -> {
                    println("Ziel nicht verstanden")
                    mainActivity.askUser("Das habe ich leider nicht verstanden", mainActivity, MainActivity.REQUEST_CODE_STT_NOTIFY)
                }
            }
            "delete" -> when (target) {
                //"appointment" -> println("$command $target")
                "appointment" -> mainActivity.appntmnt.askAppointmentDelete(mainActivity)//println("$command $target")
                "reminder" -> println("$command $target")
                "list" -> println("$command $target")
                else -> {
                    println("Ziel nicht verstanden")
                    mainActivity.askUser("Das habe ich leider nicht verstanden", mainActivity, MainActivity.REQUEST_CODE_STT_NOTIFY)
                }
            }
            "read" -> when (target) {
                //"appointment" -> println("$command $target")
                //"appointment" -> mainActivity.appntmnt.readAppointment(mainActivity)//println("$command $target")
                "appointment" -> mainActivity.askUser("Wie lautet der Name des Termins den ich vorlesen soll?", mainActivity, MainActivity.REQUEST_CODE_STT_READ_APPOINTMENT_NO_NAME)
//mainActivity.appntmnt.readAppointment(mainActivity)//println("$command $target")
                "reminder" -> println("$command $target")
                "list" -> println("$command $target")
                else -> {
                    println("Ziel nicht verstanden")
                    mainActivity.askUser("Das habe ich leider nicht verstanden", mainActivity, MainActivity.REQUEST_CODE_STT_NOTIFY)
                }
            }
            else -> {
                println("Kommando nicht verstanden")
                mainActivity.askUser("Das habe ich leider nicht verstanden", mainActivity, MainActivity.REQUEST_CODE_STT_NOTIFY)
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
        if (text.contains("lies")) return "read"

        return "error"
    }

    /**
     * This function determines if the field to change is "location"
     * @param text User input
     * @return
     */
    private fun isLocation(text: String): Boolean {
        if (text.contains("Ort") || text.contains("ort") || text.contains("location") || text.contains(
                "örtlichkeit"
            ) || text.contains("Örtlichkeit")
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
            )
        ) return true

        return false
    }

    /**
     * This function determines if the field to change is "date"
     * @param text User input
     * @return
     */
    private fun isDate(text: String): Boolean {
        if (text.contains("Datum") || text.contains("datum")) return true

        return false
    }

    /**
     * This function determines if the field to change is "name"
     * @param text User input
     * @return
     */
    private fun isName(text: String): Boolean {
        if (text.contains("Name") || text.contains("name") || text.contains("Titel") || text.contains(
                "titel"
            ) || text.contains("Bezeichnung") || text.contains("bezeichnung")
        ) return true

        return false
    }
}