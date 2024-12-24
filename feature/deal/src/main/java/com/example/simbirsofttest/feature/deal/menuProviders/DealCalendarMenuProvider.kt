package com.example.simbirsofttest.feature.deal.menuProviders

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.example.simbirsofttest.feature.deal.R

class DealCalendarMenuProvider (
    private val addAction: () -> Unit
): MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_deal_calendar, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_add -> {
                addAction()
                return true
            }
        }
        return false
    }
}