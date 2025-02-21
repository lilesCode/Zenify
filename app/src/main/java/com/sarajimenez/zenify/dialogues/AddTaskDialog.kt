package com.sarajimenez.zenify.dialogues

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

class AddTaskDialog(context: Context, private val onTaskAdded: (Task) -> Unit) : Dialog(context) {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_task)

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

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val isCompleted = isCompletedCheckBox.isChecked
            val imageUrl = imageEditText.text.toString()

            // Convertimos la hora del TimePicker a String en formato "HH:mm"
            val dueTimeString = String.format("%02d:%02d", dueTimePicker.hour, dueTimePicker.minute)

            if (name.isBlank() || description.isBlank()) {
                Toast.makeText(context, "Nombre y descripción requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newTask = Task(
                id = FirebaseFirestore.getInstance().collection("tasks").document().id,
                name = name,
                description = description,
                dueTime = dueTimeString, // Guardamos la hora como String
                isCompleted = isCompleted,
                image = imageUrl,
                color = selectedColor // Usamos el color seleccionado
            )

            onTaskAdded(newTask)
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
        gridView.numColumns = 3
        gridView.horizontalSpacing = 10
        gridView.verticalSpacing = 10

        val adapter = object : BaseAdapter() {
            override fun getCount(): Int = colorOptions.size
            override fun getItem(position: Int): Any = colorOptions[position]
            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val colorView = View(context)
                colorView.layoutParams = ViewGroup.LayoutParams(60, 60)
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
