package com.sarajimenez.zenify.fragments.tasks

import AdapterTask
import Controller
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sarajimenez.zenify.R
import com.sarajimenez.zenify.dialogues.AddTaskDialog
import com.sarajimenez.zenify.dialogues.EditTaskDialog
import com.sarajimenez.zenify.models.Task

class FragmentTasks : Fragment() {
    private val taskController: Controller by viewModels()
    private lateinit var adapterTask: AdapterTask
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddTask: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.my_recycler_view)
        btnAddTask = view.findViewById(R.id.add_task)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapterTask = AdapterTask(
            onEditClick = { taskId ->
                val task = adapterTask.currentList.find { it.id == taskId }
                task?.let {
                    val editDialog = EditTaskDialog(requireContext(), it) { updatedTask ->
                        taskController.updateTask(updatedTask)
                        Toast.makeText(requireContext(), "Tarea actualizada", Toast.LENGTH_SHORT).show()
                    }
                    editDialog.show()
                }
            },
            onDeleteClick = { taskId ->
                taskController.deleteTask(taskId)
                Toast.makeText(requireContext(), "Tarea eliminada", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.adapter = adapterTask

        taskController.tasks.observe(viewLifecycleOwner) { taskList ->
            taskList?.let {
                adapterTask.submitList(it)
            }
        }

        btnAddTask.setOnClickListener {
            val addDialog = AddTaskDialog(requireContext()) { newTask ->
                taskController.addTask(newTask)
                Toast.makeText(requireContext(), "Tarea añadida", Toast.LENGTH_SHORT).show()
            }
            addDialog.show()
        }
    }
}
