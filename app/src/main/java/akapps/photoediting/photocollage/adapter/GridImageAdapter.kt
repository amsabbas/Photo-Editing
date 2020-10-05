package akapps.photoediting.photocollage.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

import innovate.photo2Edit.R

class GridImageAdapter(context: Context, private val frames: Array<Int>) : BaseAdapter() {

    private var inflater: LayoutInflater? = null

    init {
        inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return frames.size
    }

    override fun getItem(position: Int): Any {
        return frames[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class Holder {
        internal var img: ImageView? = null
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val holder: Holder
        if (convertView == null) {
            holder = Holder()
            convertView = inflater?.inflate(R.layout.item_photo_collage_grid, parent,false)
            holder.img = convertView?.findViewById(R.id.imageView) as ImageView
            convertView.tag = holder
        } else {
            holder = convertView.tag as Holder
        }
        holder.img?.setBackgroundResource(frames[position])
        return convertView
    }

}
