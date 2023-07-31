package com.ddangddangddang.android.feature.detail

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.viewModels
import androidx.viewpager2.widget.MarginPageTransformer
import com.ddangddangddang.android.R
import com.ddangddangddang.android.databinding.ActivityAuctionDetailBinding
import com.ddangddangddang.android.feature.common.viewModelFactory
import com.ddangddangddang.android.feature.detail.bid.AuctionBidDialog
import com.ddangddangddang.android.model.RegionModel
import com.ddangddangddang.android.util.binding.BindingActivity
import com.google.android.material.tabs.TabLayoutMediator

class AuctionDetailActivity :
    BindingActivity<ActivityAuctionDetailBinding>(R.layout.activity_auction_detail) {
    private val viewModel: AuctionDetailViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.viewModel = viewModel
        val auctionId = intent.getLongExtra(AUCTION_ID_KEY, -1L)
        setupViewModel()

        if (savedInstanceState == null) { viewModel.loadAuctionDetail(auctionId) }
    }

    private fun setupViewModel() {
        viewModel.event.observe(this) { event ->
            handleEvent(event)
        }
        viewModel.auctionDetailModel.observe(this) {
            setupAuctionImages(it.images)
            setupDirectRegions(it.directRegions)
        }
    }

    private fun handleEvent(event: AuctionDetailViewModel.AuctionDetailEvent) {
        when (event) {
            is AuctionDetailViewModel.AuctionDetailEvent.Exit -> finish()
            is AuctionDetailViewModel.AuctionDetailEvent.PopupAuctionBid -> showAuctionBidDialog()
        }
    }

    private fun showAuctionBidDialog() {
        AuctionBidDialog().show(supportFragmentManager, BID_DIALOG_TAG)
    }

    private fun setupAuctionImages(images: List<String>) {
        binding.vpImageList.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 1
            adapter = AuctionImageAdapter(images)
            setPageTransformer(MarginPageTransformer(convertDpToPx(20f)))
            setPadding(200, 0, 200, 0)
        }

        TabLayoutMediator(binding.tlIndicator, binding.vpImageList) { _, _ -> }.attach()
    }

    private fun convertDpToPx(dp: Float): Int {
        val density = Resources.getSystem().displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    private fun setupDirectRegions(regions: List<RegionModel>) {
        binding.rvDirectExchangeRegions.adapter = AuctionDirectRegionsAdapter(regions)
    }

    companion object {
        private const val AUCTION_ID_KEY = "auction_id_key"
        private const val BID_DIALOG_TAG = "bid_dialog_tag"

        fun getIntent(context: Context, auctionId: Long): Intent {
            return Intent(context, AuctionDetailActivity::class.java).apply {
                putExtra(AUCTION_ID_KEY, auctionId)
            }
        }
    }
}
