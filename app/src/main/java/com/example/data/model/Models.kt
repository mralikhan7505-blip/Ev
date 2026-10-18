package com.example.data.model

enum class AiMode(val title: String, val subtitle: String, val modelName: String) {
    STANDARD(
        title = "Ev Fast",
        subtitle = "Quick conversation & ideas",
        modelName = "gemini-3.8-flash"
    ),
    HIGH_THINKING(
        title = "High Thinking",
        subtitle = "Deep reasoning & complex logic (gemini-4-argon)",
        modelName = "gemini-4-argon"
    ),
    GOOGLE_SEARCH(
        title = "Google Search",
        subtitle = "Live web facts & citations (gemini-3.8-flash)",
        modelName = "gemini-3.8-flash"
    )
}

data class SearchSource(
    val title: String,
    val url: String
)

data class AiResponseResult(
    val text: String,
    val thinkingProcess: String? = null,
    val searchSources: List<SearchSource> = emptyList(),
    val searchQueries: List<String> = emptyList()
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class FlashCardItem(
    val front: String,
    val back: String
)
