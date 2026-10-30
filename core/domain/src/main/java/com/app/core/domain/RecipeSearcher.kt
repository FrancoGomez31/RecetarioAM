package com.app.core.domain

/** Contrato del dominio: busca recetas por plato o ingrediente (la implementación usa IA). */
interface RecipeSearcher {
    /**
     * Devuelve una página de recetas relacionadas con [query] (de [PAGE_SIZE] en [PAGE_SIZE]).
     * [page] empieza en 0. Con menos de 3 letras, o fuera de [MAX_PAGES], devuelve una lista vacía.
     */
    suspend fun search(query: String, languageCode: String = "es", page: Int = 0): AppResult<List<Recipe>>

    /** Devuelve una receta encontrada antes en una búsqueda (para abrir su detalle), o null. */
    fun find(id: String): Recipe?

    companion object {
        /** Recetas que se piden por página. */
        const val PAGE_SIZE = 10

        /** Páginas máximas por búsqueda: PAGE_SIZE * MAX_PAGES = 50 recetas. */
        const val MAX_PAGES = 5
    }
}