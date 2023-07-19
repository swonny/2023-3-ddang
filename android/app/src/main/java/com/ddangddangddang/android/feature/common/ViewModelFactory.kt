package com.ddangddangddang.android.feature.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.ddangddangddang.android.feature.home.HomeViewModel
import com.ddangddangddang.android.feature.detail.AuctionDetailViewModel
import com.ddangddangddang.android.feature.main.MainViewModel
import com.ddangddangddang.android.feature.register.RegisterAuctionViewModel

@Suppress("UNCHECKED_CAST")
val viewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        with(modelClass) {
            // 레포지토리 싱글톤 객체 얻어옴
            when {
                isAssignableFrom(MainViewModel::class.java) -> MainViewModel()
                isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel()
                isAssignableFrom(AuctionDetailViewModel::class.java) -> AuctionDetailViewModel()
                isAssignableFrom(RegisterAuctionViewModel::class.java) -> RegisterAuctionViewModel()
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
