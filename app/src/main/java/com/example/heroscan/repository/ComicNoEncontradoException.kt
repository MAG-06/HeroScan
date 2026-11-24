package com.example.heroscan.repository

// Error de obtenerDetalleComic cuando Metron responde 404: el cómic con ese id no existe (por ejemplo, un enlace mal escrito).
class ComicNoEncontradoException : Exception("No se encontró el cómic en Metron")
