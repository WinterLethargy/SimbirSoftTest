package com.example.simbirsofttest.feature.deal.recyclerview.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simbirsofttest.data.models.Deal
import com.example.simbirsofttest.feature.deal.recyclerview.viewHolders.DealViewHolder
import com.example.simbirsofttest.feature.deal.recyclerview.customCells.DealCellView

class DealListAdapter(
    private val onShowDealDetail: (deal: Deal) -> Unit,
    private val onCreateDeal: (hour: Int) -> Unit
): RecyclerView.Adapter<DealViewHolder>() {

    private val cellTypeByPosition: MutableMap<Int, CellType> = mutableMapOf()
    var deals: List<Deal> = listOf()
        set(value){
            field = value
            setCellTypeByPosition(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : DealViewHolder {
        return DealViewHolder(
            DealCellView(parent.context),
            onShowDealDetail,
            onCreateDeal,
            )
    }
    override fun getItemCount(): Int{
        val result = 24 + deals.size - deals.sumOf { it.duration }
        return  result;
    }

    override fun onBindViewHolder(holder: DealViewHolder, position: Int) {
        val cellType = cellTypeByPosition[position]!!
        when (cellType){
            is DealCellType -> holder.bind(cellType.deal)
            is HourCellType -> holder.bindEmptyHour(cellType.hour)
        }
    }

    private fun setCellTypeByPosition(deals: List<Deal>){
        cellTypeByPosition.clear()
        val dealByHourStart = deals.associateBy { it.startHour }
        var hour = 0
        var position = 0
        while(hour < 24){
            val deal = dealByHourStart[hour]
            if(deal == null)
            {
                cellTypeByPosition[position] = HourCellType(hour)
                hour++
            }
            else
            {
                cellTypeByPosition[position] = DealCellType(deal)
                hour += deal.duration
            }
            position++
        }
    }

    private sealed class CellType
    private class DealCellType(
        val deal: Deal
    ) : CellType()
    private class HourCellType(
        val hour: Int
    ) : CellType()
}