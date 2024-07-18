package com.andrayudu.babaidairy.db

import com.andrayudu.babaidairy.model.CartItem

class CartItemRepository(private val cartItemDao: CartItemDao) {



    val cartItems = cartItemDao.getAllCartItems()

    suspend fun insert(cartItem: CartItem):Long{
        return cartItemDao.insertCartItem(cartItem)
    }
    suspend fun getCartItemQuantity(itemName:String):Int{
        val cartCount:Int? = cartItemDao.getCartItemQuantity(itemName)
        //below operation returns 0 if cartCount is null else returns the cartCount
        return if (cartCount == null) 0 else cartCount
    }
    suspend fun getKovaCount():Int
    {
        return cartItemDao.getKovaCount()
    }

    suspend fun update(cartItem: CartItem):Int{
        return cartItemDao.updateCartItem(cartItem)
    }
    suspend fun delete(name:String):Int{
        return cartItemDao.deleteCartItem(name)
    }
    suspend fun deleteAll():Int{
        return   cartItemDao.deleteAll()
    }
}