package com.andrayudu.sureshdiaryfoods.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.model.CartItem

class CartViewModel(private val repository: CartItemRepository):ViewModel() {

    val cartItems = repository.cartItems

    var repo = repository

    val grandTotal = MutableLiveData<String>()
    var totalcost: String = "0"


    lateinit var cartList: List<CartItem>

    var kovaQuantity: Int = 0


    fun getTotalCost(): String {
        return totalcost

    }

    fun calculateGrandTotalCost() {

        //here this value is always non null because inorder for the cart activity to appear therre should be atleast one item in the cart
        cartList = cartItems.value!!

        totalcost = "0"


        for (cartItem in cartList) {
            totalcost =
                (totalcost.toInt() + (cartItem.Price.toInt() * cartItem.Quantity.toInt())).toString()
            Log.i("the total cart value is:", totalcost)

            grandTotal.setValue(totalcost)
        }


    }

    suspend fun getKovaCount(): Int {

        kovaQuantity = repository.getKovaCount("Kova")

        return kovaQuantity
    }
}
