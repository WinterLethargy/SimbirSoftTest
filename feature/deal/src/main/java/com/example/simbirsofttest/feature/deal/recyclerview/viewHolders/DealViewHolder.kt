package com.example.simbirsofttest.feature.deal.recyclerview.viewHolders

import androidx.recyclerview.widget.RecyclerView
import com.example.simbirsofttest.data.models.Deal
import com.example.simbirsofttest.feature.deal.recyclerview.customCells.DealCellView

class DealViewHolder(
    private val view: DealCellView,
    private val onShowDealDetail: (deal: Deal) -> Unit,
    private val onCreateDeal: (hour: Int) -> Unit
) : RecyclerView.ViewHolder(view) {
    private var deal: Deal? = null
    private var hour: Int? = null
    init {
        view.setOnClickListener{
            deal?.let { onShowDealDetail(it) } ?: hour?.let{ onCreateDeal(it) }
        }
    }
    fun bind(deal: Deal){
        hour = null
        this.deal = deal

        view.dealName = deal.name
        view.hourStart = deal.startHour
        view.duration = deal.duration
    }
    fun bindEmptyHour(hour: Int){
        deal = null
        this.hour = hour

        view.dealName = null
        view.hourStart = hour
        view.duration = 0
    }
}