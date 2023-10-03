package si.uni_lj.fri.pbd.miniapp3.ui

import android.content.ContentValues.TAG
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.*
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import si.uni_lj.fri.pbd.miniapp3.R
import si.uni_lj.fri.pbd.miniapp3.adapters.RecyclerViewAdapter
import si.uni_lj.fri.pbd.miniapp3.adapters.SpinnerAdapter
import si.uni_lj.fri.pbd.miniapp3.models.dto.IngredientDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.IngredientsDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipesByIngredientDTO
import si.uni_lj.fri.pbd.miniapp3.rest.RestAPI
import si.uni_lj.fri.pbd.miniapp3.rest.ServiceGenerator.createService


class SearchFragment : Fragment(), CoroutineScope by MainScope() {

    private var recyclerView: RecyclerView? = null
    private var adapter: RecyclerViewAdapter? = null

    private var selected: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // set view and adapter
        val view: View? = inflater.inflate(R.layout.fragment_search, container, false)
        recyclerView = view?.findViewById(R.id.search_recycler_view)
        adapter = RecyclerViewAdapter(requireContext(), true)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = adapter

        // get ingredients
        getIngredient()

        view?.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)?.setOnRefreshListener {
            downloadCocktails()
            view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)?.isRefreshing = false
        }

        return view
    }

    /**
     * function gets ingredients from spinner drop down menu
     */
    private fun getIngredient() {
        // https://devtut.github.io/android/retrofit2.html#a-simple-get-request
        val service = createService(RestAPI::class.java)

        // get JSON object
        val call = service.allIngredients
        call?.enqueue(object: Callback<IngredientsDTO?> {
            override fun onResponse(
                call: Call<IngredientsDTO?>,
                response: Response<IngredientsDTO?>
            ) {
                if(response.isSuccessful) {
                    // get ingredients
                    val ingredients = response.body()

                    // set spinner
                    val spinner = view?.findViewById<Spinner>(R.id.spinner)
                    spinner?.adapter = SpinnerAdapter(context!!, ingredients?.ingredients!!)
                    spinner?.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {      // https://www.tabnine.com/code/java/methods/android.widget.Spinner/setOnItemSelectedListener
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            // get selected ingredient at position
                            val temp = (spinner!!.adapter!!.getItem(position) as IngredientDTO).strIngredient1.toString()
                            // if selection has changed, download new cocktails
                            if(selected != temp) {
                                selected =
                                    (spinner!!.adapter!!.getItem(position) as IngredientDTO).strIngredient1.toString()
                                downloadCocktails()
                            }
                            // save item form spinner, so it doesn't take the first element from spinner (default)
                            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                            with(sharedPref.edit()) {
                                putInt("SPINNER_POSITION", position)
                                commit()
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Do nothing
                        }
                    }
                    // set spinner to last selected ingredient
                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                    val spinnerPosition = sharedPref.getInt("SPINNER_POSITION", 0)
                    spinner?.setSelection(spinnerPosition)

                } else {
                    Toast.makeText(context, R.string.err_getting_ingredients, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<IngredientsDTO?>, t: Throwable) {
                Log.d(TAG, t.message.toString())
            }
        })
    }

    /**
     * function download the cocktails for selected ingredient
     */
    private fun downloadCocktails() {
        val selectedIngredients = selected

        // show progress bar while the recipes are downloading
        val progressBar = view?.findViewById<MaterialProgressBar>(R.id.progressBar)
        progressBar?.visibility = View.VISIBLE

        launch(Dispatchers.Default) {
            try {
                val service = createService(RestAPI::class.java)

                // get recipes by selected ingredient
                val call = service.getRecipesByIngredient(selectedIngredients.toString())
                call?.enqueue(object: Callback<RecipesByIngredientDTO?> {
                    override fun onResponse(
                        call: Call<RecipesByIngredientDTO?>,
                        response: Response<RecipesByIngredientDTO?>
                    ) {
                        if(response.isSuccessful) {
                            // hide progress bar
                            progressBar?.visibility = View.GONE
                            adapter?.setRecipes(response.body()!!.drinks, null)

                        } else {
                            Toast.makeText(context, R.string.err_getting_recipes, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<RecipesByIngredientDTO?>, t: Throwable) {
                        Log.d(TAG, t.message.toString())
                    }
                })
            } catch (e: InterruptedException) {
                Log.d(TAG, e.toString())
            }
        }

    }

    override fun onResume() {
        super.onResume()
        getIngredient()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val spinnerPosition = sharedPref.getInt("SPINNER_POSITION", 0)

        // set the spinner to the saved position
        view?.findViewById<Spinner>(R.id.spinner)?.setSelection(spinnerPosition)
    }

}