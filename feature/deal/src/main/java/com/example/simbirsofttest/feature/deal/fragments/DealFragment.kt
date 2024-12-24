package com.example.simbirsofttest.feature.deal.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.simbirsofttest.data.models.Deal
import com.example.simbirsofttest.data.models.dateTimeEnd
import com.example.simbirsofttest.data.models.dateTimeStart
import com.example.simbirsofttest.feature.deal.viewModels.DealViewModel.Mode
import com.example.simbirsofttest.feature.deal.R
import com.example.simbirsofttest.feature.deal.callbacks.EditableActionEnableCallback
import com.example.simbirsofttest.feature.deal.constants.BundleConstant
import com.example.simbirsofttest.feature.deal.databinding.PageDealBinding
import com.example.simbirsofttest.feature.deal.viewModels.DealViewModel
import com.example.simbirsofttest.feature.deal.viewModels.EditDealUiState
import com.example.simbirsofttest.feature.deal.extensions.setEditableBehaviour
import com.example.simbirsofttest.feature.deal.extensions.toBundle
import com.example.simbirsofttest.feature.deal.menuProviders.DealDetailMenuProvider
import com.example.simbirsofttest.feature.deal.navigation.DealRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
internal class DealFragment : Fragment() {
    private val viewModel: DealViewModel by viewModels()

    private var _binding: PageDealBinding? = null
    private val binding get() = _binding!!

