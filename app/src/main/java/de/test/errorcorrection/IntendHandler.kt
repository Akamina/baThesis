package de.test.errorcorrection

object IntendHandler {

    /**
     * This function determines the command that will be performed e.g. create or edit etc.
     * @param text user input to get command from
     * @return type of command (create, edit, delete, read)
     */
    private fun getCommand(text: String): String {
        //Create appointment
        //Create reminder
        //Create list
        //Edit appointment
        //Edit reminder
        //Edit list
        //Delete appointment
        //Delete reminder
        //Delete list
        //Read appointment
        //Read reminder
        //Read list
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
        if (text.contains("Termin")) {
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

        when (command) {
            "create" -> when (target) {
                "appointment" -> Appointment.createAppointment(mainActivity)//println("$command $target")
                "reminder" -> println("$command $target")
                "list" -> println("$command $target")
                else -> { println("Ziel nicht verstanden")}
            }
            "edit" -> when (target) {
                "appointment" -> println("$command $target")
                "reminder" -> println("$command $target")
                "list" -> println("$command $target")
                else -> { println("Ziel nicht verstanden")}
            }
            "delete" -> when (target) {
                "appointment" -> println("$command $target")
                "reminder" -> println("$command $target")
                "list" -> println("$command $target")
                else -> { println("Ziel nicht verstanden")}
            }
            "read" -> when (target) {
                "appointment" -> println("$command $target")
                "reminder" -> println("$command $target")
                "list" -> println("$command $target")
                else -> { println("Ziel nicht verstanden")}
            }
            else -> { println("Kommando nicht verstanden")}
        }
    }
}