package com.bagicode.games.maze

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MazeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                MazeGameScreen()
            }
        }
    }
}

// Model data untuk merepresentasikan dinding labirin
data class MazeWall(val startX: Int, val startY: Int, val endX: Int, val endY: Int)

@Composable
fun MazeGameScreen() {
    val context = LocalContext.current

    // 1. UBAH gridSize MENJADI STATE (Mulai dari level awal: 6x6 atau 8x8)
    var gridSize by remember { mutableStateOf(2) }

    // 2. RE-GENERATE MAZE OTOMATIS SETIAP KALI gridSize BERUBAH
    var mazeWalls by remember(gridSize) { mutableStateOf(generateRandomMaze(gridSize)) }

    // State untuk mencatat titik-titik garis coretan tangan anak
    val drawPoints = remember { mutableStateListOf<Offset>() }

    // State bantuan untuk mencatat ukuran piksel canvas asli secara real-time
    var canvasSize by remember { mutableStateOf(Offset.Zero) }

    // State pemicu munculnya dialog menang
    var showWinDialog by remember { mutableStateOf(false) }

    // Tampilkan dialog jika anak berhasil mencapai rumah
    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Hore, Level Up! 🆙🎉") },
            text = { Text("Hebat sekali! Labirin berikutnya akan menjadi lebih besar dan lebih menantang!") },
            confirmButton = {
                Button(
                    onClick = {
                        showWinDialog = false
                        drawPoints.clear() // Bersihkan coretan lama
                        gridSize = if (gridSize == 8) 8 else gridSize + 1
                        mazeWalls = generateRandomMaze(gridSize) // Mengacak ulang berdasarkan gridSize saat ini
                        drawPoints.clear()
                    }
                ) {
                    Text("Main Lagi 🎲")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .safeDrawingPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Bagian Atas: Bar Judul & Tombol Back
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // 1. Tombol Back / Kembali
            IconButton(
                onClick = {
                    // Menutup MazeActivity dan otomatis kembali ke HomeActivity
                    (context as? android.app.Activity)?.finish()
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Menggunakan ikon panah kiri bawaan Google
                    contentDescription = "Tombol Kembali",
                    tint = Color(0xFFE53935) // Mengubah warna ikon menjadi merah agar kontras
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Judul Instruksi Game
            Text(
                text = "Bantu Anak 🏃‍♂️ Menuju Rumah 🏠",
                fontSize = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                color = Color.DarkGray
            )
        }

//        Text(
//            text = "Gambarlah garis untuk menghubungkan 👦 dan 🏠",
//            fontSize = 16.sp,
//            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
//            color = Color.DarkGray
//        )

        // Bagian Tengah: Canvas Labirin Utama
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Mengunci rasio kotak sempurna (1:1)
                .background(Color.White)
                .pointerInput(mazeWalls, canvasSize) { // Reset gesture jika rute labirin diacak ulang
                    if (canvasSize == Offset.Zero) return@pointerInput

                    val cellWidth = canvasSize.x / gridSize
                    val cellHeight = canvasSize.y / gridSize

                    // Deteksi usapan jari anak untuk menggambar garis
                    detectDragGestures(
                        onDragStart = { offset ->
                            // Validasi awal: Harus mulai mengusap dari area dekat Anak (Pojok Kiri Bawah)
                            val startAreaX = 0f..cellWidth
                            val startAreaY = (gridSize - 1) * cellHeight..canvasSize.y

                            if (offset.x in startAreaX && offset.y in startAreaY) {
                                drawPoints.add(offset)
                            } else {
                                Toast.makeText(context, "Mulai dari posisi Anak (👦) dulu ya!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDrag = { change, _ ->
                            val nextPoint = change.position

                            // Abaikan usapan jika jari keluar dari batas layar kotak kanvas
                            if (nextPoint.x !in 0f..canvasSize.x || nextPoint.y !in 0f..canvasSize.y) {
                                return@detectDragGestures
                            }

                            if (drawPoints.isNotEmpty()) {
                                val lastPoint = drawPoints.last()

                                // 1. LOGIKA DETEKSI TABRAKAN TEMBOK
                                var isHitWall = false
                                for (wall in mazeWalls) {
                                    val wStart = Offset(wall.startX * cellWidth, wall.startY * cellHeight)
                                    val wEnd = Offset(wall.endX * cellWidth, wall.endY * cellHeight)

                                    // Cek matematika apakah garis tarikan jari memotong garis dinding hitam
                                    if (isLinesIntersecting(lastPoint, nextPoint, wStart, wEnd)) {
                                        isHitWall = true
                                        break
                                    }
                                }

                                if (isHitWall) {
                                    // Jika menabrak, coretan langsung terputus dan dihapus otomatis
                                    Toast.makeText(context, "Aduh, nabrak tembok! ❌ Ulangi lagi.", Toast.LENGTH_SHORT).show()
                                    drawPoints.clear()
                                } else {
                                    drawPoints.add(nextPoint)

                                    // 2. LOGIKA DETEKSI SAMPAI TUJUAN (RUMAH)
                                    // Rumah berada di Pojok Kanan Atas (Kotak koordinat X terakhir, Y awal)
                                    val winAreaX = (gridSize - 1) * cellWidth..canvasSize.x
                                    val winAreaY = 0f..cellHeight

                                    if (nextPoint.x in winAreaX && nextPoint.y in winAreaY) {
                                        showWinDialog = true
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Catat resolusi piksel asli canvas secara dinamis saat pertama kali ter-render
                if (canvasSize.x != size.width || canvasSize.y != size.height) {
                    canvasSize = Offset(size.width, size.height)
                }

                val cellWidth = size.width / gridSize
                val cellHeight = size.height / gridSize

                // 1. Gambar Grid Dasar / Jalan Labirin (Garis Abu Halus)
                for (i in 0..gridSize) {
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(i * cellWidth, 0f),
                        end = Offset(i * cellWidth, size.height),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, i * cellHeight),
                        end = Offset(size.width, i * cellHeight),
                        strokeWidth = 1f
                    )
                }

                // 2. Gambar Dinding Labirin yang Menghadang Jalan (Garis Hitam Tebal)
                mazeWalls.forEach { wall ->
                    drawLine(
                        color = Color.Black,
                        start = Offset(wall.startX * cellWidth, wall.startY * cellHeight),
                        end = Offset(wall.endX * cellWidth, wall.endY * cellHeight),
                        strokeWidth = 10f,
                        cap = StrokeCap.Square // Supaya sudut pertemuan antar dinding kotak sempurna kaku
                    )
                }

                // 3. Gambar Coretan Garis Tangan Anak (Warna Biru Cerah)
                if (drawPoints.size > 1) {
                    for (i in 0 until drawPoints.size - 1) {
                        drawLine(
                            color = Color(0xFF1E88E5),
                            start = drawPoints[i],
                            end = drawPoints[i + 1],
                            strokeWidth = 14f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

//            // Tanda Posisi Mulai (Anak di pojok kiri bawah)
//            Text(
//                text = "👦",
//                fontSize = 32.sp,
//                modifier = Modifier
//                    .align(Alignment.BottomStart)
//                    .padding(4.dp)
//            )
//
//            // Tanda Posisi Selesai (Rumah di pojok kanan atas)
//            Text(
//                text = "🏠",
//                fontSize = 32.sp,
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .padding(4.dp)
//            )

            // ================================================================
            // LOGIKA HITUNG UKURAN DINDING / KOTAK SECARA DINAMIS
            // ================================================================
            val density = LocalDensity.current
            if (canvasSize != Offset.Zero) {
                val cellWidthPx = canvasSize.x / gridSize
                val cellHeightPx = canvasSize.y / gridSize

                // Konversi ukuran pixel kotak ke satuan DP untuk ukuran komponen
                val cellWidthDp = with(density) { cellWidthPx.toDp() }
                val cellHeightDp = with(density) { cellHeightPx.toDp() }

                // Tentukan ukuran Text/Font (Kira-kira 60% dari tinggi kotak agar pas dan ada ruang bernapas)
                val emojiFontSize = with(density) { (cellHeightPx * 0.6f).toSp() }

                // Posisi Mulai Anak (👦): Pojok Kiri Bawah -> Kolom 0, Baris ke (gridSize - 1)
                Box(
                    modifier = Modifier
                        .size(width = cellWidthDp, height = cellHeightDp)
                        .offset(
                            x = 0.dp,
                            y = cellHeightDp * (gridSize - 1)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👦", fontSize = emojiFontSize)
                }

                // Posisi Selesai Rumah (🏠): Pojok Kanan Atas -> Kolom ke (gridSize - 1), Baris 0
                Box(
                    modifier = Modifier
                        .size(width = cellWidthDp, height = cellHeightDp)
                        .offset(
                            x = cellWidthDp * (gridSize - 1),
                            y = 0.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🏠", fontSize = emojiFontSize)
                }
            }
        }

        // Bagian Bawah: Tombol Navigasi Aksi
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Tombol Cancel untuk Menghapus Coretan Garis
            Button(
                onClick = { drawPoints.clear() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Hapus Rute ❌", fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Tombol Next untuk Mengacak Rute/Labirin Baru di level yang sama
            Button(
                onClick = {
                    mazeWalls = generateRandomMaze(gridSize) // Mengacak ulang berdasarkan gridSize saat ini
                    drawPoints.clear()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Rute Baru 🎲", fontSize = 16.sp, color = Color.White)
            }

        }
    }
}

fun generateRandomMaze(size: Int): List<MazeWall> {
    val walls = mutableListOf<MazeWall>()
    val visited = Array(size) { BooleanArray(size) { false } }

    // List untuk menampung semua dinding horizontal dan vertikal internal penuh dulu
    val allVerticalWalls = Array(size + 1) { BooleanArray(size + 1) { true } }
    val allHorizontalWalls = Array(size + 1) { BooleanArray(size + 1) { true } }

    // Fungsi RECURSIVE BACKTRACKING untuk membuat jalur labirin yang valid
    fun carvePath(cx: Int, cy: Int) {
        visited[cx][cy] = true

        // Arah gerakan: Kanan, Bawah, Kiri, Atas
        val directions = listOf(Pair(1, 0), Pair(0, 1), Pair(-1, 0), Pair(0, -1)).shuffled()

        for (dir in directions) {
            val nx = cx + dir.first
            val ny = cy + dir.second

            if (nx in 0 until size && ny in 0 until size && !visited[nx][ny]) {
                // Bongkar dinding pembatas antara cell saat ini dengan cell tujuan
                if (dir.first == 1) allVerticalWalls[cx + 1][cy] = false     // Bongkar kanan
                if (dir.first == -1) allVerticalWalls[cx][cy] = false       // Bongkar kiri
                if (dir.second == 1) allHorizontalWalls[cx][cy + 1] = false   // Bongkar bawah
                if (dir.second == -1) allHorizontalWalls[cx][cy] = false     // Bongkar atas

                carvePath(nx, ny)
            }
        }
    }

    // Mulai membuat jalur dari titik awal (0, size - 1) yaitu pojok kiri bawah tempat anak berada
    carvePath(0, size - 1)

    // Konversi hasil hancuran dinding tadi menjadi List<MazeWall> untuk digambar di Canvas
    for (x in 0..size) {
        for (y in 0..size) {
            // Gambar dinding vertikal jika tidak dihancurkan
            if (x in 1 until size && y in 0 until size && allVerticalWalls[x][y]) {
                walls.add(MazeWall(x, y, x, y + 1))
            }
            // Gambar dinding horizontal jika tidak dihancurkan
            if (y in 1 until size && x in 0 until size && allHorizontalWalls[x][y]) {
                walls.add(MazeWall(x, y, x + 1, y))
            }
        }
    }

    // Tambahkan bingkai pembatas luar labirin (border hitam)
    for (i in 0 until size) {
        if (i != 0) walls.add(MazeWall(0, i, 0, i + 1)) // Sisi Kiri (Sisakan pintu masuk di kiri bawah)
        if (i != size - 1) walls.add(MazeWall(size, i, size, i + 1)) // Sisi Kanan (Sisakan pintu keluar di kanan atas dekat rumah)
        walls.add(MazeWall(i, 0, i + 1, 0)) // Sisi Atas
        walls.add(MazeWall(i, size, i + 1, size)) // Sisi Bawah
    }

    return walls
}

/**
 * Fungsi Matematika CCW (Counter-Clockwise) & Intersect
 * Berguna memvalidasi apakah garis coretan memotong koordinat garis dinding hitam
 */
fun ccw(A: Offset, B: Offset, C: Offset): Boolean {
    return (C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x)
}

fun isLinesIntersecting(p1: Offset, p2: Offset, p3: Offset, p4: Offset): Boolean {
    return ccw(p1, p3, p4) != ccw(p2, p3, p4) && ccw(p1, p2, p3) != ccw(p1, p2, p4)
}

