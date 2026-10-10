package com.app.relatosfutbolerosnd_20.ui.stream

import android.graphics.Bitmap
import android.opengl.GLES20
import android.os.Build
import androidx.annotation.RequiresApi
import com.pedro.encoder.input.gl.render.filters.`object`.BaseObjectFilterRender
import com.pedro.encoder.utils.gl.ImageStreamObject

/**
 * Filtro OpenGL ES que renderiza un marcador de fútbol como overlay
 * sobre el stream de video. Extiende BaseObjectFilterRender para aprovechar
 * el pipeline de texturas bidireccionales (cámara + objeto).
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class ScoreboardObjectFilterRender : BaseObjectFilterRender() {

    private val imageStreamObject = ImageStreamObject()

    init {
        streamObject = imageStreamObject
        alpha = 1f
    }

    override fun drawFilter() {
        super.drawFilter()
        // Vincular la textura del marcador al sampler uObject
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, streamObjectTextureId[0])
        // Aplicar alpha (0 si no hay imagen cargada aún)
        GLES20.glUniform1f(uAlphaHandle, if (streamObjectTextureId[0] == -1) 0f else alpha)
    }

    /**
     * Actualiza el bitmap del marcador. Este método es thread-safe
     * porque shouldLoad se lee en el thread de GL durante drawFilter().
     */
    fun updateScoreboard(bitmap: Bitmap) {
        imageStreamObject.load(bitmap)
        shouldLoad = true
    }

    fun setScoreboardAlpha(alphaValue: Float) {
        alpha = alphaValue.coerceIn(0f, 1f)
    }

    override fun release() {
        super.release()
    }
}