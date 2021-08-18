package de.test.errorcorrection

import android.content.Context
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.lang.Exception

class List : Serializable {
    internal var lists: MutableList<MutableList<String>> =
        mutableListOf<MutableList<String>>()//MutableList<MutableList<String>>
    internal var replaceable: String = ""
    internal var replaceableIndex: Int = -1

    /**
     * This function checks if the name is already taken and notifies the user if it is taken. Otherwise the list will be created and the user gets asked for items.
     * @param mainActivity Context
     * @param text User input
     */
    internal fun createList(mainActivity: MainActivity, text: String) {
        //Check if a list with the given name already exists
        for (item in lists) {
            if (item[0] == text) {
                mainActivity.askUser(
                    "Der Name wird bereits verwendet. Wie soll die Liste heißen?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_LIST_NAME
                )
                return
            }
        }
        mainActivity.countName = 0
        var lst = mutableListOf(text)
        lists.add(lst)
        mainActivity.currentList = lists.indexOf(lst)
        mainActivity.askUser(
            "Ich habe die Liste $text erstellt. Was soll hinzugefügt werden?",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_LIST_CREATE_ITEM
        )
        saveLists<List>(mainActivity)
    }

    /**
     * This function adds an item to the list
     * @param mainActivity Context
     * @param text User input
     */
    internal fun addItem(mainActivity: MainActivity, text: String) {
        lists[mainActivity.currentList].add(text)
        mainActivity.askUser(
            "Ich habe $text der Liste hinzugefügt. Möchtest du noch etwas hinzufügen?",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_LIST_CREATE_ITEM
        )
        saveLists<List>(mainActivity)
    }

    /**
     * This function checks if the list exists and if it exists it gets deleted.
     * @param mainActivity Context
     * @param text User input
     */
    internal fun deleteList(mainActivity: MainActivity, text: String) {
        if (findList(mainActivity, text, MainActivity.REQUEST_CODE_STT_LIST_DELETE)) return
        mainActivity.countName = 0
        lists.removeAt(mainActivity.currentList)
        mainActivity.currentList = -1
        mainActivity.askUser(
            "Die Liste $text wurde entfernt.",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_NOTIFY
        )
        saveLists<List>(mainActivity)
    }

    /**
     * This function reads out a list
     * @param mainActivity Context
     * @param text User input
     * @param requestCode Variable to check if it is during edit or not
     */
    internal fun readList(mainActivity: MainActivity, text: String, requestCode: Int) {
        if (requestCode != MainActivity.REQUEST_CODE_STT_LIST_EDIT_ITEM_READ) {
            if (findList(mainActivity, text, requestCode)) return
            mainActivity.countName = 0
            read(mainActivity, lists[mainActivity.currentList])
        } else {
            read(mainActivity, lists[mainActivity.currentList])
            mainActivity.askUser(
                "Was möchtest du sonst bearbeiten?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_LIST_EDIT_FIELD
            )
        }
    }

    /**
     * This helper function reads the given list
     * @param mainActivity Context
     * @param lst List to read out
     */
    private fun read(mainActivity: MainActivity, lst: MutableList<String>) {
        var i = 0
        mainActivity.askUser(
            "Der Name der Liste lautet ${lst[i]}",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_NOTIFY
        )
        i++
        while (i < lst.size) {
            mainActivity.waitForTTS(mainActivity)
            mainActivity.askUser(
                "$i. ${lst[i]}",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
            i++
        }
    }

    /**
     * This helper function looks for a list in the list of lists
     * @param mainActivity Context
     * @param text User input
     * @param requestCode Context
     * @return Boolean
     */
    private fun findList(mainActivity: MainActivity, text: String, requestCode: Int): Boolean {
        mainActivity.currentList = -1
        for (item in lists) {
            if (item[0] == text) {
                mainActivity.currentList = lists.indexOf(item)
                break
            }
        }
        return if (mainActivity.currentList == -1) {
            mainActivity.askUser(
                "Die Liste $text konnte nicht gefunden werden.",
                mainActivity,
                requestCode
            )
            true
        } else false
    }

    /**
     * This function checks if the list exists and asks what to do next
     * @param mainActivity Context
     * @param text User input
     */
    internal fun editList(mainActivity: MainActivity, text: String) {
        if (findList(mainActivity, text, MainActivity.REQUEST_CODE_STT_LIST_EDIT)) return
        mainActivity.countName = 0
        mainActivity.askUser(
            "Was willst du ändern?",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_LIST_EDIT_FIELD
        )
    }

    /**
     * This function adds an item during editing
     * @param mainActivity Context
     * @param text User input
     */
    internal fun addItemEdit(mainActivity: MainActivity, text: String) {
        lists[mainActivity.currentList].add(text)
        mainActivity.askUser(
            "Ich habe $text der Liste hinzugefügt. Möchtest du noch etwas ändern?",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_LIST_EDIT_FIELD
        )
        saveLists<List>(mainActivity)
    }

    /**
     * This function checks if the item exists and if it exists it gets removed
     * @param mainActivity Context
     * @param recognizedText User input
     */
    internal fun removeItem(mainActivity: MainActivity, recognizedText: String) {
        var i = lists[mainActivity.currentList].indexOf(recognizedText)
        if (i < 0) {
            mainActivity.askUser(
                "Den Gegenstand $recognizedText habe ich nicht gefunden. Was soll geändert werden?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_LIST_EDIT_ITEM_REMOVE
            )
            return
        }
        mainActivity.countName = 0
        lists[mainActivity.currentList].removeAt(i)
        mainActivity.askUser(
            "Ich habe $recognizedText von der Liste entfernt. Was möchtest du noch bearbeiten?",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_LIST_EDIT_FIELD
        )
    }

    /**
     * This function saves the lists into a file
     * @param T Type
     * @param mainActivity Context
     */
    internal fun <T : Serializable?> saveLists(mainActivity: MainActivity) {
        try {
            val fos = mainActivity.openFileOutput("lists", Context.MODE_PRIVATE)
            val os = ObjectOutputStream(fos)
            os.writeObject(this)
            os.close()
            fos.close()
            println("List saved")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * This function loads the lists from a file
     * @param T Type
     * @param mainActivity Context
     * @return Object with saved lists
     */
    internal fun <T : Serializable?> loadLists(mainActivity: MainActivity): T? {
        var ret: T? = null
        try {
            val fis = mainActivity.openFileInput("lists")
            val ois = ObjectInputStream(fis)
            ret = ois.readObject() as T
            ois.close()
            fis.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ret
    }
}