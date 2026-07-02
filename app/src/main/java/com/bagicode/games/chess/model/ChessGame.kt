package com.bagicode.games.chess.model

enum class PieceType { PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING }
enum class Player { WHITE, BLACK }
data class ChessPiece(val type: PieceType, val player: Player, var hasMoved: Boolean = false)

class ChessGame {
    var board = Array(8) { arrayOfNulls<ChessPiece>(8) }
    var currentTurn = Player.WHITE
    var selectedSquare: Pair<Int, Int>? = null
    var isGameOver = false
    var winner: Player? = null
    var onPromotionRequired: ((row: Int, col: Int) -> Unit)? = null
    private var pendingPromotion: Pair<Int, Int>? = null

    init {
        resetGame()
    }

    fun resetGame() {
        board = Array(8) { arrayOfNulls<ChessPiece>(8) }
        currentTurn = Player.WHITE
        selectedSquare = null
        isGameOver = false
        winner = null
        pendingPromotion = null

        // Set up pieces
        setupRow(0, Player.BLACK)
        setupPawns(1, Player.BLACK)
        setupPawns(6, Player.WHITE)
        setupRow(7, Player.WHITE)
    }

    private fun setupRow(row: Int, player: Player) {
        board[row][0] = ChessPiece(PieceType.ROOK, player)
        board[row][1] = ChessPiece(PieceType.KNIGHT, player)
        board[row][2] = ChessPiece(PieceType.BISHOP, player)
        board[row][3] = ChessPiece(PieceType.QUEEN, player)
        board[row][4] = ChessPiece(PieceType.KING, player)
        board[row][5] = ChessPiece(PieceType.BISHOP, player)
        board[row][6] = ChessPiece(PieceType.KNIGHT, player)
        board[row][7] = ChessPiece(PieceType.ROOK, player)
    }

    private fun setupPawns(row: Int, player: Player) {
        for (col in 0..7) {
            board[row][col] = ChessPiece(PieceType.PAWN, player)
        }
    }

    fun isPromotionPending(): Boolean = pendingPromotion != null

    fun handleTouch(row: Int, col: Int): Boolean {
        if (isGameOver || pendingPromotion != null) return false
        val piece = board[row][col]
        
        if (selectedSquare == null) {
            if (piece?.player == currentTurn) {
                selectedSquare = row to col
                return true
            }
        } else {
            val (fromRow, fromCol) = selectedSquare!!
            if (fromRow == row && fromCol == col) {
                selectedSquare = null
                return true
            }
            
            if (isValidMoveAndSafe(fromRow, fromCol, row, col)) {
                executeMove(fromRow, fromCol, row, col)
                selectedSquare = null
                
                // Check for pawn promotion
                val movedPiece = board[row][col]
                if (movedPiece?.type == PieceType.PAWN && (row == 0 || row == 7)) {
                    pendingPromotion = row to col
                    onPromotionRequired?.invoke(row, col)
                    return true
                }

                completeTurn()
                return true
            } else if (piece?.player == currentTurn) {
                selectedSquare = row to col
                return true
            }
        }
        return false
    }

    private fun completeTurn() {
        currentTurn = if (currentTurn == Player.WHITE) Player.BLACK else Player.WHITE
        checkGameOver()
    }

    fun promotePawn(type: PieceType) {
        val (row, col) = pendingPromotion ?: return
        val player = board[row][col]?.player ?: return
        board[row][col] = ChessPiece(type, player, hasMoved = true)
        pendingPromotion = null
        completeTurn()
    }

