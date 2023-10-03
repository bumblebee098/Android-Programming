package si.uni_lj.fri.pbd.miniapp3.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import si.uni_lj.fri.pbd.miniapp3.R
import si.uni_lj.fri.pbd.miniapp3.database.entity.RecipeDetails
import si.uni_lj.fri.pbd.miniapp3.databinding.ActivityDetailsBinding
import si.uni_lj.fri.pbd.miniapp3.rest.ServiceGenerator.createService
import si.uni_lj.fri.pbd.miniapp3.models.Mapper
import si.uni_lj.fri.pbd.miniapp3.models.RecipeDetailsIM
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipeDetailsDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipesByIdDTO
import si.uni_lj.fri.pbd.miniapp3.rest.RestAPI

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    private lateinit var recipeDetails: RecipeDetails
    private lateinit var recipeDetailsIM: RecipeDetailsIM
    private lateinit var recipeDetailsDTO: RecipeDetailsDTO

    private lateinit var recipeId: String
    private var fromSearch = false
    private var favourite = false

    private var recipeViewModel: RecipeViewModel? = null

    companion object {
        private const val TAG = "DetailsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get binding
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get extra fields
        var bundle: Bundle? = intent.extras
        recipeId = bundle!!.getString("id").toString()
        fromSearch = bundle.getBoolean("fromSearch")

        // https://medium.com/androiddevelopers/viewmodels-a-simple-example-ed5ac416317e
        recipeViewModel = ViewModelProviders.of(this).get(RecipeViewModel::class.java) // create ViewModel association

        // get recipes
        getRecipes()

        recipeViewModel?.findRecipe(recipeId)

        // favourite button action
        binding.btnFavourite.setOnClickListener {
            if(fromSearch) {
                setFavourite()
            } else {
                setFavouriteDB()
            }

        }
    }

    /**
     * set recipe as favourite, if we come from Search
     */
    fun setFavourite() {
        if(favourite) {
            // remove recipe from favourites
            recipeViewModel?.deleteRecipe(recipeDetailsIM.idDrink.toString())
            recipeDetailsIM.setFavorite(false)
            changeFavouriteBtn(false)
            favourite = false
        } else {
            // add recipe to favourites
            val recipe = Mapper.mapRecipeDetailsDtoToRecipeDetails(true, recipeDetailsDTO)
            recipeDetailsIM.setFavorite(true)
            recipeViewModel?.insertRecipe(recipe)
            changeFavouriteBtn(true)
            favourite = true
        }
    }

    /**
     * set recipe as favourite, if we don't come from search
     */
    fun setFavouriteDB() {
        if(favourite) {
            // remove recipe from favourites
            recipeViewModel?.deleteRecipe(recipeDetailsIM.idDrink.toString())
            recipeDetailsIM.setFavorite(false)
            changeFavouriteBtn(false)
            favourite = false
        } else {
            // add recipe to favourites
            val recipe = recipeDetails
            recipeViewModel?.insertRecipe(recipe)
            recipeDetailsIM.setFavorite(true)
            changeFavouriteBtn(true)
            favourite = true
        }
    }

    /**
     * function sets the heart to red if a recipe is tagged favourite
     */
    fun changeFavouriteBtn(favourite: Boolean) {
        if(favourite) {
            binding.btnFavourite.setBackgroundResource(R.drawable.heart)
        } else {
            binding.btnFavourite.setBackgroundResource(R.drawable.no_heart)
        }
    }

    /**
     * function gets recipes
     */
    fun getRecipes() {
        recipeViewModel?.searchResults?.observe(this
        ) { recipe ->
            if (recipe == null) {
                // if recipe is null, get it from online database
                getRecipeDetailsDB()
                favourite = false
            } else {
                // if recipe already exists
                recipeDetails = recipe
                recipeDetailsIM = Mapper.mapRecipeDetailsToRecipeDetailsIm(true, recipe)
                favourite = true
                fromSearch = false
                showDetails()
            }
        }
    }

    /**
     * function gets recipe from online database, if the recipe doesn't exist already
     */
    fun getRecipeDetailsDB() {
        // make an API call
        val service = createService(RestAPI::class.java)
        val call = service.getRecipeDetailsByID(recipeId)

        call?.enqueue(object: Callback<RecipesByIdDTO?> {
            override fun onResponse(
                call: Call<RecipesByIdDTO?>,
                response: Response<RecipesByIdDTO?>
            ) {
                if(response.isSuccessful) {
                    // if response is successful get the recipes
                    recipeDetailsDTO = response.body()!!.drinks!![0]
                    recipeDetailsIM = Mapper.mapRecipeDetailsDtoToRecipeDetailsIm(false, recipeDetailsDTO)
                    showDetails()
                } else {
                    Toast.makeText(applicationContext, R.string.err_getting_ingredients, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecipesByIdDTO?>, t: Throwable) {
                Log.d(TAG, t.message.toString())
            }
        })
    }

    /**
     * function setts the details for recipe (image, title, ingredients and measurements, instructions, heart)
     */
    fun showDetails() {
        Glide.with(this)
            .load(recipeDetailsIM.strDrinkThumb)
            .fitCenter()
            .into(binding.imageCocktailDetails)

        binding.titleCocktailDetails.text = recipeDetailsIM.strDrink + "\n"
        binding.ingredientsCocktailDetails.text = "Ingredients and measures:\n" + notNullIngredients() + "\n"
        binding.preparationInstructionsCocktailDetails.text = "Instructions:\n" + recipeDetailsIM.strInstructions

        changeFavouriteBtn(favourite)
    }

    /**
     * function gets all ingredients and their measurements, if they are not null
     */
    private fun notNullIngredients(): String {
        val ingredients = mutableListOf<String>()
        for(i in 1..15) {
            val ingredient = recipeDetailsIM.javaClass.getDeclaredMethod("getStrIngredient$i").invoke(recipeDetailsIM) as? String
            val measure = recipeDetailsIM.javaClass.getDeclaredMethod("getStrMeasure$i").invoke(recipeDetailsIM) as? String
            ingredient?.let {
                ingredients.add(ingredient + " " + measure)
            }
        }
        return ingredients.joinToString(", ")
    }

}