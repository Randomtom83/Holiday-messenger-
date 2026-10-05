package com.holidaymessenger.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holidaymessenger.data.review.HolidayReviewRepository
import com.holidaymessenger.data.review.ReviewItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewUiState(
    val items: List<ReviewItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: HolidayReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = ReviewUiState(items = reviewRepository.queue(), isLoading = false)
        }
    }

    fun approve(item: ReviewItem) {
        viewModelScope.launch {
            reviewRepository.approve(item)
            refresh()
        }
    }

    fun skip(item: ReviewItem) {
        reviewRepository.skip(item)
        refresh()
    }

    fun approveAll(holidayId: Long) {
        viewModelScope.launch {
            reviewRepository.approveAll(_uiState.value.items.filter { it.holidayId == holidayId })
            refresh()
        }
    }

    fun skipAll(holidayId: Long) {
        reviewRepository.skipAll(_uiState.value.items.filter { it.holidayId == holidayId })
        refresh()
    }
}
