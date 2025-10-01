@file:Suppress("SpellCheckingInspection")

package com.arkhe.csd.utils

import android.content.ClipData
import android.content.Context
import android.widget.Toast
import android.content.ClipboardManager as AndroidClipboardManager

class ClipboardManager(private val context: Context) {

    private val clipboard =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager

    fun copyToClipboard(text: String, label: String = "Copied Text") {
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Text berhasil di-copy! 📋", Toast.LENGTH_SHORT).show()
    }

    fun copyWithInjection(baseText: String, injectedData: Map<String, String>): String {
        var result = baseText
        injectedData.forEach { (key, value) ->
            result = result.replace("{{$key}}", value)
        }
        copyToClipboard(result, "Copied with Injection")
        return result
    }
}