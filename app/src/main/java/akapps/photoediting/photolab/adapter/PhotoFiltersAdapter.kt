package akapps.photoediting.photolab.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zomato.photofilters.utils.ThumbnailItem
import innovate.photo2Edit.R
import kotlinx.android.synthetic.main.item_photo_lab.view.*

class PhotoFiltersAdapter(var items: List<ThumbnailItem>, val callback: Callback) : RecyclerView.Adapter<PhotoFiltersAdapter.MainHolder>() {

    private var clickedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MainHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_photo_lab, parent, false))

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class MainHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: ThumbnailItem) {
            itemView.imgBorder.visibility = View.GONE
            itemView.imageView.setImageBitmap(item.image)
            itemView.setOnClickListener {
                if (clickedPosition != adapterPosition) {
                    callback.onItemClicked(item)
                    itemView.imgBorder.visibility = View.VISIBLE
                    notifyItemChanged(clickedPosition)
                }
                clickedPosition = adapterPosition

            }
        }
    }

    interface Callback {
        fun onItemClicked(item: ThumbnailItem)
    }

}