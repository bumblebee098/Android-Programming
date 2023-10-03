package si.uni_lj.fri.pbd.miniapp3.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import si.uni_lj.fri.pbd.miniapp3.ui.FavoritesFragment
import si.uni_lj.fri.pbd.miniapp3.ui.SearchFragment

class SectionsPagerAdapter(fa: FragmentActivity, private val tabNumber: Int) : FragmentStateAdapter(fa!!) {

    /**
     * configure fragments
     */
    override fun createFragment(position: Int): Fragment {
        when(position) {
            0 -> return SearchFragment()
            1 -> return FavoritesFragment()
            else -> {
                return SearchFragment()
            }
        }
    }

    override fun getItemCount(): Int {
        return tabNumber
    }
}