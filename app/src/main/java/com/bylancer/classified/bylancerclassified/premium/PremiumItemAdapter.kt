package com.bylancer.classified.bylancerclassified.premium

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bylancer.classified.bylancerclassified.R
import kotlinx.android.synthetic.main.premium_item_layout.view.*

class PremiumItemAdapter(private val items : List<PremiumObjectDetails>, private val mPremiumItemSelection: OnPremiumItemSelection)
        : RecyclerView.Adapter<PremiumItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.premium_item_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: PremiumObjectDetails = items[position]
        holder?.categoryName?.text = item?.name
        holder?.categoryDescription?.text = item?.description
        if (item?.isSelected) {
            holder?.mSelectionIcon?.setImageResource(R.drawable.ic_selected)
        } else {
            holder?.mSelectionIcon?.setImageResource(R.drawable.ic_add_to_select)
        }

        if (item?.canCancelSelection) {
            holder?.mSelectionIcon.setOnClickListener {
                if (item?.isSelected) {
                    holder?.mSelectionIcon?.setImageResource(R.drawable.ic_add_to_select)
                } else {
                    holder?.mSelectionIcon?.setImageResource(R.drawable.ic_selected)
                }
                item?.isSelected = !item?.isSelected
                mPremiumItemSelection?.onItemSelection(item?.isSelected, item?.cost, item?.name)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val mSelectionIcon = view.premium_item_icon
        val categoryName = view.premium_item_title
        val categoryDescription = view.premium_item_description
    }
}