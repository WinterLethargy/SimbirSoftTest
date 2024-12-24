package com.example.simbirsofttest.feature.deal.navigation

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.fragment.fragment
import com.example.simbirsofttest.feature.deal.R
import com.example.simbirsofttest.feature.deal.fragments.DealCalendarFragment
import com.example.simbirsofttest.feature.deal.fragments.DealFragment
import kotlinx.serialization.Serializable

@Serializable
internal data class DealRoute private constructor(
    val type: Type,
    val id: Long?,
    val year: Int?,
    val month: Int?,
    val day: Int?,
    val hour: Int?,
) {
    enum class Type{
        creation,
        detail
    }

    companion object{
        val EmptyDealCreation = DealRoute(
            Type.creation,
            null,
            null,
            null,
            null,
            null,
        )

        fun creation(
            year: Int?,
            month: Int?,
            day: Int?,
            hour: Int?,): DealRoute{
            return DealRoute(
                type = Type.creation,
                id = null,
                year = year,
                month = month,
                day = day,
                hour = hour,
            )
        }
        fun detail(id: Long): DealRoute{
            return DealRoute(
                type = Type.detail,
                id = id,
                year = null,
                month = null,
                day = null,
                hour = null,
            )
        }
    }
}

@Serializable
object DealCalendarRoute

context(Context)
fun NavGraphBuilder.dealFragments() {
    fragment<DealCalendarFragment, DealCalendarRoute> {
        label = getString(R.string.deal_calendar_label)
    }
    fragment<DealFragment, DealRoute>()
}