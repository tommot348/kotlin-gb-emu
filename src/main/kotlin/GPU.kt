package de.prt.gb

data class colors(
    val white: Int = 0,
    val light: Int = 1,
    val dark: Int = 2,
    val black: Int = 3)
private fun getBit(of: Short, nr: Int): Char {
  return of.toString(2).padStart(8, '0').get(nr - 7)
}
object GPU {
  var state = 2
  fun tick() {
    val lcdc = RAM.getByteAt(0xFF40)
    if (getBit(lcdc, 7) == '1') {
      val WindowTileMap = if (getBit(lcdc, 6) == '0') {
        (0x9800..0x9BFF).map({ RAM.getByteAt(it) })
      } else {
        (0x9C00..0x9FFF).map({ RAM.getByteAt(it) })
      }
      val showWindow = (getBit(lcdc, 5) == '1')
      val BGandWindowTileData = if (getBit(lcdc, 4) == '0') {
        (0x8800..0x97FF).map({ RAM.getByteAt(it) })
      } else {
        (0x8000..0x8FFF).map({ RAM.getByteAt(it) })
      }
      val BGTileMap = if (getBit(lcdc, 3) == '0') {
        (0x9800..0x9BFF).map({ RAM.getByteAt(it) })
      } else {
        (0x9C00..0x9FFF).map({ RAM.getByteAt(it) })
      }
      val spriteSize = getBit(lcdc, 2)
      val showSprites = (getBit(lcdc, 1) == '1')
      val stat = RAM.getByteAt(0xFF41)
      
      val scy = RAM.getByteAt(0xFF42)
      val scx = RAM.getByteAt(0xFF43)
      val ly = RAM.getByteAt(0xFF44)
      val lyc = RAM.getByteAt(0xFF45)
      val bgp = RAM.getByteAt(0xFF47)
      val obp0 = RAM.getByteAt(0xFF48)
      val obp1 = RAM.getByteAt(0xFF49)
      val wy = RAM.getByteAt(0xFF4A)
      val wx = RAM.getByteAt(0xFF4B)
      val oam: List<Short> = (0xFE00..0xFE9F).map({ RAM.getByteAt(it) })
    }
  }
}
