package si.uni_lj.fri.pbd.miniapp3.database

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import si.uni_lj.fri.pbd.miniapp3.database.dao.RecipeDao
import si.uni_lj.fri.pbd.miniapp3.database.entity.RecipeDetails

class RecipeRepository(app: Application?) {

    private val recipeDao: RecipeDao
    val allRecipes: LiveData<List<RecipeDetails>>
    val searchResults = MutableLiveData<RecipeDetails>()

    init {
        val db: Database = Database.getDatabase(app?.applicationContext!!)!!
        recipeDao = db.RecipeDao()
        allRecipes = recipeDao.getAllRecipes()
    }

    fun findRecipe(idDrink: String) {
        Database.databaseWriteExecutor.execute( Runnable {
            searchResults.postValue(recipeDao.getRecipeById(idDrink))
        })
    }

    fun deleteRecipe(idDrink: String) {
        Database.databaseWriteExecutor.execute( Runnable {
            recipeDao.deleteRecipeById(idDrink)
        })
    }

    fun insertRecipe(recipe: RecipeDetails) {
        Database.databaseWriteExecutor.execute(Runnable {
            recipeDao.insertRecipe(recipe)
        })
    }
}