    private fun executeMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int) {
        val piece = board[fromRow][fromCol] ?: return
        
        // Handle Castling Move
        if (piece.type == PieceType.KING && Math.abs(toCol - fromCol) == 2) {
            val isKingSide = toCol > fromCol
            val rookCol = if (isKingSide) 7 else 0
            val newRookCol = if (isKingSide) 5 else 3
            val rook = board[fromRow][rookCol]
            board[fromRow][newRookCol] = rook
            board[fromRow][rookCol] = null
            rook?.hasMoved = true
        }

        board[toRow][toCol] = piece
        board[fromRow][fromCol] = null
        piece.hasMoved = true
    }

    fun isValidMoveAndSafeForPrediction(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        return isValidMoveAndSafe(fromRow, fromCol, toRow, toCol)
    }

    private fun isValidMoveAndSafe(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        if (!isValidMove(board, fromRow, fromCol, toRow, toCol)) return false
        
        // Special check for castling safety (King doesn't pass through check)
        val piece = board[fromRow][fromCol]
        if (piece?.type == PieceType.KING && Math.abs(toCol - fromCol) == 2) {
            if (isInCheck(board, currentTurn)) return false
            val step = if (toCol > fromCol) 1 else -1
            val tempBoard = copyBoard(board)
            tempBoard[fromRow][fromCol + step] = tempBoard[fromRow][fromCol]
            tempBoard[fromRow][fromCol] = null
            if (isInCheck(tempBoard, currentTurn)) return false
        }

        // Check if move leaves king in check
        val tempBoard = copyBoard(board)
        tempBoard[toRow][toCol] = tempBoard[fromRow][fromCol]
        tempBoard[fromRow][fromCol] = null
        
        return !isInCheck(tempBoard, currentTurn)
    }

    private fun isValidMove(currentBoard: Array<Array<ChessPiece?>>, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        val piece = currentBoard[fromRow][fromCol] ?: return false
        val targetPiece = currentBoard[toRow][toCol]
        if (targetPiece?.player == piece.player) return false

        val deltaRow = toRow - fromRow
        val deltaCol = toCol - fromCol

        return when (piece.type) {
            PieceType.PAWN -> {
                val direction = if (piece.player == Player.WHITE) -1 else 1
                if (deltaCol == 0 && targetPiece == null) {
                    if (deltaRow == direction) true
                    else if (deltaRow == 2 * direction && ((piece.player == Player.WHITE && fromRow == 6) || (piece.player == Player.BLACK && fromRow == 1))) {
                        currentBoard[fromRow + direction][fromCol] == null
                    } else false
                } else if (Math.abs(deltaCol) == 1 && deltaRow == direction && targetPiece != null) {
                    true
                } else false
            }
            PieceType.ROOK -> (deltaRow == 0 || deltaCol == 0) && isPathClear(currentBoard, fromRow, fromCol, toRow, toCol)
            PieceType.KNIGHT -> (Math.abs(deltaRow) == 2 && Math.abs(deltaCol) == 1) || (Math.abs(deltaRow) == 1 && Math.abs(deltaCol) == 2)
            PieceType.BISHOP -> Math.abs(deltaRow) == Math.abs(deltaCol) && isPathClear(currentBoard, fromRow, fromCol, toRow, toCol)
            PieceType.QUEEN -> (deltaRow == 0 || deltaCol == 0 || Math.abs(deltaRow) == Math.abs(deltaCol)) && isPathClear(currentBoard, fromRow, fromCol, toRow, toCol)
            PieceType.KING -> {
                if (Math.abs(deltaRow) <= 1 && Math.abs(deltaCol) <= 1) true
                else if (deltaRow == 0 && Math.abs(deltaCol) == 2 && !piece.hasMoved) {
                    // Castling Logic
                    val rookCol = if (deltaCol > 0) 7 else 0
                    val rook = currentBoard[fromRow][rookCol]
                    rook?.type == PieceType.ROOK && !rook.hasMoved && isPathClear(currentBoard, fromRow, fromCol, fromRow, rookCol)
                } else false
            }
        }
    }

    private fun isPathClear(currentBoard: Array<Array<ChessPiece?>>, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        val stepRow = if (toRow > fromRow) 1 else if (toRow < fromRow) -1 else 0
        val stepCol = if (toCol > fromCol) 1 else if (toCol < fromCol) -1 else 0
        
        var currentRow = fromRow + stepRow
        var currentCol = fromCol + stepCol
        
        while (currentRow != toRow || currentCol != toCol) {
            if (currentBoard[currentRow][currentCol] != null) return false
            currentRow += stepRow
            currentCol += stepCol
        }
        return true
    }

    fun isInCheck(currentBoard: Array<Array<ChessPiece?>>, player: Player): Boolean {
        var kingPos: Pair<Int, Int>? = null
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = currentBoard[r][c]
                if (piece?.type == PieceType.KING && piece.player == player) {
                    kingPos = r to c
                    break
                }
            }
        }
        
        kingPos ?: return false
        
        val opponent = if (player == Player.WHITE) Player.BLACK else Player.WHITE
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = currentBoard[r][c]
                if (piece?.player == opponent) {
                    if (isValidMove(currentBoard, r, c, kingPos.first, kingPos.second)) return true
                }
            }
        }
        return false
    }

    private fun checkGameOver() {
        if (isCheckmate(currentTurn)) {
            isGameOver = true
            winner = if (currentTurn == Player.WHITE) Player.BLACK else Player.WHITE
        } else if (isStalemate(currentTurn)) {
            isGameOver = true
            winner = null // Draw
        }
    }

    private fun isCheckmate(player: Player): Boolean {
        return isInCheck(board, player) && !hasValidMoves(player)
    }

    private fun isStalemate(player: Player): Boolean {
        return !isInCheck(board, player) && !hasValidMoves(player)
    }

    private fun hasValidMoves(player: Player): Boolean {
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board[r][c]
                if (piece?.player == player) {
                    for (tr in 0..7) {
                        for (tc in 0..7) {
                            if (isValidMoveAndSafe(r, c, tr, tc)) return true
                        }
                    }
                }
            }
        }
        return false
    }

    private fun copyBoard(original: Array<Array<ChessPiece?>>): Array<Array<ChessPiece?>> {
        return Array(8) { r -> 
            Array(8) { c ->
                original[r][c]?.copy()
            }
        }
    }

    fun resign(player: Player) {
        isGameOver = true
        winner = if (player == Player.WHITE) Player.BLACK else Player.WHITE
    }
}
