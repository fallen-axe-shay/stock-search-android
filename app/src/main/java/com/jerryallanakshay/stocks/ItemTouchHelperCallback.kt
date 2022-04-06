package com.jerryallanakshay.stocks

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


class ItemTouchHelperCallback(val adapter: WatchlistAdapter): ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END, ItemTouchHelper.LEFT) {

    var mAdapter: WatchlistAdapter = adapter
    var blackIcon = ContextCompat.getDrawable(mAdapter.getContext(), R.drawable.delete)
    var icon: Drawable? = null
    var background = ColorDrawable(mAdapter.getContext().getColor(R.color.red_tint))

    init {
        val bitmap = (blackIcon as BitmapDrawable).bitmap
        blackIcon = BitmapDrawable(mAdapter.getContext().resources, Bitmap.createScaledBitmap(bitmap, 30, 30, true))
        icon = DrawableCompat.wrap(blackIcon!!)
        DrawableCompat.setTint(icon!!, mAdapter.getContext().getColor(R.color.white));
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val from = viewHolder.adapterPosition
        var to = target.adapterPosition
        val itemBounds = mAdapter.getItemBounds(mAdapter.getData()?.get(from)?.type)
        if(to>itemBounds[1]!!) {
            to = itemBounds[1]!!
        } else if(to<itemBounds[0]!!) {
            to = itemBounds[0]!!
        }
        mAdapter.notifyItemMoved(from, to)
        return true
    }

    override fun isLongPressDragEnabled(): Boolean {
        // Allows for long click so items can be dragged, moved up or down in the list.
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        // Allows items to be swiped left or right.
        return true
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val  dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END
        val swipeFlags = ItemTouchHelper.LEFT
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        mAdapter.deleteItem(position)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(
            c, recyclerView, viewHolder, dX,
            dY, actionState, isCurrentlyActive
        )

        val itemView: View = viewHolder.itemView

        val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - icon!!.intrinsicHeight) / 2
        val iconBottom = iconTop + icon!!.intrinsicHeight

        if (dX < 0) { // Swiping to the left
            val iconLeft = itemView.right - iconMargin - icon!!.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top, itemView.right, itemView.bottom
            )
        } else { // view is unswiped
            background.setBounds(0, 0, 0, 0)
        }
        background.draw(c)
        icon!!.draw(c)
    }

}