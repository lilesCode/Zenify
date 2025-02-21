package com.sarajimenez.zenify.dialogues

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.sarajimenez.zenify.R
import com.sarajimenez.zenify.models.Task

class EditTaskDialog(context: Context, private val task: Task, private val onTaskUpdated: (Task) -> Unit) : Dialog(context) {

    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var dueTimePicker: TimePicker
    private lateinit var isCompletedCheckBox: CheckBox
    private lateinit var imageEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var colorButton: Button
    private lateinit var colorDialog: Dialog

    private var selectedColor: String = "pastelBlue" // Color predeterminado

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_edit_task)

        nameEditText = findViewById(R.id.edit_task_name)
        descriptionEditText = findViewById(R.id.edit_task_description)
        dueTimePicker = findViewById(R.id.due_time)
        isCompletedCheckBox = findViewById(R.id.isCompletedCheckbox)
        imageEditText = findViewById(R.id.edit_task_image_url)
        saveButton = findViewById(R.id.save_button)
        cancelButton = findViewById(R.id.cancel_button)
        colorButton = findViewById(R.id.select_color_button)

        // Configuración del ancho del Dialog
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)


        colorButton.setOnClickListener {
            showColorDialog()
        }

        nameEditText.setText(task.name)
        descriptionEditText.setText(task.description)

        // Convertimos la hora guardada como String a los valores del TimePicker
        val timeParts = task.dueTime.split(":")
        dueTimePicker.hour = timeParts[0].toInt()
        dueTimePicker.minute = timeParts[1].toInt()

        isCompletedCheckBox.isChecked = task.isCompleted
        imageEditText.setText(task.image)

        saveButton.setOnClickListener {
            val updatedName = nameEditText.text.toString()
            val updatedDescription = descriptionEditText.text.toString()
            val updatedDueTime = String.format("%02d:%02d", dueTimePicker.hour, dueTimePicker.minute)
            val updatedIsCompleted = isCompletedCheckBox.isChecked
            val updatedImageUrl = imageEditText.text.toString()

            if (updatedName.isBlank() || updatedDescription.isBlank()) {
                Toast.makeText(context, "Nombre y descripción requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedTask = task.copy(
                name = updatedName,
                description = updatedDescription,
                dueTime = updatedDueTime, // Guardamos la hora como String
                isCompleted = updatedIsCompleted,
                image = updatedImageUrl,
                color = selectedColor // Usamos el color seleccionado
            )

            onTaskUpdated(updatedTask)
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun showColorDialog() {
        // Crear un diálogo con los colores disponibles
        val colorOptions = listOf(
            R.color.pastelBlue,
            R.color.pastelPink,
            R.color.pastelPurple,
            R.color.pastelOrange,
            R.color.pastelYellow,
            R.color.pastelPeach,
            R.color.pastelLavender,
            R.color.pastelRed,
            R.color.pastelMint
        )

        val colorNames = arrayOf(
            "Pastel Blue", "Pastel Pink", "Pastel Purple", "Pastel Orange",
            "Pastel Yellow", "Pastel Peach", "Pastel Lavender",
            "Pastel Red", "Pastel Mint"
        )

        val gridView = GridView(context)
        gridView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        gridView.numColumns = 3  // Ajusta a 3 columnas, puede cambiar según lo que necesites
        gridView.horizontalSpacing = 10
        gridView.verticalSpacing = 10
        gridView.stretchMode = GridView.STRETCH_COLUMN_WIDTH

        val adapter = object : BaseAdapter() {
            override fun getCount(): Int = colorOptions.size
            override fun getItem(position: Int): Any = colorOptions[position]
            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val colorView = View(context)
                colorView.layoutParams = ViewGroup.LayoutParams(120, 120) // Aumentamos el tamaño del colorView
                colorView.setBackgroundColor(ContextCompat.getColor(context, colorOptions[position]))
                return colorView
            }
        }

        gridView.adapter = adapter

        gridView.setOnItemClickListener { _, _, position, _ ->
            selectedColor = colorNames[position].replace(" ", "") // Guardamos el nombre en lugar del HEX
            colorButton.text = colorNames[position] // Mostramos el nombre en el botón
            colorDialog.dismiss() // Cerrar el diálogo de colores
        }

        colorDialog = Dialog(context)
        colorDialog.setContentView(gridView)
        colorDialog.show()
    }
}
