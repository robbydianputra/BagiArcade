package com.bagicode.games.maze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MazeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val gridSize = 8 // Labirin berukuran 8x8 kotak

    // State untuk menyimpan daftar dinding labirin yang di-generate acak
    var mazeWalls by remember { mutableStateOf(generateRandomMaze(gridSize)) }

    // State untuk mencatat titik-titik garis coretan tangan anak
    val drawPoints = remember { mutableStateListOf<Offset>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Bagian Atas: Judul Instruksi
        Text(
            text = "Bantu Anak Menuju Rumah! 🏠🏃‍♂️",
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 16.dp)
        )

        // Bagian Tengah: Canvas Labirin Utama
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Mengunci rasio kotak sempurna (1:1)
                .background(Color.White)
                .pointerInput(Unit) {
                    // Deteksi usapan jari anak untuk menggambar garis coretan bebas
                    detectDragGestures(
                        onDragStart = { offset ->
                            drawPoints.add(offset)
                        },
                        onDrag = { change, _ ->
                            drawPoints.add(change.position)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
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
//                mazeWalls.forEach { wall ->
//                    drawLine(
//                        color = Color.Black,
//                        start = Offset(wall.startX * cellWidth, wall.startY * cellHeight),
//                        end = Offset(wall.endX * cellWidth, wall.endY * cellHeight),
//                        strokeWidth = 8f,
//                        cap = StrokeCap.Round
//                    )
//                }
                mazeWalls.forEach { wall ->
                    drawLine(
                        color = Color.Black,
                        start = Offset(wall.startX * cellWidth, wall.startY * cellHeight),
                        end = Offset(wall.endX * cellWidth, wall.endY * cellHeight),
                        strokeWidth = 8f,
                        cap = StrokeCap.Square // DIUBAH DISINI: Supaya sudut pertemuan antar dinding kotak sempurna kaku
                    )
                }

                // 3. Gambar Coretan Garis Tangan Anak (Warna Biru Cerah)
                if (drawPoints.size > 1) {
                    for (i in 0 until drawPoints.size - 1) {
                        drawLine(
                            color = Color(0xFF1E88E5),
                            start = drawPoints[i],
                            end = drawPoints[i + 1],
                            strokeWidth = 12f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Tanda Posisi Mulai (Anak di pojok kiri bawah)
            Text(
                text = "👦",
                fontSize = 32.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
            )

            // Tanda Posisi Selesai (Rumah di pojok kanan atas)
            Text(
                text = "🏠",
                fontSize = 32.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            )
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
                Text("Hapus Garis ❌", fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Tombol Next untuk Mengacak Rute/Labirin Baru
            Button(
                onClick = {
                    mazeWalls = generateRandomMaze(gridSize) // Acak rute baru
                    drawPoints.clear() // Hapus coretan otomatis saat ganti rute
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Rute Baru 🎲", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

/**
 * Fungsi pembantu untuk membuat struktur dinding labirin acak berdasarkan algoritma Kruskal sederhana
 * Memastikan labirin selalu terhubung dan menghasilkan rute bervariasi setiap dipanggil
 */
//fun generateRandomMaze(size: Int): List<MazeWall> {
//    val walls = mutableListOf<MazeWall>()
//
//    // Masukkan semua kemungkinan batas dinding dalam grid kotak
//    for (x in 0 until size) {
//        for (y in 0 until size) {
//            if (x < size - 1) walls.add(MazeWall(x + 1, y, x + 1, y + 1)) // Dinding Vertikal
//            if (y < size - 1) walls.add(MazeWall(x, y + 1, x + 1, y + 1)) // Dinding Horizontal
//        }
//    }
//
//    // Acak urutan seluruh dinding pendukung
//    walls.shuffle()
//
//    // Ambil sebagian besar susunan dinding acak untuk membentuk rute jalur labirin unik
//    // Menyisakan jalan masuk di kiri bawah (0, size) dan pintu keluar di kanan atas (size, 0)
//    val mazeStructure = walls.take((walls.size * 0.65).toInt()).toMutableList()
//
//    // Tambahkan bingkai pembatas luar labirin
//    for (i in 0 until size) {
//        if (i != 0) mazeStructure.add(MazeWall(0, i, 0, i + 1)) // Sisi Kiri
//        if (i != size - 1) mazeStructure.add(MazeWall(size, i, size, i + 1)) // Sisi Kanan
//        mazeStructure.add(MazeWall(i, 0, i + 1, 0)) // Sisi Atas
//        mazeStructure.add(MazeWall(i, size, i + 1, size)) // Sisi Bawah
//    }
//
//    return mazeStructure
//}
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

