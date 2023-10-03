package si.uni_lj.fri.pbd.miniapp3.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import si.uni_lj.fri.pbd.miniapp3.R
import si.uni_lj.fri.pbd.miniapp3.adapters.SectionsPagerAdapter
import si.uni_lj.fri.pbd.miniapp3.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private const val NUM_OF_TABS = 2
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureTabLayout()
    }

    /**
     * function configures tab layout,
     * calls SectionsPagerAdapter and sets the titles of tabs
     */
    private fun configureTabLayout() {
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewpager2
        val VPadapter = SectionsPagerAdapter(this, NUM_OF_TABS)
        viewPager.adapter = VPadapter

        TabLayoutMediator(tabLayout, viewPager) {
            tab, position ->
            when(position) {
                0 -> tab.setText(R.string.search_by_ingredient)
                1 -> tab.setText(R.string.favorite)
            }
        }.attach()

    }

}