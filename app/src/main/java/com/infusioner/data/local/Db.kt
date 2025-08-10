package com.infusioner.data.local
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName="devices")
data class DeviceEntity(@PrimaryKey val id:String, val name:String, val ssid:String?, val password:String?, val ip:String, val status:String?, val recipeId:String?)
@Entity(tableName="recipes")
data class RecipeEntity(@PrimaryKey val id:String, val name:String, val temperature:Double, val frequency:Double, val durationSec:Int)
data class DeviceWithRecipe(@Embedded val d:DeviceEntity, @Relation(parentColumn="recipeId", entityColumn="id") val r:RecipeEntity?)

@Dao interface DeviceDao{
  @Transaction @Query("SELECT * FROM devices") fun observe(): Flow<List<DeviceWithRecipe>>
  @Transaction @Query("SELECT * FROM devices WHERE id=:id") suspend fun get(id:String): DeviceWithRecipe?
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(d:DeviceEntity)
  @Query("UPDATE devices SET name=:name WHERE id=:id") suspend fun rename(id:String, name:String)
  @Query("DELETE FROM devices WHERE id=:id") suspend fun delete(id:String)
  @Query("UPDATE devices SET recipeId=:rid WHERE id=:id") suspend fun setRecipe(id:String, rid:String?)
  @Query("UPDATE devices SET status=:st WHERE id=:id") suspend fun setStatus(id:String, st:String?)
}
@Dao interface RecipeDao{
  @Query("SELECT * FROM recipes") fun observe(): Flow<List<RecipeEntity>>
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(r:RecipeEntity)
  @Query("DELETE FROM recipes WHERE id=:id") suspend fun delete(id:String)
}
@Database(entities=[DeviceEntity::class, RecipeEntity::class], version=1, exportSchema=false)
abstract class AppDatabase: RoomDatabase(){ abstract fun device():DeviceDao; abstract fun recipe():RecipeDao }
