package com.example.heroscan.model

object MockComics {
    val comics = listOf(
        Comic(
            id = "1",
            title = "The Amazing Spider-Man",
            issueNumber = "#300",
            publisher = "Marvel Comics",
            releaseDate = "Mayo 1988",
            description = "Una entrega histórica que marca la primera aparición completa de Venom. Peter Parker se enfrenta a su pesadilla más temible cuando el simbionte alienígena regresa fusionado con Eddie Brock, desatando una batalla campal que cambiará la vida del lanzarredes para siempre.",
            characters = listOf("Spider-Man", "Venom", "Mary Jane", "Eddie Brock", "Black Cat"),
            creators = listOf(
                Creator(role = "Guion", name = "David Michelinie"),
                Creator(role = "Dibujo", name = "Todd McFarlane"),
                Creator(role = "Arte / Color", name = "Bob Sharen")
            ),
            barcode = "9781234567890",
            scanType = "EAN-13"
        ),
        Comic(
            id = "2",
            title = "Batman: Year One",
            issueNumber = "#1",
            publisher = "DC Comics",
            releaseDate = "Febrero 1987",
            description = "Los inicios de Bruce Wayne como Batman, narrando su primer año luchando contra el crimen en una Gotham corrupta, con la ayuda del comisionado James Gordon.",
            characters = listOf("Batman", "James Gordon", "Selina Kyle"),
            creators = listOf(
                Creator(role = "Guion", name = "Frank Miller"),
                Creator(role = "Dibujo", name = "David Mazzucchelli")
            ),
            barcode = "9780123456786",
            scanType = "EAN-13"
        )
    )

    fun getById(id: String): Comic? = comics.find { it.id == id }
}
