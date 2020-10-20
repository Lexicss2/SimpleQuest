package com.lex.simplequest.presentation.utils

import android.database.DataSetObserver
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class LoopingPagerAdapter(private val adapter: PagerAdapter) : PagerAdapter() {

    companion object {

        fun setupLoopingBehavior(viewPager: ViewPager, adapter: PagerAdapter): LoopingViewPagerHelper =
            LoopingPagerAdapterHelper(viewPager, adapter)

        private class LoopingPagerAdapterHelper(
            private val viewPager: ViewPager,
            adapter: PagerAdapter
        ) : LoopingViewPagerHelper {

            private var pagerAdapter: PagerAdapter? = null
            private val pageChangedListener = object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    // do nothing
                }

                override fun onPageSelected(position: Int) {
                    // do nothing
                }

                override fun onPageScrollStateChanged(state: Int) {
                    if (ViewPager.SCROLL_STATE_IDLE == state) {
                        validateCurrentItem(false)
                    }
                }
            }
            private val adapterDataSetObserver = object : DataSetObserver() {
                override fun onChanged() {
                    validateCurrentItem(true)
                }

                override fun onInvalidated() {
                    validateCurrentItem(true)
                }
            }

            init {
                viewPager.adapter = LoopingPagerAdapter(adapter)
                viewPager.addOnPageChangeListener(pageChangedListener)
                adapterChanged()
            }

            override fun adapterChanged() {
                pagerAdapter?.unregisterDataSetObserver(adapterDataSetObserver)
                pagerAdapter = viewPager.adapter
                pagerAdapter?.registerDataSetObserver(adapterDataSetObserver)
                validateCurrentItem(true)
            }

            override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
                val adapter = viewPager.adapter
                var pageIndex = item
                if (adapter is LoopingPagerAdapter) {
                    pageIndex = when {
                        0 == adapter.count -> item
                        0 == item -> adapter.count - 2
                        adapter.count - 1 == item -> 1
                        else -> item
                    }
                }
                viewPager.setCurrentItem(pageIndex, smoothScroll)
            }

            private fun validateCurrentItem(checkInitialBounds: Boolean) {
                viewPager.adapter?.let { adapter ->
                    if (adapter is LoopingPagerAdapter) {
                        val currentItem = viewPager.currentItem
                        val requiredItem = when {
                            0 == adapter.count -> currentItem
                            checkInitialBounds && (0 == currentItem) -> 1
                            checkInitialBounds && (adapter.count - 1 == currentItem) -> adapter.count - 2
                            0 == currentItem -> adapter.count - 2
                            adapter.count - 1 == currentItem -> 1
                            else -> currentItem
                        }
                        if (requiredItem != currentItem) {
                            viewPager.setCurrentItem(requiredItem, false)
                        }
                    }
                }
            }
        }
    }

    private val forwardingDataSetObserver = object : DataSetObserver() {
        override fun onChanged() {
            notifyDataSetChanged()
        }

        override fun onInvalidated() {
            notifyDataSetChanged()
        }
    }

    init {
        this.adapter.registerDataSetObserver(forwardingDataSetObserver)
    }

    fun getRealCount(): Int =
        adapter.count

    override fun getCount(): Int =
        getRealCount().let { realCount ->
            if (0 != realCount) realCount + 2
            else 0
        }

    override fun getItemPosition(`object`: Any): Int =
        adapter.getItemPosition(`object`).let { itemPosition ->
            if (POSITION_NONE != itemPosition && POSITION_UNCHANGED != itemPosition) {
                itemPosition + 1
            } else itemPosition
        }

    override fun instantiateItem(container: ViewGroup, position: Int): Any =
        adapter.instantiateItem(container, virtualPositionToRealPosition(position))

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        adapter.destroyItem(container, virtualPositionToRealPosition(position), `object`)
    }

    override fun finishUpdate(container: ViewGroup) {
        adapter.finishUpdate(container)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean =
        adapter.isViewFromObject(view, `object`)

    override fun restoreState(bundle: Parcelable?, classLoader: ClassLoader?) {
        adapter.restoreState(bundle, classLoader)
    }

    override fun saveState(): Parcelable? =
        adapter.saveState()

    override fun startUpdate(container: ViewGroup) {
        adapter.startUpdate(container)
    }

    override fun getPageTitle(position: Int): CharSequence? =
        adapter.getPageTitle(virtualPositionToRealPosition(position))

    override fun getPageWidth(position: Int): Float =
        adapter.getPageWidth(position)

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        adapter.setPrimaryItem(container, position, `object`)
    }

    private fun virtualPositionToRealPosition(virtualPosition: Int): Int =
        when (virtualPosition) {
            0 -> getRealCount() - 1
            count - 1 -> 0
            else -> virtualPosition - 1
        }

    interface LoopingViewPagerHelper {
        fun adapterChanged()
        fun setCurrentItem(item: Int, smoothScroll: Boolean)
    }
}