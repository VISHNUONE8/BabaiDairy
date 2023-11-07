package com.andrayudu.sureshdiaryfoods.fragments

import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.andrayudu.sureshdiaryfoods.HomeActivityViewModel
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.FragmentHomeBinding
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.db.FoodItemDatabase
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.ui.CartActivity
import com.andrayudu.sureshdiaryfoods.ui.FoodItemsActivity
import com.andrayudu.sureshdiaryfoods.ui.FoodItemsViewModel
import com.andrayudu.sureshdiaryfoods.ui.FoodItemsViewModelFactory

/*Done clearCoding , mostly no testing required...  , check livedata if possible....*/
class HomeFragment : Fragment() {

    private val TAG = "HomeFragment"

    private val sharedViewModel: HomeActivityViewModel by activityViewModels()

    private lateinit var foodIntent: Intent
    private lateinit var foodItemsViewModel: FoodItemsViewModel

    //UI components
    private lateinit var tTotalCost: TextView
    private lateinit var tCartQuantity: TextView
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        //here inside getInstance() we can also pass mContext but still it is preferred to pass application context
        val dao = FoodItemDatabase.getInstance(requireActivity().application).cartItemDao
        val repository = CartItemRepository(dao)
        val factory = FoodItemsViewModelFactory(repository)
        foodItemsViewModel = ViewModelProvider(this, factory)[FoodItemsViewModel::class.java]


        //this is used for making the scrollingText scrolling..
        binding.outstandingScrollTV.isSelected = true

        foodIntent = Intent(mContext, FoodItemsActivity::class.java)

        initViews()
        initClickListeners()
        initObservers()

        sharedViewModel.subscribeToSDF()
        sharedViewModel.loadUserData()

        return binding.root
    }

    private fun initObservers() {

        foodItemsViewModel.cartItems.observe(viewLifecycleOwner) {
            updateCartUI(it)
        }

        sharedViewModel.userLive.observe(viewLifecycleOwner) { userDetails ->
            userDetails?.let {
                binding.outstandingScrollTV.text =
                    getString(R.string.scrolling_text, userDetails.Name, userDetails.Outstanding)
            }
        }
    }

    private fun initClickListeners() {

        binding.oilCardview.setOnClickListener {

            foodIntent.putExtra("itemName", "Oil")
            startActivity(foodIntent)

        }
        binding.gheeCardview.setOnClickListener {

            foodIntent.putExtra("itemName", "Ghee")
            startActivity(foodIntent)


        }
        binding.milkCardview.setOnClickListener {
            foodIntent.putExtra("itemName", "Milk")
            startActivity(foodIntent)


        }
        binding.otherSweetsCardview.setOnClickListener {
            foodIntent.putExtra("itemName", "OtherSweets")
            startActivity(foodIntent)


        }

        binding.normalKovaTV.setOnClickListener {

            foodIntent.putExtra("itemName", "Kova")
            startActivity(foodIntent)
        }
        binding.speciallKovaTV.setOnClickListener {

            foodIntent.putExtra("itemName", "SpecialKova")
            startActivity(foodIntent)
        }
        binding.sugarKovaTV.setOnClickListener {

            foodIntent.putExtra("itemName", "SugarKova")
            startActivity(foodIntent)
        }
        binding.sugarLessKovaTV.setOnClickListener {

            foodIntent.putExtra("itemName", "SugarLessKova")
            startActivity(foodIntent)
        }
        binding.kovaLayout.setOnClickListener {
            expand()
        }
        binding.mixtureCardview.setOnClickListener {
            foodIntent.putExtra("itemName", "Hot")
            startActivity(foodIntent)

        }
        binding.paneerCardView.setOnClickListener {
            foodIntent.putExtra("itemName", "Paneer")
            startActivity(foodIntent)

        }

        binding.bCart.setOnClickListener {
            startActivity(Intent(mContext, CartActivity::class.java))
        }
    }

    private fun initViews() {
        tTotalCost = binding.cartView.findViewById(R.id.t_total_price)
        tCartQuantity = binding.cartView.findViewById(R.id.t_cart_count)
        binding.kovaLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }


    //updates the cartUI bar at the bottom
    private fun updateCartUI(cartItems: List<CartItem>?) {
        if (!cartItems.isNullOrEmpty()) {
            binding.cartView.visibility = View.VISIBLE
            var price = 0
            var quantity = 0
            for (cartItem in cartItems) {
                price += (cartItem.Price * cartItem.Quantity)
                quantity += cartItem.Quantity
            }
            tCartQuantity.text = cartItems.size.toString()
            tTotalCost.text = getString(R.string.rupee_symbol_new, price.toString())

        } else {
            binding.cartView.visibility = View.GONE
            tCartQuantity.text = "0"
            tTotalCost.text = getString(R.string.rupee_symbol_new, "0")
        }

    }

    //expands and contracts the Kova cardView
    private fun expand() {
        val v = if (binding.hiddenLayout.visibility == View.GONE) View.VISIBLE
        else View.GONE
        TransitionManager.beginDelayedTransition(binding.kovaLayout, AutoTransition())
        binding.hiddenLayout.visibility = v

    }


}