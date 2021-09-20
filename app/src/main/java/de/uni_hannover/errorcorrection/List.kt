package de.uni_hannover.errorcorrection

import android.content.Context
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.lang.Exception
import kotlin.random.Random

class List : Serializable {
    internal var lists: MutableList<MutableList<String>> = mutableListOf()
    internal var replaceable: String = ""
    internal var replaceableIndex: Int = -1
    private var errorItems = mutableListOf("Video game", "TV Magazine", "Apples", "Apple Pie", "Butter", "Eggs", "Cheese", "Milk")
    private var rng = Random

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
                    "The name is already in use. What should the list be called?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_LIST_NAME
                )
                return
            }
        }
        mainActivity.countName = 0
        var lst = mutableListOf(text)
        //add list and then ask for items to add
        lists.add(lst)
        mainActivity.currentList = lists.indexOf(lst)
        mainActivity.askUser(
            "I created the $text list. What should I add?",
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
        //add error here if just the name exists for this list, else add regular item
        if (lists[mainActivity.currentList].size == 1) {
            lists[mainActivity.currentList].add(errorItems[rng.nextInt(errorItems.size)])
            mainActivity.askUser(
                "I've added ${lists[mainActivity.currentList][1]} to the list. Anything else you want to add?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_LIST_CREATE_ITEM
            )
        } else {
            lists[mainActivity.currentList].add(text)
            mainActivity.askUser(
                "I've added $text to the list. Anything else you want to add?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_LIST_CREATE_ITEM
            )
        }
        saveLists<List>(mainActivity)
    }

    /**
     * This function checks if the list exists and if it exists it gets deleted.
     * @param mainActivity Context
     * @param text User input
     */
    internal fun deleteList(mainActivity: MainActivity, text: String) {
        //checks if list exists
        if (findList(mainActivity, text, MainActivity.REQUEST_CODE_STT_LIST_DELETE)) return
        mainActivity.countName = 0
        lists.removeAt(mainActivity.currentList)
        mainActivity.currentList = -1
        mainActivity.askUser(
            "The $text list has been removed.",
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
        //check if this function is called during editing or not
        if (requestCode != MainActivity.REQUEST_CODE_STT_LIST_EDIT_ITEM_READ) {
            if (findList(mainActivity, text, requestCode)) return
            mainActivity.countName = 0
            read(mainActivity, lists[mainActivity.currentList])
        } else {
            read(mainActivity, lists[mainActivity.currentList])
            mainActivity.askUser(
                "What else do you want to edit?",
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
        //read name
        mainActivity.askUser(
            "The name of the list is ${lst[i]}",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_NOTIFY
        )
        i++
        //read items
        while (i < lst.size) {
            mainActivity.waitForTTS()
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
        //look for list in list of lists
        for (item in lists) {
            if (item[0] == text) {
                mainActivity.currentList = lists.indexOf(item)
                break
            }
        }
        return if (mainActivity.currentList == -1) {
            mainActivity.askUser(
                "The list $text could not be found.",
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
        //look for list
        if (findList(mainActivity, text, MainActivity.REQUEST_CODE_STT_LIST_EDIT)) return
        mainActivity.countName = 0
        mainActivity.askUser(
            "What do you want to change?",
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
            "I've added $text to the list. Would you like to change anything?",
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
        //check for item in list
        if (i < 0) {
            mainActivity.askUser(
                "I did not find the item $recognizedText. What should be changed?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_LIST_EDIT_ITEM_REMOVE
            )
            return
        }
        mainActivity.countName = 0
        lists[mainActivity.currentList].removeAt(i)
        mainActivity.askUser(
            "I removed $recognizedText from the list. What else do you want to edit?",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_LIST_EDIT_FIELD
        )
    }

    /**
     * This function saves the lists into a file
     * @param T Type
     * @param mainActivity Context
     */
    private fun <T : Serializable?> saveLists(mainActivity: MainActivity) {
        //write this object to file
        try {
            val fos = mainActivity.openFileOutput("lists", Context.MODE_PRIVATE)
            val os = ObjectOutputStream(fos)
            os.writeObject(this)
            os.close()
            fos.close()
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
        //read List object from file
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