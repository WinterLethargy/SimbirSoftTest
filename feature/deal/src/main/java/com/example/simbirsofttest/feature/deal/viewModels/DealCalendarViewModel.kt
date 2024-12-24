package com.example.simbirsofttest.feature.deal.viewModels

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simbirsofttest.data.models.Deal
import com.example.simbirsofttest.data.repositories.IDealRepository
import com.example.simbirsofttest.feature.deal.extensions.readLocalDate
import com.example.simbirsofttest.feature.deal.extensions.toBundle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DealCalendarViewModel @Inject constructor(
    private val dealRepository: IDealRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val date: Flow<LocalDate> = savedStateHandle
       .getStateFlow<Bundle?>(CURRENT_CALENDAR_DATE, null)
       .map { it?.readLocalDate() ?: LocalDate.now() }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val deals: Flow<List<Deal>> = date
        .flatMapLatest { date ->
            dealRepository
                .getByDay(date)
                .catch { exception ->
                    Log.e(null, null, exception)
                    emit(emptyList())
                }
        }

    val uiState: StateFlow<DealCalendarUiState> = combine(
        date,
        deals,
        ::DealCalendarUiState
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DealCalendarUiState()
    )

    fun setDay(date: LocalDate) = savedStateHandle.set(CURRENT_CALENDAR_DATE, date.toBundle())

    companion object{
        const val CURRENT_CALENDAR_DATE = "CURRENT_CALENDAR_DATE"
    }
}

data class DealCalendarUiState(
    val date: LocalDate = LocalDate.now(),
    val deals: List<Deal> = emptyList(),
)