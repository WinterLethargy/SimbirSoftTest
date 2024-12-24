package com.example.simbirsofttest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.createGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.simbirsofttest.databinding.ActivityMainBinding
import com.example.simbirsofttest.feature.deal.navigation.DealCalendarRoute
import com.example.simbirsofttest.feature.deal.navigation.dealFragments
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val navController by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host)
            .let{ it as NavHostFragment }
            .navController
    }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavController()
        setupAppBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun setupNavController(){
        navController.graph = navController.createGraph(
            startDestination = DealCalendarRoute
        ){
            dealFragments()
        }
    }

    private fun setupAppBar() {
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
