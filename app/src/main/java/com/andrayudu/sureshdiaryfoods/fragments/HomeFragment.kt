package com.andrayudu.sureshdiaryfoods.fragments

import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.andrayudu.sureshdiaryfoods.FoodItemsViewModelFactory
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.FragmentHomeBinding
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.db.FoodItemDatabase
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.ui.CartActivity
import com.andrayudu.sureshdiaryfoods.ui.FoodItemsActivity
import com.andrayudu.sureshdiaryfoods.ui.FoodItemsViewModel

class HomeFragment : Fragment() {


    lateinit var binding: FragmentHomeBinding
    lateinit var mContext: Context
    lateinit var FoodIntent: Intent
    //this viewmodel is shared b/w foodItemsActivity and this fragment (normally by viewModels() should be added below but it is working without it..)
    private lateinit var homeFragmentViewModel: FoodItemsViewModel

    private lateinit var tTotalCost: TextView
    private lateinit var tCartQuantity: TextView



    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    fun expand(){
        val v = if(binding.hiddenLayout.visibility == View.GONE)
            View.VISIBLE
        else
            View.GONE
        TransitionManager.beginDelayedTransition(binding.kovaLayout,AutoTransition())
        binding.hiddenLayout.visibility = v


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        //here inside getInstance() we can also pass mContext but still it is preferred to pass application context
        val dao = FoodItemDatabase.getInstance(requireActivity().application).cartItemDao
        val repository = CartItemRepository(dao)
        val factory = FoodItemsViewModelFactory(repository)
        homeFragmentViewModel = ViewModelProvider(this,factory)[FoodItemsViewModel::class.java]


        tTotalCost = binding.cartView.findViewById(R.id.t_total_price)
        tCartQuantity = binding.cartView.findViewById(R.id.t_cart_count)


        binding.kovaLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)


        FoodIntent = Intent(mContext, FoodItemsActivity::class.java)

        binding.oilCardview.setOnClickListener {

            FoodIntent.putExtra("itemName", "Oil")
            startActivity(FoodIntent)

        }
        binding.gheeCardview.setOnClickListener {
            Log.i("TAG", "The  ghee cardbiew is clicked")

            FoodIntent.putExtra("itemName", "Ghee")
            startActivity(FoodIntent)


        }
        binding.milkCardview.setOnClickListener {
            Log.i("TAG", "The  Milk cardbiew is clicked")
            FoodIntent.putExtra("itemName", "Milk")
            startActivity(FoodIntent)


        }
        binding.otherSweetsCardview.setOnClickListener {
            Log.i("TAG", "The  Milk cardbiew is clicked")
            FoodIntent.putExtra("itemName", "OtherSweets")
            startActivity(FoodIntent)


        }

        binding.normalKovaTV.setOnClickListener {

            FoodIntent.putExtra("itemName", "Kova")
            startActivity(FoodIntent)
        }
        binding.speciallKovaTV.setOnClickListener {

            FoodIntent.putExtra("itemName", "SpecialKova")
            startActivity(FoodIntent)
        }
        binding.kovaLayout.setOnClickListener {
            expand()
        }
        binding.mixtureCardview.setOnClickListener {
            FoodIntent.putExtra("itemName", "Hot")
            startActivity(FoodIntent)

        }

        binding.bCart.setOnClickListener {
            startActivity(Intent(mContext, CartActivity::class.java))
        }

        homeFragmentViewModel.cartItems.observe(viewLifecycleOwner, Observer {
            updateCartUI(it)

            Log.i("TAG", "The list from homeFragment is:$it")
        })

        return binding.root
    }

    private fun updateCartUI(cartItems: List<CartItem>?) {
        if(cartItems!=null && cartItems.size > 0){
            binding.cartView.visibility = View.VISIBLE
            var price =0
            var quantity = 0

            for (cartItem in cartItems) {
                price = price +( cartItem.Price.toInt() * cartItem.Quantity.toInt())
                quantity = quantity + cartItem.Quantity.toInt()
            }
            tCartQuantity.setText(cartItems.size.toString())
            tTotalCost.setText(getString(R.string.rupee_symbol) + price.toString())

        }
        else
        {
            binding.cartView.setVisibility(View.GONE)
            tCartQuantity.text = "0"
            tTotalCost.text = getString(R.string.rupee_symbol) + "0"
        }

    }


}