package si.uni_lj.fri.pbd.miniapp3.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import si.uni_lj.fri.pbd.miniapp3.R
import si.uni_lj.fri.pbd.miniapp3.models.dto.IngredientDTO

class SpinnerAdapter(context: Context, ingredientsList: List<IngredientDTO>) : BaseAdapter() {

    private var ingredientsList = ingredientsList
    private var inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * returns size of ingredientsList
     */
    override fun getCount(): Int {
        return ingredientsList.size
    }

    /**
     * returns ingredient at position
     */
    override fun getItem(position: Int): Any {
        return ingredientsList[position]
    }

    /**
     * returns item id
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val layout: View = inflater.inflate(R.layout.spinner_item, parent, false)
        layout.findViewById<TextView>(R.id.text_view_spinner_item).text = ingredientsList[position].strIngredient1
        return layout
    }
}