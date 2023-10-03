package si.uni_lj.fri.pbd.miniapp3.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import si.uni_lj.fri.pbd.miniapp3.database.RecipeRepository
import si.uni_lj.fri.pbd.miniapp3.database.entity.RecipeDetails

class RecipeViewModel(application: Application?) : AndroidViewModel(application!!) {
    var allRecipes: LiveData<List<RecipeDetails>>
    var searchResults: MutableLiveData<RecipeDetails>

    private val repository: RecipeRepository

    /**
     * functions for inserting, finding and deleting recipes
     */
    fun insertRecipe(recipe: RecipeDetails) {
        repository.insertRecipe(recipe)
    }

    fun findRecipe(recipeId: String) {
        repository.findRecipe(recipeId)
    }

    fun deleteRecipe(recipeId: String) {
        repository.deleteRecipe(recipeId)
    }

    init {
        repository = RecipeRepository(application)
        allRecipes = repository.allRecipes
        searchResults = repository.searchResults
    }

}