package com.sarajimenez.zenify.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.sarajimenez.zenify.databinding.ItemLayoutBinding
import com.sarajimenez.zenify.models.Task
import java.time.format.DateTimeFormatter

class ViewTask(view: View, deleteOnClick: (Int) -> Unit, updateOnClick: (Int) -> Unit) :
    RecyclerView.ViewHolder(view) {
    /*lateinit var binding: ItemLayoutBinding

    init {
        binding = ItemLayoutBinding.bind(view)
    }

    fun renderize(task: Task) {
        binding.txtviewName.setText(task.name)
        binding.txtviewDueTime.setText(task.dueTime.format(DateTimeFormatter.ofPattern("HH:mm")))
        binding.txtviewIsCompleted.text = if (task.isCompleted) "Completada" else "Pendiente"

    }
*/


    }