package com.example.simbirsofttest.feature.deal.viewModels

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.simbirsofttest.core.utils.empty
import com.example.simbirsofttest.data.models.Deal
import com.example.simbirsofttest.data.models.dateTimeEnd
import com.example.simbirsofttest.data.models.dateTimeStart
import com.example.simbirsofttest.data.repositories.IDealRepository
import com.example.simbirsofttest.feature.deal.extensions.readLocalDate
import com.example.simbirsofttest.feature.deal.extensions.toBundle
import com.example.simbirsofttest.feature.deal.navigation.DealRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class DealViewModel @Inject constructor(
    private val dealRepository: IDealRepository,
    private val savedStateHandle: SavedStateHandle,
)  : ViewModel() {
    val route: DealRoute = savedStateHandle.toRoute()
    val initialMode: Mode = when(route.type) {
        DealRoute.Type.creation -> Mode.valueOf(Mode.create.name)
        DealRoute.Type.detail -> Mode.valueOf(Mode.show.name)
    }

    private val mode: StateFlow<Mode> = savedStateHandle
        .getStateFlow(MODE, initialMode.name)
        .map{ Mode.valueOf(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            initialMode
        )

    private val id: StateFlow<Long?> = savedStateHandle
        .getStateFlow(ID, route.id)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val deal: Flow<Deal?> = id
        .flatMapLatest {
            if(it != null)
                dealRepository.getFlow(it)
                    .catch {
                        Log.e(null, null, it)
                        emit(null)
                    }
            else
                flowOf<Deal?>(null)
        }

    private val editDate: Flow<LocalDate?> = savedStateHandle
        .getStateFlow<Bundle?>(EDIT_DATE, null)
        .map {
            if(it == null && route.type == DealRoute.Type.creation){
                if(route.year != null && route.month != null && route.day != null)
                    LocalDate.of(
                        route.year,
                        route.month,
                        route.day,
                    )
                else
                    LocalDate.now()
            }
            else{
                it?.readLocalDate()
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dealsByDay: Flow<List<Deal>> = editDate
        .flatMapLatest { date ->
            if(date == null)
                flowOf(emptyList())
            else
                dealRepository
                    .getByDay(date)
                    .catch { exception ->
                        Log.e(null, null, exception)
                        emit(emptyList())
                    }
        }

    private val editStartHour: StateFlow<Int?> = savedStateHandle
        .getStateFlow(EDIT_START_HOUR, if(route.type == DealRoute.Type.creation) route.hour ?: 0 else null)

    private val editEndHour: StateFlow<Int?> = savedStateHandle
        .getStateFlow(
            EDIT_END_HOUR,
            if(route.type == DealRoute.Type.creation) route.hour?.inc() ?: 1 else null)

    private val editName: StateFlow<String?> = savedStateHandle
        .getStateFlow(EDIT_NAME, null)

    private val editDescription: StateFlow<String?> = savedStateHandle
        .getStateFlow(EDIT_DESCRIPTION, null)

    private val editDealWithoutDescription: Flow<Deal?> = combine(
        id,
        editDate,
        editStartHour,
        editEndHour,
        editName
    ) { id, date, startHour, endHour, name ->
        if (date == null) {
            null
        } else {
            val preserve24endHour = if (endHour == 0) 24 else endHour
            Deal(
                id = id,
                date = date,
                startHour = startHour ?: 0,
                duration = if (preserve24endHour != null && startHour != null) preserve24endHour - startHour else 0,
                name = name ?: String.empty,
                description = String.empty
            )
        }
    }

    private val editDeal: Flow<EditDealUiState?> = combine(
        editDealWithoutDescription,
        editDescription
    ) { dealWithoutDescription, description ->
        if (dealWithoutDescription == null) {
            null
        } else {
            EditDealUiState(
                deal = dealWithoutDescription.copy(description = description ?: String.empty),
                crossDeals = false,
                incorrectTime = false,
            )
        }
    }

    val uiState: StateFlow<DealUiState> = combine(
        mode,
        deal,
        editDeal,
        dealsByDay,
        { mode, deal, editDeal, dealsByDay ->
            when(mode){
                Mode.show -> DealUiState(
                    mode,
                    deal,
                    null
                )
                Mode.create -> DealUiState(
                    mode,
                    null,
                    setupEditDealValidData(editDeal, null, dealsByDay)
                )
                Mode.edit -> DealUiState(
                    mode,
                    deal,
                    setupEditDealValidData(editDeal, deal, dealsByDay)
                )
            }
        }
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DealUiState(
            initialMode
        )
    )

    private fun setupEditDealValidData(editDealUiState: EditDealUiState?, originalDeal: Deal?, dealsByDay: List<Deal>): EditDealUiState? {
        if(editDealUiState == null)
            return null
        val editDeal = editDealUiState.deal
        val dateTimeStart = editDeal.dateTimeStart
        val dateTimeEnd = editDeal.dateTimeEnd
        val incorrectTime = dateTimeStart >= dateTimeEnd

        var localDealsByDay = dealsByDay
        if(originalDeal != null)
            localDealsByDay = localDealsByDay - originalDeal

        val crossDeals =
            if(incorrectTime)
                false
            else
                !localDealsByDay.all { it.dateTimeStart >= dateTimeEnd || it.dateTimeEnd <= dateTimeStart }

        return editDealUiState.copy(incorrectTime = incorrectTime, crossDeals = crossDeals)
    }

    fun edit(){
        if(uiState.value.mode != Mode.show)
            return

        val deal = uiState.value.deal!!
        setEditDeal(deal)
        setMode(Mode.edit)
    }

    fun cancelEdit(){
        if(uiState.value.mode != Mode.edit)
            return

        setMode(Mode.show)
        setNullEditDeal()
    }

    suspend fun save(): Deal?{
        val deferred = viewModelScope.async {
            when(uiState.value.mode) {
                Mode.show -> null
                Mode.edit -> handleSave(uiState.value.editDeal!!)
                Mode.create -> handleSave(uiState.value.editDeal!!)
            }
        }
        return deferred.await()
    }

    private suspend fun handleSave(editDealUiState: EditDealUiState): Deal?{
        if(editDealUiState.crossDeals || editDealUiState.incorrectTime)
            return null

        try {
            val deal = editDealUiState.deal
            val id: Long
            if(deal.id == null){
                id = dealRepository.insert(deal)
                savedStateHandle[ID] = id
            }
            else{
                dealRepository.update(deal)
                id = deal.id!!
            }
            setMode(Mode.show)
            setNullEditDeal()
            return dealRepository.getDeal(id)
        }
        catch (ex: Exception){
            Log.e(null, null, ex)
            return null
        }
    }

    private fun setMode(mode: Mode) = savedStateHandle.set(MODE, mode.name)

    fun setEditDate(date: LocalDate?) = savedStateHandle.set(EDIT_DATE, date?.toBundle())
    fun setEditStartHour(startHour: Int?) = savedStateHandle.set(EDIT_START_HOUR, startHour)
    fun setEditEndHour(endHour: Int?) = savedStateHandle.set(EDIT_END_HOUR, endHour)
    fun setEditName(name: String?) = savedStateHandle.set(EDIT_NAME, name)
    fun setEditDescription(description: String?) = savedStateHandle.set(EDIT_DESCRIPTION, description)

    private fun setNullEditDeal(){
        setEditDate(null)
        setEditStartHour(null)
        setEditEndHour(null)
        setEditName(null)
        setEditDescription(null)
    }

    private fun setEditDeal(deal: Deal){
        setEditDate(deal.date)
        setEditStartHour(deal.startHour)
        val preserve24endHour = deal.startHour + deal.duration
        val endHour = if (preserve24endHour == 24) 0 else preserve24endHour
        setEditEndHour(endHour)
        setEditName(deal.name)
        setEditDescription(deal.description)
    }

    enum class Mode{
        create,
        show,
        edit;
        val isEditableMode get() = when(this){
            show -> false
            edit -> true
            create -> true
        }
    }

    companion object{
        const val MODE = "MODE"

        const val ID = "ID"

        const val EDIT_DATE = "EDIT_DATE"
        const val EDIT_START_HOUR = "EDIT_START_HOUR"
        const val EDIT_END_HOUR = "EDIT_END_HOUR"
        const val EDIT_NAME = "EDIT_NAME"
        const val EDIT_DESCRIPTION = "EDIT_DESCRIPTION"
    }
}

internal data class DealUiState(
    val mode: DealViewModel.Mode = DealViewModel.Mode.edit,
    val deal: Deal? = null,
    val editDeal: EditDealUiState? = null
)

internal data class EditDealUiState(
    val deal: Deal,
    val crossDeals: Boolean,
    val incorrectTime: Boolean,
)