package com.lex.simplequest.presentation.screen.home.tracks.list

import android.content.Context
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
                    format = "%s, %.2f " + context.resources.getString(R.string.others_km) + if (BuildConfig.DEBUG && Config.SHOW_DEBUG_INFO) ", ${track.checkPoints.size} / ${track.points.size}" else ""
                    distance /= Config.METERS_IN_KILOMETER
                } else {
                    format = "%s, %.2f " + context.resources.getString(R.string.others_m) + if (BuildConfig.DEBUG && Config.SHOW_DEBUG_INFO) ", ${track.checkPoints.size} / ${track.points.size}" else ""
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