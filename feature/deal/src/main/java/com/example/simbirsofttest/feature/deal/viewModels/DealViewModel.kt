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
import com.example.simbirsofttest.feature.deal.extensions.readDeal
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
                    .catch { ex ->
                        Log.e(null, null, ex)
                        emit(null)
                    }
            else
                flowOf<Deal?>(null)
        }

    private val editDeal: Flow<Deal?> = savedStateHandle
        .getStateFlow<Bundle?>(EDIT_DEAL, getInitialDeal()?.toBundle())
        .map{ it?.readDeal() }

    private fun getInitialDeal() = when(initialMode){
        Mode.show -> null
        Mode.edit -> null
        Mode.create -> Deal(
            id = null,
            name = String.empty,
            description =  String.empty,
            date = route.run{
                if (year != null && month != null && day != null)
                    LocalDate.of(year, month, day)
                else
                    LocalDate.now()
            },
            startHour = route.hour ?: 0,
            duration = 1,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dealsByDay: Flow<List<Deal>> = editDeal
        .flatMapLatest { editDeal ->
            if(editDeal?.date == null)
                flowOf(emptyList())
            else
                dealRepository
                    .getByDay(editDeal.date)
                    .catch { exception ->
                        Log.e(null, null, exception)
                        emit(emptyList())
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

    private fun setupEditDealValidData(editDeal: Deal?, originalDeal: Deal?, dealsByDay: List<Deal>): EditDealUiState? {
        if(editDeal == null)
            return null

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

        return EditDealUiState(
            editDeal,
            crossDeals,
            incorrectTime,
        )
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
        setEditDeal(null)
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
            setEditDeal(null)
            return dealRepository.getDeal(id)
        }
        catch (ex: Exception){
            Log.e(null, null, ex)
            return null
        }
    }

    private fun setMode(mode: Mode) = savedStateHandle.set(MODE, mode.name)

    fun setEditDate(date: LocalDate) = uiState.value.editDeal?.deal?.let{
        setEditDeal(it.copy(date = date))
    }
    fun setEditStartHour(startHour: Int) = uiState.value.editDeal?.deal?.let{
        val delta = it.startHour - startHour
        val newDuration = it.duration + delta
        setEditDeal(it.copy(startHour = startHour, duration = newDuration))
    }
    fun setEditEndHour(endHour: Int) = uiState.value.editDeal?.deal?.let{
        setEditDeal(it.copy(duration = endHour - it.startHour))
    }
    fun setEditName(name: String) = uiState.value.editDeal?.deal?.let{
        setEditDeal(it.copy(name = name))
    }
    fun setEditDescription(description: String) = uiState.value.editDeal?.deal?.let{
        setEditDeal(it.copy(description = description))
    }

    private fun setEditDeal(deal: Deal?) = savedStateHandle.set(EDIT_DEAL, deal?.toBundle())

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
        const val EDIT_DEAL = "EDIT_DEAL"
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