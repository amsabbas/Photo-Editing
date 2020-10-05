package akapps.photoediting.photolab.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import innovate.photo2Edit.R

class PhotoFramesAdapter(var items: IntArray, val callback: Callback) : RecyclerView.Adapter<PhotoFramesAdapter.MainHolder>() {

    private var clickedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MainHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_photo_lab, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class MainHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        private val imageBorder = itemView.findViewById<ImageView>(R.id.imgBorder)

        fun bind(item: Int) {
            imageBorder.visibility = View.GONE
            imageView.setBackgroundResource(item)
            itemView.setOnClickListener {
                if (clickedPosition != adapterPosition) {
                    callback.onItemClicked(adapterPosition)
                    imageBorder.visibility = View.VISIBLE
                    notifyItemChanged(clickedPosition)
                }
                clickedPosition = adapterPosition
            }
        }
    }

    interface Callback {
        fun onItemClicked(position: Int)
    }

}