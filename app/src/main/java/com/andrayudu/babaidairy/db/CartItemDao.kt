package com.andrayudu.babaidairy.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.andrayudu.babaidairy.model.CartItem

@Dao
interface CartItemDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem):Long

    //returns null if there is no item matching the name, so use ifnull()
    @Query("SELECT IFNULL(quantity,0) FROM cart_item_table WHERE item_name = :name")
    suspend fun getCartItemQuantity(name:String):Int?

    @Update
    suspend fun updateCartItem(cartItem: CartItem):Int

    @Query("DELETE FROM cart_item_table WHERE item_name = :name")
    suspend fun deleteCartItem(name: String):Int

    //Here ifNull is working perfectly i.e it is returning 0 if there are no items
    // matching the conditions instead of returning null..
    @Query("SELECT IFNULL(SUM(quantity),0) FROM cart_item_table WHERE item_category IN ('Kova','KovaSpl')")
    suspend fun getKovaCount():Int


    @Query("DELETE FROM cart_item_table")
    suspend fun deleteAll():Int

    @Query("SELECT * FROM cart_item_table")
    fun getAllCartItems(): LiveData<List<CartItem>>

    @Query("SELECT COUNT(*) FROM cart_item_table")
    fun getCartItemsTotalCount():Int




}