package de.prt.gb

data class colors(
    val white: Int = 0,
    val light: Int = 0,
    val dark: Int = 0,
    val black: Int = 0)
object GPU {
  var state = 2
  fun tick() {
    val lcdc = RAM.getByteAt(0xFF40)
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
    val tiles: List<Short> = (0x8000..0x97FF).map({ RAM.getByteAt(it) })
    val bgm0: List<Short> = (0x9800..0x9BFF).map({ RAM.getByteAt(it) })
    val bgm1: List<Short> = (0x9C00..0x9FFF).map({ RAM.getByteAt(it) })
    val oam: List<Short> = (0xFE00..0xFE9F).map({ RAM.getByteAt(it) })
  }
}
