package com.app.relatosfutbolerosnd_20.ui.stream

import android.graphics.*
import androidx.compose.ui.graphics.toArgb
import com.app.relatosfutbolerosnd_20.ui.screens.AdAlert
import com.app.relatosfutbolerosnd_20.ui.screens.CautionAlert

object ScoreboardBitmapGenerator {

    fun generate(
        homeTeamName: String,
        awayTeamName: String,
        homeScore: Int,
        awayScore: Int,
        homeColor: Int,
        homeColor2: Int,
        awayColor: Int,
        awayColor2: Int,
        elapsedTime: String,
        isTimerRunning: Boolean,
        radioLogo: Bitmap? = null,
        cautionAlert: CautionAlert = CautionAlert(),
        adAlert: AdAlert = AdAlert(),
        homeRedCards: Int = 0,
        awayRedCards: Int = 0,
        extraTime: Int = 0,
        streamWidth: Int = 1280
    ): Bitmap {
        val streamHeight = streamWidth * 9 / 16
        val scale = streamWidth / 1280f

        val bitmap = Bitmap.createBitmap(streamWidth, streamHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true; isDither = true }
        val generalPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        val marginX = 35f * scale
        val marginY = 25f * scale

        // --- 1. LOGO RADIO (MANTENIDO IGUAL QUE ESTÁ PERFECTO) ---
        if (radioLogo != null) {
            val maxLogoSize = 95f * scale
            val srcW = radioLogo.width.toFloat(); val srcH = radioLogo.height.toFloat()
            val ratio = srcW / srcH
            val drawW: Float; val drawH: Float
            if (ratio > 1) { drawW = maxLogoSize; drawH = maxLogoSize / ratio } else { drawH = maxLogoSize; drawW = maxLogoSize * ratio }
            canvas.drawBitmap(radioLogo, null, RectF(streamWidth - drawW - marginX, marginY, streamWidth - marginX, marginY + drawH), logoPaint)
        }

        // --- 2. MARCADOR (REDUCIDO UN 30% ADICIONAL) ---
        val barHeight = (45 * scale).toInt() // De 65 a 45 (-30%)
        val scoreboardWidth = (streamWidth * 0.31f).toInt() // De 0.44 a 0.31 (-30%)
        val scoreboardX = marginX; val scoreboardY = marginY
        
        val slant = 8f * scale; val dividerWidth = 6f * scale; val timerWidth = scoreboardWidth * 0.22f; val gap = 4f * scale
        val glassColor = Color.argb(190, 15, 18, 28)
        
        val mainBarWidth = scoreboardWidth - timerWidth - gap
        val sideSectionWidth = (mainBarWidth - (slant * 2)) / 3f
        val homeBoxEnd = scoreboardX + sideSectionWidth + slant; val scoreBoxStart = homeBoxEnd; val scoreBoxEnd = scoreBoxStart + sideSectionWidth + slant; val awayBoxStart = scoreBoxEnd; val awayBoxEnd = scoreboardX + mainBarWidth

        // Bloques del Marcador
        generalPaint.color = glassColor
        canvas.drawPath(Path().apply { moveTo(scoreboardX, scoreboardY); lineTo(homeBoxEnd - dividerWidth, scoreboardY); lineTo(homeBoxEnd - slant - dividerWidth, scoreboardY + barHeight); lineTo(scoreboardX, scoreboardY + barHeight); close() }, generalPaint)
        generalPaint.color = homeColor
        canvas.drawPath(Path().apply { moveTo(homeBoxEnd - dividerWidth, scoreboardY); lineTo(homeBoxEnd, scoreboardY); lineTo(homeBoxEnd - (slant / 2f), scoreboardY + (barHeight / 2f)); lineTo(homeBoxEnd - dividerWidth - (slant / 2f), scoreboardY + (barHeight / 2f)); close() }, generalPaint)
        generalPaint.color = homeColor2
        canvas.drawPath(Path().apply { moveTo(homeBoxEnd - dividerWidth - (slant / 2f), scoreboardY + (barHeight / 2f)); lineTo(homeBoxEnd - (slant / 2f), scoreboardY + (barHeight / 2f)); lineTo(homeBoxEnd - slant, scoreboardY + barHeight); lineTo(homeBoxEnd - slant - dividerWidth, scoreboardY + barHeight); close() }, generalPaint)
        generalPaint.color = glassColor
        canvas.drawPath(Path().apply { moveTo(scoreBoxStart, scoreboardY); lineTo(scoreBoxEnd, scoreboardY); lineTo(scoreBoxEnd - slant, scoreboardY + barHeight); lineTo(scoreBoxStart - slant, scoreboardY + barHeight); close() }, generalPaint)
        generalPaint.color = awayColor
        canvas.drawPath(Path().apply { moveTo(scoreBoxEnd, scoreboardY); lineTo(scoreBoxEnd + dividerWidth, scoreboardY); lineTo(scoreBoxEnd + dividerWidth - (slant / 2f), scoreboardY + (barHeight / 2f)); lineTo(scoreBoxEnd - (slant / 2f), scoreboardY + (barHeight / 2f)); close() }, generalPaint)
        generalPaint.color = awayColor2
        canvas.drawPath(Path().apply { moveTo(scoreBoxEnd - (slant / 2f), scoreboardY + (barHeight / 2f)); lineTo(scoreBoxEnd + dividerWidth - (slant / 2f), scoreboardY + (barHeight / 2f)); lineTo(scoreBoxEnd + dividerWidth - slant, scoreboardY + barHeight); lineTo(scoreBoxEnd - slant, scoreboardY + barHeight); close() }, generalPaint)
        generalPaint.color = glassColor
        canvas.drawPath(Path().apply { moveTo(scoreBoxEnd + dividerWidth, scoreboardY); lineTo(awayBoxEnd, scoreboardY); lineTo(awayBoxEnd, scoreboardY + barHeight); lineTo(scoreBoxEnd + dividerWidth - slant, scoreboardY + barHeight); close() }, generalPaint)
        val timerXStart = scoreboardX + scoreboardWidth - timerWidth; val timerXEnd = scoreboardX + scoreboardWidth
        canvas.drawPath(Path().apply { moveTo(timerXStart, scoreboardY); lineTo(timerXEnd, scoreboardY); lineTo(timerXEnd, scoreboardY + barHeight); lineTo(timerXStart, scoreboardY + barHeight); close() }, generalPaint)

        // Rojas y Tiempo Extra (Escalados)
        val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.RED }
        repeat(homeRedCards) { i -> val rx = scoreboardX + 8f * scale + (i * 8f * scale); canvas.drawRect(rx, scoreboardY + 3f * scale, rx + 5f * scale, scoreboardY + 10f * scale, redPaint) }
        repeat(awayRedCards) { i -> val rx = awayBoxEnd - 15f * scale - (i * 8f * scale); canvas.drawRect(rx, scoreboardY + 3f * scale, rx + 5f * scale, scoreboardY + 10f * scale, redPaint) }
        if (extraTime > 0) { val exW = 35f * scale; val exH = 25f * scale; val exX = timerXEnd + 5f * scale; val exY = scoreboardY + (barHeight - exH) / 2f; generalPaint.color = glassColor; canvas.drawRoundRect(RectF(exX, exY, exX + exW, exY + exH), 3f * scale, 3f * scale, generalPaint); generalPaint.color = Color.parseColor("#FBBF24"); generalPaint.textSize = 18f * scale; generalPaint.textAlign = Paint.Align.CENTER; canvas.drawText("+$extraTime", exX + (exW / 2f), exY + (exH * 0.75f), generalPaint) }

