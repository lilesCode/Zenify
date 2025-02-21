import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sarajimenez.zenify.R
import com.sarajimenez.zenify.databinding.ItemLayoutBinding
import com.sarajimenez.zenify.models.Task
import java.time.format.DateTimeFormatter

class AdapterTask(
    private val onDeleteClick: (String) -> Unit,
    private val onEditClick: (String) -> Unit
) : ListAdapter<Task, AdapterTask.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)

        holder.binding.btnEdit.setOnClickListener {
            onEditClick(task.id)
        }

        holder.binding.btnDelete.setOnClickListener {
            onDeleteClick(task.id)
        }
    }

    inner class TaskViewHolder(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun bind(task: Task) {
            binding.txtviewName.text = task.name
            binding.txtviewDueTime.text = task.dueTime.format(timeFormatter)
            binding.txtviewIsCompleted.text = if (task.isCompleted) "Completada" else "Pendiente"

            // Cargar la imagen de la tarjeta
            Glide.with(binding.root.context)
                .load(task.image)
                .placeholder(com.sarajimenez.zenify.R.drawable.loading)
                .error(com.sarajimenez.zenify.R.drawable.error_image)
                .into(binding.ivTask)

            val taskColor = when (task.color) {
                "PastelPink" -> ContextCompat.getColor(binding.root.context, R.color.pastelPink)
                "PastelOrange" -> ContextCompat.getColor(binding.root.context, R.color.pastelOrange)
                "PastelPurple" -> ContextCompat.getColor(binding.root.context, R.color.pastelPurple)
                "PastelBlue" -> ContextCompat.getColor(binding.root.context, R.color.pastelBlue)
                "PastelMint" -> ContextCompat.getColor(binding.root.context, R.color.pastelMint)
                "PastelLavender" -> ContextCompat.getColor(binding.root.context, R.color.pastelLavender)
                "PastelYellow" -> ContextCompat.getColor(binding.root.context, R.color.pastelYellow)
                "PastelRed" -> ContextCompat.getColor(binding.root.context, R.color.pastelRed)
                "PastelPeach" -> ContextCompat.getColor(binding.root.context, R.color.pastelPeach)
                else -> ContextCompat.getColor(binding.root.context, R.color.pastelGreen2)
            }
            binding.cardViewTask.setCardBackgroundColor(taskColor)
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
