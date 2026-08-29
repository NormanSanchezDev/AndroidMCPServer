package com.acme.shop.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.acme.shop.R

class CartFragment : Fragment() {

    private val viewModel: CheckoutViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<android.widget.Button>(R.id.payButton).setOnClickListener {
            viewModel.submit(cartId = "cart-42")
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CheckoutUi.Success -> {
                    findNavController().navigate(R.id.action_cartToSummary)
                }

                is CheckoutUi.Error -> {
                    Toast.makeText(requireContext(), "Checkout failed", Toast.LENGTH_SHORT).show()
                }

                CheckoutUi.Loading -> Unit
            }
        }
    }
}