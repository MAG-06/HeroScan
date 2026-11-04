package com.example.heroscan.network.mapper

import com.example.heroscan.model.Comic
import com.example.heroscan.model.ComicResumen
import com.example.heroscan.model.FuenteComic
import com.example.heroscan.model.Personaje
import com.example.heroscan.model.TipoCodigo
import com.example.heroscan.network.dto.ComicListaMetron
import com.example.heroscan.network.dto.DetalleComicMetron

// Convierte el detalle de un cómic de Metron (DTO) en el modelo Comic de la app.
fun DetalleComicMetron.aComic(tipoCodigo: TipoCodigo, personajes: List<Personaje>, descripcionTraducida: String): Comic {

    return Comic(
        id = id.toString(),
        titulo = series.name,
        numero = number,
        editorial = publisher.name,
        fechaPublicacion = cover_date ?: "Desconocida",
        descripcion = descripcionTraducida,
        personajes = personajes,
        creadores = credits.map { it.creator },
        codigoBarras = upc ?: "",
        tipoCodigo = tipoCodigo,
        portadaUrl = image ?: "",
        fuente = FuenteComic.METRON
    )
}

// Convierte un elemento de la lista de resultados de Metron en un ComicResumen para mostrarlo en la lista.
fun ComicListaMetron.aComicResumen(): ComicResumen {
    return ComicResumen(
        id = id,
        titulo = series.name,
        numero = number,
        fechaPortada = cover_date ?: "",
        portadaUrl = image ?: ""
    )
}
