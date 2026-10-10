package com.app.relatosfutbolerosnd_20.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class KeyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        var out = ""
        for (i in originalText.indices) {
            out += originalText[i]
            if (i % 4 == 3 && i != originalText.lastIndex) out += "-"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return offset
                val separators = (offset - 1) / 4
                return offset + separators
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return offset
                val separators = offset / 5
                return (offset - separators).coerceIn(0, originalText.length)
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}
