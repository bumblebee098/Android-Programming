package si.uni_lj.fri.pbd.miniapp3.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import si.uni_lj.fri.pbd.miniapp3.R
import si.uni_lj.fri.pbd.miniapp3.adapters.RecyclerViewAdapter
import si.uni_lj.fri.pbd.miniapp3.databinding.FragmentFavoritesBinding
import si.uni_lj.fri.pbd.miniapp3.models.Mapper
import si.uni_lj.fri.pbd.miniapp3.models.RecipeSummaryIM

class FavoritesFragment : Fragment() {

    private var binding: FragmentFavoritesBinding? = null

    private var favouritesViewModel: RecipeViewModel? = null

    private var recyclerView: RecyclerView? = null
    private var adapter: RecyclerViewAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val view = binding?.root

        // setup for recyclerView
        recyclerView = view?.findViewById(R.id.favourites_recycler_view)
        adapter = RecyclerViewAdapter(requireContext(), false)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(context)

        return view
    }

    /**
     * Function sets favouritesViewModel and call function to show recipes tagged favourite
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favouritesViewModel = ViewModelProvider(this).get(RecipeViewModel::class.java)
        getFavourites()
    }

    /**
     * Function returns recipes that were tagged favourite
     */
    fun getFavourites() {
        favouritesViewModel?.allRecipes?.observe(viewLifecycleOwner
        ) { recipes ->
            var recipeSummaryIMs: MutableList<RecipeSummaryIM> = mutableListOf()
            recipes.forEach {
                recipeSummaryIMs.add(Mapper.mapRecipeDetailsToRecipeSummaryIm(it))
            }

            adapter?.setRecipes(null, recipeSummaryIMs)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}