package si.uni_lj.fri.pbd.miniapp1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import org.json.JSONObject
import java.io.ByteArrayOutputStream

private const val TIMESTAMP = "timestamp"

class DetailsFragment : Fragment() {

    val SHAREDPREF = "sharedPreferences"
    var timestamp: String? = null
    var detailsMemo = MemoModel(null, "", null, "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { timestamp =  it.getString(TIMESTAMP)}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_details, container, false)

        // load data
        val sharedPreferences = loadData(view)

        // delete button action
        val btnDelete = view.findViewById<Button>(R.id.btn_delete)
        btnDelete.setOnClickListener {
            with(sharedPreferences?.edit()) {
                this?.remove(timestamp)
                this?.apply()
            }
            findNavController().navigate(R.id.action_detailsFragment_to_listFragment)
        }

        // share button action
        val btnShare = view.findViewById<Button>(R.id.btn_share)
        btnShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type="text/plain"
            // subject: title
            intent.putExtra(Intent.EXTRA_SUBJECT, detailsMemo.title)

            // body text: description + timestap
            intent.putExtra(Intent.EXTRA_TEXT, detailsMemo.description + "\n\n" + detailsMemo.timestamp)

            // attachment: photo
            if(detailsMemo.image != null) {
                val bitmapImage: Bitmap? = detailsMemo.image
                val byteStream = ByteArrayOutputStream()
                bitmapImage?.compress(Bitmap.CompressFormat.JPEG, 100, byteStream)
                val path = MediaStore.Images.Media.insertImage(context?.contentResolver, bitmapImage,"Attachment", null)
                val uri = Uri.parse(path)
                intent.putExtra(Intent.EXTRA_STREAM, uri)
            }

            startActivity(Intent.createChooser(intent, "Select app"))
        }

        return view
    }

    /**
     * Function loads data from memo in ListFragment and returns sharedPreferences
     */
    private fun loadData(view: View?): SharedPreferences? {
        val sharedPreferences = (activity as MainActivity).getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE)
        val firstValue = sharedPreferences.getString(timestamp, "").toString()
        val json = JSONObject(firstValue)
        detailsMemo = MemoModel(
            json.getString("timestamp"),
            json.getString("title"),
            stringToBitmap(json.getString("image")),
            json.getString("description")
        )

        view?.findViewById<TextView>(R.id.details_title)?.text = detailsMemo.title
        view?.findViewById<ImageView>(R.id.details_image)?.setImageBitmap(detailsMemo.image)
        view?.findViewById<TextView>(R.id.details_timestamp)?.text = detailsMemo.timestamp
        view?.findViewById<TextView>(R.id.details_description)?.text = detailsMemo.description

        return sharedPreferences
    }

    /**
     * Returns bitmap image from base64 encoded string
     */
    private fun stringToBitmap(stringImage: String?): Bitmap? {
        val decodedString = Base64.decode(stringImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }
}