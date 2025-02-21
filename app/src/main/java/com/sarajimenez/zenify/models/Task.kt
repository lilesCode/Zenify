package com.sarajimenez.zenify.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.time.LocalTime
import java.time.format.DateTimeFormatter


data class Task(
    @DocumentId var id: String = "",
    var name: String = "",
    var description: String = "",
    var dueTime: String = "",  // Ya es un String, no hace falta convertir
    var isCompleted: Boolean = false,
    var image: String = "",
    var color: String = ""
) {
    // Constructor vacío
    constructor() : this(
        id = "",
        name = "",
        description = "",
        dueTime = "",  // Valor por defecto
        isCompleted = false,
        image = "",
        color = ""
    )

    // Aquí ya no necesitas conversiones a LocalTime, simplemente maneja 'dueTime' como String
}


