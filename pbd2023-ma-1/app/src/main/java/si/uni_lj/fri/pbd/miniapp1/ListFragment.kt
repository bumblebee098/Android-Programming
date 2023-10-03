package si.uni_lj.fri.pbd.miniapp1

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import kotlin.collections.HashMap
import android.util.Base64
import org.json.JSONException
import kotlin.collections.ArrayList

class ListFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<*>? = null
    private var fab: FloatingActionButton? = null
    private var memosList = ArrayList<MemoModel>()

    private val SHAREDPREF = "sharedPreferences"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        layoutManager = LinearLayoutManager(view.context)
        recyclerView?.layoutManager = layoutManager

        adapter = RecyclerAdapter(parentFragmentManager, memosList)
        recyclerView?.adapter = adapter

        // click on memo
        (adapter as RecyclerAdapter).setOnItemClickListener(
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val memoModel = memosList[position]
                val timestamp = memoModel.timestamp as String
                val action = ListFragmentDirections.actionListFragmentToDetailsFragment(timestamp)
                findNavController(this).navigate(action)
            })

        // fab click
        fab = view.findViewById(R.id.fab)
        fab?.setOnClickListener {
            findNavController(this).navigate(R.id.action_listFragment_to_newFragment)
            memosList.clear()
        }

        return view
    }

    override fun onResume() {
        makeNewMemo()
        adapter?.notifyDataSetChanged()
        super.onResume()
    }

    /**
     * Function makes a new memo
     */
    fun makeNewMemo() {
        memosList.clear()
        val sharedPreferences = (activity as MainActivity).getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE).all as HashMap<String, String>
        sharedPreferences.forEach {
            (_, v) ->
            try {
                val json = JSONObject(v)
                val newMemo = MemoModel(
                    json.getString("timestamp"),
                    json.getString("title"),
                    stringToBitmap(json.getString("image")),
                    json.getString("description")
                )
                memosList.add(newMemo)
            } catch (e: JSONException) {
                println("Failed to parse memo JSON: $e")
            }
        }
    }

    /**
     * Returns bitmap image from base64 encoded string
     */
    private fun stringToBitmap(stringImage: String?): Bitmap? {
        val decodedString = Base64.decode(stringImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }
}