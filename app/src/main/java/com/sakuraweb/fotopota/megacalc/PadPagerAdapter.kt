package com.sakuraweb.fotopota.megacalc

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

const val FRAGMENT_SETTINGS = 0
const val FRAGMENT_NUM_PAD = 1
const val FRAGMENT_OP_PAD = 2

class PadPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        lateinit var fm: Fragment

        when( position ) {
            FRAGMENT_SETTINGS -> {
                fm = SettingsFragment() as Fragment
            }

            FRAGMENT_NUM_PAD -> {
                fm = NumPad.newInstance("A", "B")
            }
            FRAGMENT_OP_PAD -> {
                fm = OpPad.newInstance( "C", "D")
            }
        }
        return fm
    }
}