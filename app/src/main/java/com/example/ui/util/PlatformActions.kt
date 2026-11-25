package com.example.ui.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PlatformActions {

    fun copyToClipboard(context: Context, label: String, text: String): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                ?: return false
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
            true
        } catch (_: Exception) {
            false
        }
    }

    fun shareText(context: Context, title: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun playFeedbackTone(enabled: Boolean) {
        if (!enabled) return
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val tone = ToneGenerator(AudioManager.STREAM_SYSTEM, 28)
                tone.startTone(ToneGenerator.TONE_PROP_BEEP, 45)
                kotlinx.coroutines.delay(80)
                tone.release()
            } catch (_: Exception) {
                // Ignore audio hardware failures gracefully
            }
        }
    }

    fun printExplanation(context: Context, title: String, reportBody: String) {
        val escaped = reportBody
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\n", "<br/>")

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8"/>
              <style>
                body { font-family: sans-serif; padding: 24px; color: #0f172a; line-height: 1.55; }
                h1 { color: #1d4ed8; font-size: 20px; border-bottom: 2px solid #cbd5e1; padding-bottom: 8px; }
                pre, .body { font-family: monospace; font-size: 13px; white-space: pre-wrap; }
              </style>
            </head>
            <body>
              <h1>$title</h1>
              <div class="body">$escaped</div>
            </body>
            </html>
        """.trimIndent()

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val jobName = "NumiX_Explanation"
                val adapter = view?.createPrintDocumentAdapter(jobName)
                if (printManager != null && adapter != null) {
                    printManager.print(
                        jobName,
                        adapter,
                        PrintAttributes.Builder().build()
                    )
                }
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
}
