package com.andrayudu.sureshdiaryfoods

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.andrayudu.sureshdiaryfoods.ui.HomeActivityViewModel
import com.google.common.truth.Truth

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeActivityViewModelTest {

    private val TAG = "HomeActivityViewModelTest"


    private lateinit var viewModel: HomeActivityViewModel

    private val demoUserId = "zzox4y6TCIQI0TrpoqnOj28ut773"
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        viewModel = HomeActivityViewModel()
    }

    @Test
    fun loadItemsCatalogue_noInputParam_liveData(){
         viewModel.loadItemsCatalogue()
        val result =  viewModel.itemsCatalogueLive.getOrAwaitValue().itemsList
        Truth.assertThat(result?.isNotEmpty()).isTrue()
    }
    @Test
    fun loadCustomerPayments_userId_liveData(){
         viewModel.loadCustomerPayments(demoUserId)
        val result =  viewModel.paymentListLive.getOrAwaitValue()
        Truth.assertThat(result.isNotEmpty()).isTrue()
    }
    @Test
    fun loadCustomerOrders_userId_liveData(){
         viewModel.loadCustomerOrders(demoUserId)
        val result =  viewModel.ordersListLive.getOrAwaitValue()
        Truth.assertThat(result.isNotEmpty()).isTrue()
    }
    @Test
    fun loadUserData_userId_liveData(){
         viewModel.loadUserData(demoUserId)
         val result =  viewModel.userLive.getOrAwaitValue()
         Truth.assertThat(result!=null).isTrue()
    }

    @Test
    fun sortOrdersByDate_ordersList_sortedList(){
        viewModel.loadCustomerOrders(demoUserId)
        val orders = viewModel.ordersListLive.getOrAwaitValue()
        val sortedOrders = viewModel.sortOrdersByDate(orders)
        Truth.assertThat(sortedOrders.isNotEmpty()).isTrue()
    }

    @Test
    fun createDatesLogicListRV_sortedOrdersList_logicList(){
        viewModel.loadCustomerOrders(demoUserId)
        val orders = viewModel.ordersListLive.getOrAwaitValue()
        val sortedOrders = viewModel.sortOrdersByDate(orders)
        viewModel.createDatesListLogicRV(sortedOrders)
        val logicList = viewModel.datesList
        Truth.assertThat(logicList.isNotEmpty()).isTrue()
    }
}