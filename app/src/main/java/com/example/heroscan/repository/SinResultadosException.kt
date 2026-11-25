package com.example.heroscan.repository

// Error cuando Metron responde bien pero sin resultados. Es distinto de un 429, un 500 o la falta de conexión:
// con este se puede probar otra búsqueda sin gastar peticiones de más.
class SinResultadosException(mensaje: String) : Exception(mensaje)
