package com.example.simbirsofttest.feature.deal.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simbirsofttest.data.models.Deal
import com.example.simbirsofttest.feature.deal.constants.BundleConstant
import com.example.simbirsofttest.feature.deal.extensions.readLocalDate
import com.example.simbirsofttest.feature.deal.extensions.toEpochMillis
import com.example.simbirsofttest.feature.deal.databinding.PageDealCalendarBinding
import com.example.simbirsofttest.feature.deal.viewModels.DealCalendarViewModel
import com.example.simbirsofttest.feature.deal.recyclerview.adapters.DealListAdapter
import com.example.simbirsofttest.feature.deal.menuProviders.DealCalendarMenuProvider
import com.example.simbirsofttest.feature.deal.navigation.DealRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
internal class DealCalendarFragment : Fragment() {
    private val viewModel: DealCalendarViewModel by viewModels()

    private var adapter: DealListAdapter? = null

    private var _binding: PageDealCalendarBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PageDealCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenuProvider()
        setupView()
        launchRepeatOnLifeCycle()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenuProvider(){
        requireActivity().addMenuProvider(DealCalendarMenuProvider(this::navigateToCreateDeal), viewLifecycleOwner)
    }

    private fun setupView(){
        binding.calendarView.setOnDateChangeListener{ _, year, month, dayOfMonth ->
            viewModel.setDay(LocalDate.of(year,month + 1, dayOfMonth))
        }
        adapter = DealListAdapter(
            this::navigateToShowDealDetail,
            this::navigateToCreateDeal,
        )
        binding.dealsRecycler.layoutManager = LinearLayoutManager(context)
        binding.dealsRecycler.adapter = adapter
    }

    private fun launchRepeatOnLifeCycle(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.uiState.collect {
                    adapter!!.deals = it.deals
                    binding.calendarView.date = it.date.toEpochMillis()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    findNavController()
                        .currentBackStackEntry
                        ?.savedStateHandle
                        ?.let { savedStateHandle ->
                            savedStateHandle.get<Bundle?>(BundleConstant.CREATED_DEAL_DATE)
                                ?.let{ createdDealDate ->
                                    viewModel.setDay(createdDealDate.readLocalDate())
                                    savedStateHandle[BundleConstant.CREATED_DEAL_DATE] = null
                                }
                }
            }
        }
    }

    private fun navigateToCreateDeal(){
        findNavController().navigate(DealRoute.EmptyDealCreation)
    }

    private fun navigateToCreateDeal(hour: Int){
        val date = viewModel.uiState.value.date
        findNavController().navigate(DealRoute.creation(
            date.year,
            date.monthValue,
            date.dayOfMonth,
            hour,
        ))
    }

    private fun navigateToShowDealDetail(deal: Deal){
        findNavController().navigate(DealRoute.detail(deal.id!!))
    }
}