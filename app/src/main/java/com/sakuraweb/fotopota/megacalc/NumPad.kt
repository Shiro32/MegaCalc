package com.sakuraweb.fotopota.megacalc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_num_pad.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NumPad.newInstance] factory method to
 * create an instance of this fragment.
 */
class NumPad : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        val pad = inflater.inflate(R.layout.fragment_num_pad, container, false)
        val lcd: MainActivity = activity as MainActivity

        pad.btn0.setOnClickListener { lcd.clickNumPad("0") }
        pad.btn1.setOnClickListener { lcd.clickNumPad("1") }
        pad.btn2.setOnClickListener { lcd.clickNumPad("2") }
        pad.btn3.setOnClickListener { lcd.clickNumPad("3") }
        pad.btn4.setOnClickListener { lcd.clickNumPad("4") }
        pad.btn5.setOnClickListener { lcd.clickNumPad("5") }
        pad.btn6.setOnClickListener { lcd.clickNumPad("6") }
        pad.btn7.setOnClickListener { lcd.clickNumPad("7") }
        pad.btn8.setOnClickListener { lcd.clickNumPad("8") }
        pad.btn9.setOnClickListener { lcd.clickNumPad("9") }
        pad.btnPoint.setOnClickListener { lcd.clickNumPad(".") }
        pad.btnBS.setOnClickListener { lcd.clickNumPad("BS") }
        return pad
    }






    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NumPad.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NumPad().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}