        // Amonestación Lateral (Ajustada al nuevo tamaño)
        if (cautionAlert.isVisible) { val alphaInt = (cautionAlert.alpha * 255).toInt(); val alertPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = alphaInt }; val alertW = 200f * scale; val alertH = 35f * scale; val alertX = if (cautionAlert.isHome) scoreboardX else awayBoxEnd - alertW + (60f * scale); val alertY = scoreboardY + barHeight + (5f * scale); alertPaint.color = Color.argb((alphaInt * 0.9).toInt(), 15, 18, 28); canvas.drawRoundRect(RectF(alertX, alertY, alertX + alertW, alertY + alertH), 6f * scale, 6f * scale, alertPaint); alertPaint.color = cautionAlert.cardColor.toArgb(); canvas.drawRoundRect(RectF(alertX + 8f * scale, alertY + (alertH - 14f * scale) / 2f, alertX + 8f * scale + 10f * scale, alertY + (alertH + 14f * scale) / 2f), 2f * scale, 2f * scale, alertPaint); alertPaint.color = Color.WHITE; alertPaint.textSize = 18f * scale; alertPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); alertPaint.textAlign = Paint.Align.LEFT; canvas.drawText(cautionAlert.playerName.uppercase(), alertX + 30f * scale, alertY + (alertH * 0.7f), alertPaint) }

        // Textos del Marcador (Escalados)
        generalPaint.textAlign = Paint.Align.CENTER; generalPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); generalPaint.color = Color.WHITE; val ty = scoreboardY + (barHeight * 0.75f)
        generalPaint.textSize = 24f * scale; canvas.drawText(homeTeamName.take(3).uppercase(), scoreboardX + (sideSectionWidth / 2f), ty, generalPaint)
        canvas.drawText(awayTeamName.take(3).uppercase(), awayBoxStart + dividerWidth + (sideSectionWidth / 2f), ty, generalPaint)
        generalPaint.textSize = 30f * scale; canvas.drawText("$homeScore - $awayScore", scoreBoxStart + (sideSectionWidth / 2f) - (slant / 2f), ty + (1f * scale), generalPaint)
        val timerColor = if (isTimerRunning) Color.parseColor("#4ADE80") else Color.parseColor("#F87171")
        generalPaint.color = timerColor; generalPaint.textSize = 22f * scale; canvas.drawText(elapsedTime, timerXStart + (timerWidth / 2f), ty, generalPaint)

        // --- 3. VENTANA DE PUBLICIDAD (MÁS ANCHA Y TRANSPARENTE) ---
        if (adAlert.isVisible && adAlert.bitmap != null) {
            val alphaInt = (adAlert.alpha * 255).toInt()
            val adPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = alphaInt; isFilterBitmap = true }
            val adWindowW = 600f * scale; val adWindowH = 100f * scale
            val adX = (streamWidth - adWindowW) / 2f; val adY = streamHeight - adWindowH - (25f * scale)
            adPaint.color = Color.argb((alphaInt * 0.86f).toInt(), 15, 18, 28)
            canvas.drawRoundRect(RectF(adX, adY, adX + adWindowW, adY + adWindowH), 12f * scale, 12f * scale, adPaint)
            val srcW = adAlert.bitmap.width.toFloat(); val srcH = adAlert.bitmap.height.toFloat()
            val adMargin = 12f * scale
            val maxDrawW = adWindowW - (adMargin * 2); val maxDrawH = adWindowH - (adMargin * 2)
            val adRatio = srcW / srcH
            val drawW: Float; val drawH: Float
            if (adRatio > (maxDrawW / maxDrawH)) { drawW = maxDrawW; drawH = maxDrawW / adRatio } else { drawH = maxDrawH; drawW = maxDrawH * adRatio }
            canvas.drawBitmap(adAlert.bitmap, null, RectF(adX + (adWindowW - drawW) / 2f, adY + (adWindowH - drawH) / 2f, adX + (adWindowW + drawW) / 2f, adY + (adWindowH + drawH) / 2f), adPaint)
        }

        return bitmap
    }
}
