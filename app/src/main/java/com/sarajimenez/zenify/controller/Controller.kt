import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sarajimenez.zenify.models.Task
import java.text.SimpleDateFormat
import java.util.Locale

class Controller : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> get() = _tasks

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        db.collection("tasks")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("TaskController", "Error al obtener tareas: $exception")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val taskList = snapshot.toObjects(Task::class.java)

                    // 🔥 Ordenar la lista por dueTime
                    val sortedList = taskList.sortedBy { task ->
                        try {
                            timeFormat.parse(task.dueTime) // Convierte el string a Date para ordenarlo
                        } catch (e: Exception) {
                            Log.e("TaskController", "Error al parsear dueTime: ${task.dueTime}", e)
                            null
                        }
                    }

                    Log.d("TaskController", "Tareas obtenidas y ordenadas: $sortedList")
                    _tasks.value = sortedList
                } else {
                    Log.d("TaskController", "No se encontraron tareas.")
                }
            }
    }

    fun addTask(task: Task) {
        // 'dueTime' es ya un String, no necesitas convertirlo
        db.collection("tasks")
            .document(task.id)
            .set(task)
            .addOnSuccessListener { Log.d("TaskController", "Tarea añadida correctamente") }
            .addOnFailureListener { e -> Log.e("TaskController", "Error al añadir tarea", e) }

    }

    fun deleteTask(taskId: String) {
        db.collection("tasks")
            .document(taskId)
            .delete()
            .addOnSuccessListener {
                Log.d("TaskController", "Tarea eliminada correctamente")
                _tasks.value = _tasks.value?.filter { it.id != taskId }
            }
            .addOnFailureListener { e -> Log.e("TaskController", "Error al eliminar tarea", e) }
    }

    fun updateTask(task: Task) {
        if (task.id.isBlank()) {
            Log.e("TaskController", "El ID de la tarea no es válido")
            return
        }
        db.collection("tasks")
            .document(task.id)
            .set(task)
            .addOnSuccessListener { Log.d("TaskController", "Tarea actualizada correctamente") }
            .addOnFailureListener { e -> Log.e("TaskController", "Error al actualizar tarea", e) }
    }
}
