package com.andrayudu.sureshdiaryfoods.ui

import android.util.Log
import androidx.lifecycle.*
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.model.FoodItem
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.SpecialPricesModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//this viewmodel is used by both HomeFragment and FoodItemsActivity but not in a shared way
//for sharedviewmodel instance see homeActivity viewmodel as it is shared by ordersFrag and HomeActivity
//note: for sharing we should by viewModels() and by activityviewModels()
class FoodItemsViewModel(private val repository: CartItemRepository): ViewModel() {


    private val tag = "FoodItemsViewModel"

    var specialPricesModel :SpecialPricesModel?  = null
    val foodItemsList: ArrayList<FoodItem> = ArrayList()

    //LiveData
    val cartItems = repository.cartItems
    private val status = MutableLiveData<String>()
    private val firebaseFoodItems =  MutableLiveData<List<FoodItem>>()


    fun getStatus():LiveData<String>{
        return status
    }
    fun getFirebaseFoodItems():LiveData<List<FoodItem>>{
        return firebaseFoodItems
    }

    //to insert a value into the database
    fun insert(foodItem: FoodItem) {
        viewModelScope.launch(Dispatchers.IO)
        {
            //if the foodItem quantity is 0 then the item will be deleted from the cart...
            if(foodItem.Quantity == 0){
                repository.delete(foodItem.Name)
            }
            else{
                val cartItem = CartItem()
                cartItem.Name = foodItem.Name
                cartItem.Quantity = foodItem.Quantity
                cartItem.Category = foodItem.Category
                cartItem.Price = foodItem.Price
                //calculating cartItem Total Price i.e quantity * price
                if(cartItem.Category.equals("Kova") || cartItem.Category.equals("KovaSpl")){
                    cartItem.ItemTotalPrice = (cartItem.Quantity.toInt() * cartItem.Price!!.toInt() * 3)
                }
                else{
                    cartItem.ItemTotalPrice =(cartItem.Quantity.toInt() * cartItem.Price!!.toInt())

                }
                val newRowId = repository.insert(cartItem)
                withContext(Dispatchers.Main) {
                    if (newRowId > -1) {
                        Log.i("insert is successful","bro")
                    } else {

                        Log.i("insert has failed","bro")

                    }
                }
            }

        }

    }

    //loads splPriceSnap and updates the status LiveData
    fun getSpecialPricesSnapshot(){

        viewModelScope.launch (Dispatchers.IO){
            val mAuth = FirebaseAuth.getInstance()
            val userId = mAuth.currentUser?.uid

            FirebaseDatabase.getInstance().getReference("SpecialPricesTesting").child(userId.toString())
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        specialPricesModel = SpecialPricesModel()
                        if (snapshot.exists()) {
                            specialPricesModel = snapshot.getValue(SpecialPricesModel::class.java)
                            Log.i(tag,"specialprice snap loaded")
                        }
                        status.postValue("loaded")
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }


    }


    fun getSpecialPrice(foodItem: FoodItem?):FoodItem{

        var splPrice = 0

        when(foodItem?.Category){

            "Kova"-> {

                //In normal Kova the items SugarKova and sugarLess have splPricess...

                if (foodItem.Name == "SugarKova"){
                    splPrice = specialPricesModel!!.sugarKovaPrice
                }
                else if (foodItem.Name == "SugarLessKova"){
                    splPrice = specialPricesModel!!.sugarLessKovaPrice
                }
                //for all normalKova Category items the price is normalKovaPrice of user...
                else{
                    splPrice = specialPricesModel!!.normalKovaPrice
                }

            }

            "KovaSpl"->{
                //for all splKova items the price will be same..
                splPrice =  specialPricesModel!!.splKovaPrice
            }

            "Milk"->{

                if (foodItem.Name == "BuffaloMilk"){
                    splPrice = specialPricesModel!!.buffaloMilkPrice
                }
                else if (foodItem.Name == "CowMilk"){
                    splPrice = specialPricesModel!!.cowMilkPrice
                }
                else if (foodItem.Name == "SkimmedMilk"){
                    splPrice = specialPricesModel!!.skimmedMilkPrice
                }

            }

            "Ghee"->{

                if (foodItem.Name == "100% Boiled"){
                    splPrice = specialPricesModel!!.hundredBoiledPrice
                }
                else if (foodItem.Name == "70% Boiled"){
                    splPrice = specialPricesModel!!.seventyBoiledPrice
                }
                else if (foodItem.Name == "50% Boiled"){
                    splPrice = specialPricesModel!!.fiftyBoiledPrice
                }
            }
//
            "OtherSweets"->{
                //other sweets has many different items so based on the item we should get price
                if (foodItem.Name == "AgraPan"){
                    splPrice = specialPricesModel!!.agraPanPrice
                }
                else if (foodItem.Name == "KajuBytes"){
                    splPrice = specialPricesModel!!.kajuBytesPrice
                }
                else if (foodItem.Name == "Killi"){
                    splPrice = specialPricesModel!!.killiPrice
                }
                else if (foodItem.Name == "SoanPapdi"){
                    splPrice = specialPricesModel!!.soanPapdiPrice
                }
                else if (foodItem.Name == "SplSoanPapdi"){
                    splPrice = specialPricesModel!!.splSoanPapdiPrice
                }
            }

            "Hot"->{
                if (foodItem.Name == "Chakodi"){
                    splPrice = specialPricesModel!!.chakodiPrice
                }
                else if (foodItem.Name == "DhalMixture"){
                    splPrice = specialPricesModel!!.dhalMixturePrice
                }
                else if (foodItem.Name == "MarwadiMixture"){
                    splPrice = specialPricesModel!!.marwadiMixture
                }
                else if (foodItem.Name == "MoongDal"){
                    splPrice = specialPricesModel!!.moongDalPrice
                }
                else if (foodItem.Name == "SpecialMixture"){
                    splPrice = specialPricesModel!!.splMixturePrice
                }
            }

            else->{
                //if the item doesnt belong to any above categories its price will stay same...
                 splPrice = foodItem!!.Price

            }

        }
            foodItem?.Price = splPrice
            Log.i("spl price is:",""+foodItem?.Price)
            return foodItem

        }

    //for every foodItem we will change the price to userSpecific price i.e specialPrice by calling getSpecialPrice() method
    fun loadItems(itemName:String?) {
        viewModelScope.launch(Dispatchers.IO) {

            if (firebaseFoodItems.value == null) {
                FirebaseDatabase.getInstance().getReference("FoodItemsTesting").child(itemName.toString())
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            foodItemsList.clear()
                            if (snapshot.exists()) {
                                for (dataSnapshot in snapshot.children) {
                                    val foodItem: FoodItem? = dataSnapshot.getValue(FoodItem::class.java)

                                    Log.i(tag,"the item is "+foodItem!!.Name)
                                    //if the users special price snap is null then it will show normal prices
                                    val splPriceAddedItem = getSpecialPrice(foodItem)
                                    if (splPriceAddedItem!=null){
                                        foodItemsList.add(splPriceAddedItem)
                                    }

                                }
                                //java method
//                                Collections.sort(foodItemsList)
                                //kotlin method
                                foodItemsList.sort()
                                firebaseFoodItems.postValue(foodItemsList)


                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }
        }

    }

}

class FoodItemsViewModelFactory(private val repository: CartItemRepository) : ViewModelProvider.Factory {

    override fun<T: ViewModel> create(modelClass: Class<T>):T{
        if(modelClass.isAssignableFrom(FoodItemsViewModel::class.java)){
            return FoodItemsViewModel(repository) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }

}