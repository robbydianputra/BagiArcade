package com.bagicode.games.update.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import com.bagicode.games.update.model.UpdateData

@Composable
fun UpdateDialog(
    updateData: UpdateData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        // Kunci dialog jika force update (tidak bisa klik di luar dialog atau tombol back)
        properties = DialogProperties(
            dismissOnBackPress = !updateData.isForceUpdate,
            dismissOnClickOutside = !updateData.isForceUpdate
        ),
        onDismissRequest = {
            if (!updateData.isForceUpdate) onDismiss()
        },
        title = {
            Text(text = if (updateData.isForceUpdate) "Update Wajib Tersedia!" else "Versi Baru Tersedia")
        },
        text = {
            LazyColumn {
                item {
                    Text(text = "Versi ${updateData.latestVersion} membawa perubahan baru:")
                }
                items(updateData.changelog) { change ->
                    Text(text = "• $change")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { openPlayStore(context, updateData.url, updateData.urlFallback) }
            ) {
                Text("Update Sekarang")
            }
        },
        dismissButton = {
            // Sembunyikan tombol batal jika terjadi Force Update
            if (!updateData.isForceUpdate) {
                TextButton(onClick = onDismiss) {
                    Text("Nanti Saja")
                }
            }
        }
    )
}

// Fungsi Intent untuk handle link market dan fallback browser
fun openPlayStore(context: Context, marketUrl: String, fallbackUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(marketUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Jika aplikasi Play Store tidak ada (misal di Emulator), buka Browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
