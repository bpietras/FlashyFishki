package com.an.intelligence.flashyfishki.navigation

import kotlinx.serialization.Serializable

@Serializable
object AuthRoute

@Serializable 
object HomeRoute

@Serializable
object ProfileRoute

@Serializable
object CategoriesRoute

@Serializable
data class FlashcardsRoute(val categoryId: Long)

@Serializable
data class FlashcardDetailsRoute(val flashcardId: Long)

@Serializable
data class FlashcardEditRoute(
    val flashcardId: Long? = null,
    val categoryId: Long? = null
)

@Serializable
data class ExportRoute(val categoryId: Long)

@Serializable
data class LearningRoute(val categoryId: Long)

@Serializable
object StatisticsRoute
