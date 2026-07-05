package com.aistudio.hiromant.kxsrwa.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Gemini REST Models ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(
    val type: String = "application/json"
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null,
    val responseSchema: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

// --- Palmist Custom JSON Parse Models ---

@JsonClass(generateAdapter = true)
data class PalmLineAnalysis(
    val name: String, // e.g. "Линия жизни" / "Life Line"
    val color: String, // hex or color description
    val shortDescription: String,
    val fullDescription: String,
    val keyTakeaways: List<String>
)

@JsonClass(generateAdapter = true)
data class PalmMountAnalysis(
    val name: String, // Venus, Jupiter, etc.
    val active: Boolean,
    val description: String
)

@JsonClass(generateAdapter = true)
data class PalmSign(
    val name: String, // Cross, Star, Island
    val description: String,
    val location: String
)

@JsonClass(generateAdapter = true)
data class PalmistReport(
    val overallPortrait: String,
    val handType: String,
    val lines: List<PalmLineAnalysis>,
    val mounts: List<PalmMountAnalysis>,
    val signs: List<PalmSign>,
    val marriageChildren: String,
    val lifeEvents: String,
    val predictions: String,
    val recommendations: String,
    val leftHand: String,
    val rightHand: String
)

@JsonClass(generateAdapter = true)
data class CompatibilityReport(
    val compatibilityPercent: Int,
    val partner1Portrait: String,
    val partner2Portrait: String,
    val combinedAnalysis: String,
    val strongPoints: List<String>,
    val weakPoints: List<String>,
    val emotionalCompatibility: String,
    val intellectualCompatibility: String,
    val financialCompatibility: String,
    val recommendations: String
)
