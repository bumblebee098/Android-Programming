package si.uni_lj.fri.pbd.miniapp1

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import android.widget.ImageView
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class NewFragment : Fragment() {
    var timestamp: String? = null
    var imageBitmap: Bitmap? = null
    var photoWasTaken = false

    val SHAREDPREF = "sharedPreferences"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new, container, false)

        // take photo button action
        val btnTakePhoto = view?.findViewById<Button>(R.id.btn_take_photo)
        btnTakePhoto?.setOnClickListener { takePhoto() }

        // save button action
        val btnSave = view?.findViewById<Button>(R.id.btn_save)
        btnSave?.setOnClickListener {
            val title = view.findViewById<EditText>(R.id.title)
            val description = view.findViewById<EditText>(R.id.description)

            if (title.length() > 0 && description.length() > 0 && photoWasTaken) {
                saveMemo(title?.text.toString(), description?.text.toString())
                findNavController(this).navigate(R.id.action_newFragment_to_listFragment) // navigate to list fragment
            } else {
                Snackbar.make(view, "Missing title and/or description", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }

    /**
     * Saves memo
     */
    private fun saveMemo(title: String, description: String) {
        val addMemo = MemoModel(timestamp, title, imageBitmap, description)

        val jsonifiedMemo = addMemo.toJson()

        val sharedPreferences = (activity as MainActivity).getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString(timestamp, jsonifiedMemo)
        editor?.commit()

    }

    /**
     * Takes photo with phone camera
     */
    private fun takePhoto() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePhotoIntent, 1)
        } catch(e : ActivityNotFoundException) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            val imageView = view?.findViewById<ImageView>(R.id.imageView)
            imageBitmap = data?.extras?.get("data") as Bitmap
            imageView?.setImageBitmap(imageBitmap)
            photoWasTaken = true
        }
    }

}