package com.lex.simplequest.presentation.screen.home.tracks

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
                trackTimeDistanceView.text =
                String.format("%s, %.2f", duration.toStringDuration(), track.distance())
                track.points.forEach {
                    Log.d("qaz", "point = $it")
                }
                trackInfoButton.setOnClickListener {
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