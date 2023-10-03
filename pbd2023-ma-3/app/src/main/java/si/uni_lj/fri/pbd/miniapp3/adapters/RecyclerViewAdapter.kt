package si.uni_lj.fri.pbd.miniapp3.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import si.uni_lj.fri.pbd.miniapp3.R
import si.uni_lj.fri.pbd.miniapp3.models.RecipeSummaryIM
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipeSummaryDTO
import si.uni_lj.fri.pbd.miniapp3.ui.DetailsActivity


class RecyclerViewAdapter(context: Context, val fromSearch: Boolean) : RecyclerView.Adapter<RecyclerViewAdapter.CardViewHolder>(), CoroutineScope by MainScope() {

    private val context: Context = context

    private var recipeSummaryDTO: List<RecipeSummaryDTO>? = null
    private var recipeSummaryIM: List<RecipeSummaryIM>? = null

    inner class CardViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var itemImage: ImageView? = null
        var itemContent: TextView? = null


        init {
            if(itemView != null) {
                this.itemImage = itemView.findViewById(R.id.image_view)
                this.itemContent = itemView.findViewById(R.id.text_view_content)
            }

            val intent = Intent(context, DetailsActivity::class.java)
            if(fromSearch) {
                // instantiated from SearchFragment
                itemView?.setOnClickListener {
                    intent.putExtra("id", recipeSummaryDTO!![position].idDrink)
                    intent.putExtra("fromSearch", true)
                    context.startActivity(intent)
                }

            } else {
                // instantiated from FavouritesFragment
                itemView?.setOnClickListener {
                    intent.putExtra("id", recipeSummaryIM!![position].idDrink)
                    context.startActivity(intent)
                }
            }
        }
    }

    /**
     * Function sets the recipes
     */
    fun setRecipes(summaries: List<RecipeSummaryDTO>?, rsIM: List<RecipeSummaryIM>?) {
        recipeSummaryDTO = summaries
        recipeSummaryIM = rsIM
        notifyDataSetChanged()
    }

    /**
     * Inflates the view, makes a CardViewHolder and returns it
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CardViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_grid_item, viewGroup, false)
        return CardViewHolder(view)
    }

    /**
     * Sets recipe images and summaries for cocktails
     */
    override fun onBindViewHolder(viewHolder: CardViewHolder, i: Int) {
        if(fromSearch) {
            viewHolder.itemContent?.text = recipeSummaryDTO!![i].strDrink

            // use provided Glide for images (https://github.com/bumptech/glide)
            Glide.with(context)
                .load(recipeSummaryDTO!![i].strDrinkThumb)
                .fitCenter()
                .into(viewHolder.itemImage!!)
        } else {
            viewHolder.itemContent?.text = recipeSummaryIM!![i].strDrink

            // use provided Glide for images (https://github.com/bumptech/glide)
            Glide.with(context)
                .load(recipeSummaryIM!![i].strDrinkThumb)
                .fitCenter()
                .into(viewHolder.itemImage!!)
        }
    }

    /**
     * Function returns item count (if it was not instantiated from SearchFragment)
     */
    override fun getItemCount(): Int {
        var counter = 0

        if(recipeSummaryIM!= null && !fromSearch) {
            counter = recipeSummaryIM!!.size
        } else if (recipeSummaryDTO != null && fromSearch) {
            counter = recipeSummaryDTO!!.size
        }
        return counter
    }
}