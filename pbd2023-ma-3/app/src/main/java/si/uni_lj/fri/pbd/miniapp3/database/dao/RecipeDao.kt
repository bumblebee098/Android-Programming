package si.uni_lj.fri.pbd.miniapp3.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import si.uni_lj.fri.pbd.miniapp3.database.entity.RecipeDetails

@Dao
interface RecipeDao {
    @Query("SELECT * FROM RecipeDetails WHERE idDrink = :idDrink")
    fun getRecipeById(idDrink: String?): RecipeDetails?

    @Query("DELETE FROM RecipeDetails WHERE idDrink = :idDrink")
    fun deleteRecipeById(idDrink: String?)

    @Query("SELECT * FROM RecipeDetails")
    fun getAllRecipes(): LiveData<List<RecipeDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecipe(recipe: RecipeDetails)

}