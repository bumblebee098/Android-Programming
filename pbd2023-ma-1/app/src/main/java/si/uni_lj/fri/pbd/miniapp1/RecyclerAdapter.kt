package si.uni_lj.fri.pbd.miniapp1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(var parentHolderManager: FragmentManager,
var dataList: ArrayList<MemoModel>) : RecyclerView.Adapter<RecyclerAdapter.CardViewHolder?>() {

    private var listener: AdapterView.OnItemClickListener? = null

    inner class CardViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var itemImage: ImageView? = itemView?.findViewById(R.id.item_image)
        var itemTitle: TextView? = itemView?.findViewById(R.id.item_title)
        var itemTimestamp: TextView? = itemView?.findViewById(R.id.item_timestamp)

       init {
           itemView?.setOnClickListener {
               listener?.onItemClick( null, itemView, adapterPosition, 0)
           }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): CardViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.recycler_item_memo_model, viewGroup, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: CardViewHolder, i: Int) {
        viewHolder.itemImage?.setImageBitmap(dataList[i].image)
        viewHolder.itemTitle?.text = dataList[i].title
        viewHolder.itemTimestamp?.text = dataList[i].timestamp
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setOnItemClickListener(listener: AdapterView.OnItemClickListener){
        this.listener = listener
    }
}