package com.example.simbirsofttest.feature.deal.menuProviders

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.example.simbirsofttest.feature.deal.R
import com.example.simbirsofttest.feature.deal.viewModels.DealViewModel

internal class DealDetailMenuProvider(
    private val saveAction: () -> Unit,
    private val editAction: () -> Unit,
    private val endEditAction: () -> Unit,
    var mode: DealViewModel.Mode
) : MenuProvider{
    var isValidData: Boolean? = null
    private lateinit var saveItem: MenuItem
    private lateinit var editItem: MenuItem
    private lateinit var endEditItem: MenuItem

    override fun onPrepareMenu(menu: Menu) {
        when(mode){
            DealViewModel.Mode.create ->{
                editItem.isVisible = false
                endEditItem.isVisible = false
                saveItem.isVisible = true
                setSaveIconItem()
            }
            DealViewModel.Mode.show -> {
                editItem.isVisible = true
                endEditItem.isVisible = false
                saveItem.isVisible = false
            }
            DealViewModel.Mode.edit -> {
                editItem.isVisible = false
                endEditItem.isVisible = true
                saveItem.isVisible = true
                setSaveIconItem()
            }
        }
    }

    private fun setSaveIconItem(){
        isValidData?.let{
            if(it)
                saveItem.setIcon(com.example.simbirsofttest.core.R.drawable.ic_submit_enabled)
            else
                saveItem.setIcon(com.example.simbirsofttest.core.R.drawable.ic_submit_disabled)
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_deal_detail, menu)
        saveItem = menu.findItem(R.id.action_save)
        editItem = menu.findItem(R.id.action_edit)
        endEditItem = menu.findItem(R.id.action_cancel)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_save -> {
                isValidData?.let {
                    if(it) saveAction()
                }
                return true
            }
            R.id.action_edit -> {
                editAction()
                return true
            }
            R.id.action_cancel ->{
                endEditAction()
                return true
            }
        }
        return false
    }
}