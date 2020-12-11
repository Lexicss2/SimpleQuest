package com.lex.simplequest.presentation.screen.home.tracks

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lex.simplequest.BuildConfig
import com.lex.simplequest.Config
import com.lex.simplequest.R
import com.lex.simplequest.databinding.ItemTrackBinding
import com.lex.simplequest.domain.model.*
import com.lex.simplequest.presentation.utils.toStringDuration

class AdapterTracks(private val context: Context, private val clickListener: ItemClickListener) :
    RecyclerView.Adapter<AdapterTracks.TracksViewHolder>() {

    private var items: List<Track> = listOf()

    fun set(items: List<Track>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TracksViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding: ItemTrackBinding = ItemTrackBinding.inflate(inflater)
        return TracksViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: TracksViewHolder, position: Int) {
        val track = items[position]
        holder.bind(track)
    }

    override fun getItemCount(): Int =
        items.size

    inner class TracksViewHolder(itemBinding: ItemTrackBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        private var viewBinding: ItemTrackBinding = itemBinding

        fun bind(track: Track) {
            viewBinding.apply {
                root.setOnClickListener {
                    clickListener.onTrackClicked(track)
                }
                trackNameView.text = track.name
                val duration = track.movingDuration()
                val format: String
                var distance = track.movingDistance()
                if (distance >= Config.METERS_IN_KILOMETER) {
                    // TODO: Restore 2nd dist
                    format = "%s, %.2f km" + if (BuildConfig.DEBUG) ", ${track.checkPoints.size} / ${track.points.size}" else ""
                    distance /= Config.METERS_IN_KILOMETER
                } else {
                    format = "%s, %.2f m" + if (BuildConfig.DEBUG) ", ${track.checkPoints.size} / ${track.points.size}" else ""
                }

                trackTimeDistanceView.text =
                    String.format(format, duration.toStringDuration(), distance)
                if (null == track.endTime) {
                    trackNameView.setTextAppearance(R.style.AppTheme_Text_Small_Red)
                    trackTimeDistanceView.setTextAppearance(R.style.AppTheme_Text_ExtraSmall_Red)
                } else {
                    trackNameView.setTextAppearance(R.style.AppTheme_Text_Small)
                    trackTimeDistanceView.setTextAppearance(R.style.AppTheme_Text_ExtraSmall)
                }
                infoButton.setOnClickListener {
                    clickListener.onInfoClicked(track)
                }
            }
        }
    }

    interface ItemClickListener {
        fun onTrackClicked(track: Track)
        fun onInfoClicked(track: Track)
    }
}