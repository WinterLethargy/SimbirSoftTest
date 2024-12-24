package com.example.simbirsofttest.feature.deal.callbacks

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem

class EditableActionEnableCallback(
    private val isEditableActionEnable: () -> Boolean
): ActionMode.Callback {
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        if(!isEditableActionEnable()){
            menu.removeItem(android.R.id.cut)
            menu.removeItem(android.R.id.paste)
            menu.removeItem(android.R.id.pasteAsPlainText)
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {}
}