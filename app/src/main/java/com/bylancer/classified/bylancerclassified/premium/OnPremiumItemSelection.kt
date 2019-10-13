package com.bylancer.classified.bylancerclassified.premium

interface OnPremiumItemSelection {
    fun onItemSelection(isSelected : Boolean, cost : Int, isFor : String)
}