    private val menuProvider: DealDetailMenuProvider by lazy {
        DealDetailMenuProvider(
            mode = viewModel.uiState.value.mode,
            editAction = viewModel::edit,
            endEditAction = {
                viewModel.cancelEdit()
                hideKeyboard()
            },
            saveAction = {
                lifecycleScope.launch {
                    val savedDeal = viewModel.save()
                    hideKeyboard()
                    savedDeal?.let {
                        findNavController()
                            .previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(BundleConstant.CREATED_DEAL_DATE,
                                it.date.toBundle()
                            )
                    }
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PageDealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTitle()
        setupView()
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
        launchRepeatOnLifeCycle()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun launchRepeatOnLifeCycle(){
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.uiState.collect {
                    when(it.mode){
                        Mode.edit -> it.editDeal?.let(this@DealFragment::setEditDeal)
                        Mode.create -> it.editDeal?.let(this@DealFragment::setEditDeal)
                        Mode.show -> it.deal?.let(this@DealFragment::setShowDeal)
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.uiState
                    .map { it.mode }
                    .distinctUntilChanged()
                    .collect(this@DealFragment::onModeChanged)
            }
        }
    }

    private fun setShowDeal(deal: Deal){
        binding.apply {
            datePicker.text = deal.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            timeStartPicker.text = deal.dateTimeStart.format(DateTimeFormatter.ofPattern("HH:mm"))
            timeEndPicker.text = deal.dateTimeEnd.format(DateTimeFormatter.ofPattern("HH:mm"))
            dealNameText.setText(deal.name)
            dealDescriptionText.setText(deal.description)
        }
    }

    private fun setEditDeal(editDeal: EditDealUiState){
        binding.apply {
            val deal = editDeal.deal
            datePicker.text = deal.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            timeStartPicker.text = deal.dateTimeStart.format(DateTimeFormatter.ofPattern("HH:mm"))
            timeEndPicker.text = deal.dateTimeEnd.format(DateTimeFormatter.ofPattern("HH:mm"))
            if(dealNameText.text?.toString() != deal.name){
                dealNameText.setText(deal.name)
            }
            if(dealDescriptionText.text?.toString() != deal.description){
                dealDescriptionText.setText(deal.description)
            }

            if(editDeal.incorrectTime){
                timeStartPicker.setTextColor(Color.RED)
                timeEndPicker.setTextColor(Color.RED)
            }
            else {
                setTimePickersDefaultColor()
            }

            if(editDeal.crossDeals)
                crossDealWarning.visibility = View.VISIBLE
            else
                crossDealWarning.visibility = View.GONE

            val newIsValidData = editDeal.run { !(crossDeals || incorrectTime) && deal.name.isNotBlank() }
            if(menuProvider.isValidData != newIsValidData){
                menuProvider.isValidData = newIsValidData
                requireActivity().invalidateOptionsMenu()
            }
        }
    }

    private fun setTimePickersDefaultColor(){
        binding.apply {
            val defaultTextColor = TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, defaultTextColor, true).let {
                val color = requireContext().getColor(defaultTextColor.resourceId)
                timeStartPicker.setTextColor(color)
                timeEndPicker.setTextColor(color)
            }
        }
    }

    private fun onModeChanged(mode: Mode){
        menuProvider.mode = mode
        when (mode){
            Mode.show -> {
                menuProvider.isValidData = null
                setNotEditable()
                requireActivity().invalidateOptionsMenu()
            }
            Mode.edit -> setEditable()
            Mode.create -> setEditable()
        }
    }

    private fun setNotEditable(){
        binding.apply {
            dealNameText.setEditableBehaviour(false)
            dealDescriptionText.setEditableBehaviour(false)
            setTimePickersDefaultColor()
            crossDealWarning.visibility = View.GONE
        }
    }

    private fun setEditable(){
        binding.apply {
            dealNameText.setEditableBehaviour(true)
            dealDescriptionText.setEditableBehaviour(true)
        }
    }

    private fun setupTitle(){
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            when(viewModel.route.type){
                DealRoute.Type.creation -> getString(R.string.deal_creation_label)
                DealRoute.Type.detail -> getString(R.string.deal_detail_label)
            }
    }
    
    private fun setupView(){
        binding.apply {
            datePicker.setOnClickListener{
                showDatePickerDialog()
            }
            timeStartPicker.setOnClickListener{
                selectStartHour()
            }
            timeEndPicker.setOnClickListener {
                selectEndHour()
            }
            dealNameText.doOnTextChanged { text, start, before, count -> viewModel.setEditName(text?.toString()) }
            dealDescriptionText.doOnTextChanged { text, start, before, count -> viewModel.setEditDescription(text?.toString()) }
            EditableActionEnableCallback{
                when(viewModel.uiState.value.mode){
                    Mode.show -> false
                    Mode.edit -> true
                    Mode.create -> true
                }
            }.let {
                dealNameText.customSelectionActionModeCallback = it
                dealDescriptionText.customSelectionActionModeCallback = it
            }
        }
    }

    private fun showDatePickerDialog() {
        val uiState = viewModel.uiState.value
        if(uiState.mode == Mode.show)
            return;

        val date = uiState.editDeal?.deal?.date ?: LocalDate.now()

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                viewModel.setEditDate(LocalDate.of(year, month + 1, dayOfMonth))
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        ).show()

    }

    private fun selectStartHour(){
        val uiState = viewModel.uiState.value
        if(uiState.mode == DealViewModel.Mode.show)
            return;

        val startHour = uiState.editDeal?.deal?.startHour ?: LocalTime.now().hour

        showTimePickerDialog(startHour, 0){ hour, minute ->
            viewModel.setEditStartHour(hour)
        }
    }

    private fun selectEndHour(){
        val uiState = viewModel.uiState.value
        if(uiState.mode == DealViewModel.Mode.show)
            return;

        val endDateTime = uiState.editDeal?.deal?.dateTimeEnd ?: LocalDateTime.now()
        val endHour = endDateTime.hour

        showTimePickerDialog(endHour, 0){ hour, minute ->
            hour.let { if(minute > 0) it.inc() else it }
                .let { if(it == 24) 0 else it }
                .let { viewModel.setEditEndHour(it) }
        }
    }

    private fun showTimePickerDialog(hour: Int, minute: Int, resultAction: (hour: Int, minute: Int) -> Unit) {
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute2 ->
                resultAction(hourOfDay, minute2)
            },
            hour,
            minute,
            true,
        ).show()
    }

    private fun hideKeyboard() {
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        val view = requireActivity().currentFocus
        view?.let {
            imm?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}