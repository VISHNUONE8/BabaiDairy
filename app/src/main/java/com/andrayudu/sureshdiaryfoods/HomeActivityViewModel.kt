package com.andrayudu.sureshdiaryfoods

import androidx.lifecycle.ViewModel
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository

class HomeActivityViewModel(private val repository: CartItemRepository): ViewModel() {
    val cartItems = repository.cartItems

}