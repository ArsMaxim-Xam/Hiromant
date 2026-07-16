package com.aistudio.hiromant.kxsrwa.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object BitmapUtils {
    fun Bitmap.toBase64(quality: Int = 70): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateMysticHandBitmap(context: Context, slot: String, isRussian: Boolean): Bitmap {
        val width = 1024
        val height = 1024
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Background color (Mystic dark blue/purple space background)
        val bgPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
        }
        val gradient = android.graphics.LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            android.graphics.Color.parseColor("#0F0C1B"),
            android.graphics.Color.parseColor("#1C1635"),
            android.graphics.Shader.TileMode.CLAMP
        )
        bgPaint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        
        // Draw starry space aura (glow effect)
        val auraPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.parseColor("#44D4AF37") // Mystic Gold with alpha
            maskFilter = android.graphics.BlurMaskFilter(150f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(width / 2f, height / 2f, 250f, auraPaint)
        
        // Draw golden mystical circle frame
        val framePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f
            color = android.graphics.Color.parseColor("#D4AF37") // Gold
        }
        canvas.drawCircle(width / 2f, height / 2f, 480f, framePaint)
        
        framePaint.strokeWidth = 1f
        canvas.drawCircle(width / 2f, height / 2f, 465f, framePaint)
        
        // Draw small stars
        val starPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.parseColor("#FFFFFF")
        }
        val random = java.util.Random(slot.hashCode().toLong())
        for (i in 0..40) {
            val sx = random.nextFloat() * width
            val sy = random.nextFloat() * height
            val r = 1f + random.nextFloat() * 3f
            starPaint.alpha = 100 + random.nextInt(155)
            canvas.drawCircle(sx, sy, r, starPaint)
        }
        
        // Draw the golden hand outline
        val handPath = android.graphics.Path()
        val cx = width / 2f
        val cy = height / 2f + 50f
        
        // Simple elegant path representing a palm
        handPath.moveTo(cx - 150f, cy + 200f) // wrist left
        handPath.lineTo(cx - 180f, cy + 50f)  // thumb base
        handPath.lineTo(cx - 260f, cy + 10f)  // thumb tip
        handPath.lineTo(cx - 210f, cy - 30f)  // thumb inner joint
        
        // Index finger
        handPath.lineTo(cx - 160f, cy - 80f)
        handPath.lineTo(cx - 150f, cy - 320f) // Index tip
        handPath.lineTo(cx - 80f, cy - 320f)
        handPath.lineTo(cx - 70f, cy - 80f)
        
        // Middle finger
        handPath.lineTo(cx - 60f, cy - 100f)
        handPath.lineTo(cx - 50f, cy - 360f) // Middle tip
        handPath.lineTo(cx + 20f, cy - 360f)
        handPath.lineTo(cx + 30f, cy - 100f)
        
        // Ring finger
        handPath.lineTo(cx + 40f, cy - 90f)
        handPath.lineTo(cx + 50f, cy - 330f) // Ring tip
        handPath.lineTo(cx + 120f, cy - 330f)
        handPath.lineTo(cx + 130f, cy - 90f)
        
        // Pinky finger
        handPath.lineTo(cx + 140f, cy - 60f)
        handPath.lineTo(cx + 150f, cy - 260f) // Pinky tip
        handPath.lineTo(cx + 210f, cy - 260f)
        handPath.lineTo(cx + 200f, cy - 20f)
        
        // Outer palm
        handPath.lineTo(cx + 220f, cy + 150f)
        handPath.lineTo(cx + 150f, cy + 200f) // wrist right
        handPath.close()
        
        val handPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 6f
            color = android.graphics.Color.parseColor("#D4AF37")
            strokeJoin = android.graphics.Paint.Join.ROUND
            strokeCap = android.graphics.Paint.Cap.ROUND
        }
        canvas.drawPath(handPath, handPaint)
        
        // Fill the hand with subtle translucent gold
        val fillPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.parseColor("#15D4AF37")
        }
        canvas.drawPath(handPath, fillPaint)
        
        // Draw hand lines (Life, Heart, Head, Destiny) inside the palm
        val linesPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 4f
            color = android.graphics.Color.parseColor("#E5C158")
            strokeCap = android.graphics.Paint.Cap.ROUND
        }
        
        // Heart Line (top curve from under pinky towards index)
        val heartPath = android.graphics.Path().apply {
            moveTo(cx + 160f, cy + 20f)
            quadTo(cx, cy - 20f, cx - 110f, cy - 50f)
        }
        canvas.drawPath(heartPath, linesPaint)
        
        // Head Line (middle line across palm starting with Life Line)
        val headPath = android.graphics.Path().apply {
            moveTo(cx - 130f, cy + 40f)
            quadTo(cx, cy + 60f, cx + 150f, cy + 100f)
        }
        canvas.drawPath(headPath, linesPaint)
        
        // Life Line (curve wrapping around thumb base)
        val lifePath = android.graphics.Path().apply {
            moveTo(cx - 130f, cy + 40f)
            quadTo(cx - 50f, cy + 100f, cx - 100f, cy + 190f)
        }
        canvas.drawPath(lifePath, linesPaint)
        
        // Destiny Line (vertical line up the center)
        val destinyPath = android.graphics.Path().apply {
            moveTo(cx + 10f, cy + 190f)
            lineTo(cx - 20f, cy - 10f)
        }
        linesPaint.color = android.graphics.Color.parseColor("#C5A028")
        canvas.drawPath(destinyPath, linesPaint)
        
        // Draw Planetary Mount Symbols (as beautifully rendered text or ancient icons)
        val textPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.parseColor("#E5C158")
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD_ITALIC)
        }
        
        // Mount labels
        canvas.drawText(" Jupiter ", cx - 110f, cy - 100f, textPaint)
        canvas.drawText(" Saturn ", cx - 35f, cy - 120f, textPaint)
        canvas.drawText(" Sun ", cx + 55f, cy - 110f, textPaint)
        canvas.drawText(" Mercury ", cx + 140f, cy - 80f, textPaint)
        canvas.drawText(" Venus ", cx - 120f, cy + 120f, textPaint)
        canvas.drawText(" Moon ", cx + 140f, cy + 130f, textPaint)
        
        // Title of the Slot
        val titleText = when (slot) {
            "left_palm" -> if (isRussian) "ЛЕВАЯ ЛАДОНЬ (ПАССИВНАЯ)" else "LEFT PALM (INHERITED)"
            "left_back" -> if (isRussian) "ЛЕВАЯ ТЫЛЬНАЯ СТОРОНА" else "LEFT BACK (PROTECTION)"
            "right_palm" -> if (isRussian) "ПРАВАЯ ЛАДОНЬ (АКТИВНАЯ)" else "RIGHT PALM (REALIZED)"
            "right_back" -> if (isRussian) "ПРАВАЯ ТЫЛЬНАЯ СТОРОНА" else "RIGHT BACK (EXPRESSION)"
            else -> if (isRussian) "СВЯЩЕННАЯ ЛАДОНЬ" else "SACRED PALM"
        }
        
        val titlePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.parseColor("#FFFFFF")
            textSize = 36f
            textAlign = android.graphics.Paint.Align.CENTER
            letterSpacing = 0.1f
            typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD)
        }
        canvas.drawText(titleText, cx, 150f, titlePaint)
        
        // Add a small mystical quote at the bottom
        val quoteText = if (isRussian) {
            "«Что начертано звёздами — отражено на твоей руке»"
        } else {
            "\"What is written in the stars is reflected on your hand\""
        }
        val quotePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.parseColor("#8E80B5")
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.ITALIC)
        }
        canvas.drawText(quoteText, cx, 920f, quotePaint)
        
        return bitmap
    }

    /**
     * Сохраняет изображение (Bitmap) в галерею устройства с использованием MediaStore API.
     * Работает на Android 10+ (API 29+) и более ранних версиях.
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Boolean {
        val resolver = context.contentResolver
        val imageCollection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            android.provider.MediaStore.Images.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "$title-${System.currentTimeMillis()}.jpg")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/Palmist")
            }
        }

        val imageUri = resolver.insert(imageCollection, contentValues) ?: return false

        return try {
            resolver.openOutputStream(imageUri)?.use { outStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                resolver.delete(imageUri, null, null)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            false
        }
    }
}
