package com.sakuraweb.fotopota.megacalc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_op_pad.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OpPad.newInstance] factory method to
 * create an instance of this fragment.
 */
class OpPad : Fragment() {
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

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ) : View? {
        val pad = inflater.inflate(R.layout.fragment_op_pad, container, false)
        val main : MainActivity = activity as MainActivity

        pad.btnPlus.setOnClickListener { main.clickOpPad(getString(R.string.pad_plus)) }
        pad.btnMinus.setOnClickListener { main.clickOpPad(getString(R.string.pad_minus)) }
        pad.btnMul.setOnClickListener { main.clickOpPad(getString(R.string.pad_mul)) }
        pad.btnDiv.setOnClickListener { main.clickOpPad(getString(R.string.pad_div)) }
        pad.btnRoot.setOnClickListener { main.clickOpPad(getString(R.string.pad_root)) }
        pad.btnAC2.setOnClickListener { main.clickOpPad(getString(R.string.pad_ac)) }
        return pad
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OpPad.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OpPad().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}