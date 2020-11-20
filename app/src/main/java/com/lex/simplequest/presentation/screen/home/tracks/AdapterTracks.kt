package com.lex.simplequest.presentation.screen.home.tracks

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lex.simplequest.Config
import com.lex.simplequest.R
import com.lex.simplequest.databinding.ItemTrackBinding
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.model.distance
import com.lex.simplequest.domain.model.duration
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
                val duration = track.duration()
                val format: String
                var distance = track.distance()
                if (distance >= Config.METERS_IN_KILOMETER) {
                    format = "%s, %.2f km"
                    distance /= Config.METERS_IN_KILOMETER
                } else {
                    format = "%s, %.2f m"
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
                infoView.setOnClickListener {
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