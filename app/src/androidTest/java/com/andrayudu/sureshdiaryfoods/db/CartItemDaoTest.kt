package com.andrayudu.sureshdiaryfoods.db

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.andrayudu.sureshdiaryfoods.getOrAwaitValue
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.google.common.truth.Truth
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class CartItemDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dao: CartItemDao
    private lateinit var database: FoodItemDatabase


    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FoodItemDatabase::class.java
        ).build()
        dao = database.cartItemDao
    }

    @After
    fun tearDown() {
        database.close()
    }


    //checks whether the insertion is successful or not...
    @Test
    fun insertCartItemTest(): Unit = runBlocking {

        val cartItem = CartItem("Kova", 10, 10, 1, "Kova")
        dao.insertCartItem(cartItem)
        Truth.assertThat(dao.getCartItemsTotalCount()).isEqualTo(1)

    }

    //checks whether the cartCount of specific item  is working  or not...
    @Test
    fun getCartItemQuantityTest(): Unit = runBlocking {

        val cartItem = CartItem("Kova", 10, 10, 1, "Kova")
        dao.insertCartItem(cartItem)
        Truth.assertThat(dao.getCartItemQuantity("Kova")).isEqualTo(1)

    }

    //checks whether the update cartCount  is working  or not...
    @Test
    fun updateCartItemTest(): Unit = runBlocking {

        val cartItem = CartItem("Kova", 10, 10, 1, "Kova")
        dao.insertCartItem(cartItem)
        cartItem.Quantity = 2
        val cartItemUpdated = cartItem
        dao.updateCartItem(cartItemUpdated)
        Truth.assertThat(dao.getCartItemsTotalCount()).isEqualTo(1)


    }
    //checks whether the deleteCartItem  is working  or not...
    @Test
    fun deleteCartItemTest(): Unit = runBlocking {

        val cartItem = CartItem("Kova", 10, 10, 1, "Kova")
        dao.insertCartItem(cartItem)
        dao.deleteCartItem("Kova")
        Truth.assertThat(dao.getCartItemsTotalCount()).isEqualTo(0)
    }
    //checks whether the getCovaCount()  is working  or not...
    @Test
    fun getKovaCountTest(): Unit = runBlocking {

        val cartItem = CartItem("Kova", 10, 10, 1, "Kova")
        dao.insertCartItem(cartItem)
        dao.deleteCartItem("Kova")
        Truth.assertThat(dao.getKovaCount()).isEqualTo(0)
    }

    //checks whether the deleteAll functionality is working properly or not...
    @Test
    fun deleteAllTest(): Unit = runBlocking {

        val cartItem = CartItem("Kova", 10, 10, 1, "Kova")
        dao.insertCartItem(cartItem)
        dao.deleteAll()
        Truth.assertThat(dao.getCartItemsTotalCount()).isEqualTo(0)

    }
    @Test
    fun getAllCartItemsTest(): Unit = runBlocking {

        val cartItem = CartItem("Kova", 10, 10, 1, "Kova")
        dao.insertCartItem(cartItem)
        val cartItems = dao.getAllCartItems().getOrAwaitValue()
        Log.i("tag","the items is list are"+cartItems.toString())
        Truth.assertThat(cartItems).hasSize(1)

    }
//    getAllCartItemsCount() - no need to check it as it working fine and implemented in the above tests...





    }