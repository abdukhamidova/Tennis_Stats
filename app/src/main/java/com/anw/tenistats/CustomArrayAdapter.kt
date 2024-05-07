package com.anw.tenistats

import android.content.Context
import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class CustomArrayAdapter(
    private val context: Context,
    private val objects: List<String>,
    private val player1Name: String?,
    private val player2Name: String?
) : ListAdapter {

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        // Implementacja
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        // Implementacja
    }

    override fun getCount(): Int {
        return objects.size
    }

    override fun getItem(position: Int): Any {
        return objects[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: TextView(context)
        val currentItem = getItem(position) as String

        if (currentItem.contains(player1Name ?: "")) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.app_statsViewButton_Click))
        } else if (currentItem.contains(player2Name ?: "")) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.app_tableBg_color))
        } else {
            view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }

        // Ustawienie tekstu dla TextView
        (view as TextView).text = currentItem
        return view
    }


    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun isEmpty(): Boolean {
        return objects.isEmpty()
    }

    override fun areAllItemsEnabled(): Boolean {
        return true
    }

    override fun isEnabled(position: Int): Boolean {
        return true
    }
}
