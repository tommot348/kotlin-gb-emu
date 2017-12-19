package de.prt.gb

object GPU {
  var state = 2
  private fun getBit(of: Short, nr: Int): Char {
    return of.toString(2).padStart(8, '0').get(nr - 7)
  }
  private fun byteToPalette(input: Short): List<Int> {
    val ps = input.toString(2).padStart(8, '0')
    return listOf(ps.substring(6).toInt(2),
                  ps.substring(4, 6).toInt(2),
                  ps.substring(2, 4).toInt(2),
                  ps.substring(0, 2).toInt(2))
  }
  private fun byteToColorData(least: Short, most: Short, palette: List<Int>): List<Int> {
    val binaryLeast = least.toString(2).padStart(8, '0')
    val binaryMost = most.toString(2).padStart(8, '0')
    return (7 downTo 0).map({ palette["${binaryMost[it]}${binaryLeast[it]}".toInt(2)] })
  }
  fun tick() {
    val lcdc = RAM.getByteAt(0xFF40)
    if (getBit(lcdc, 7) == '1') {
      val WindowTileMap = if (getBit(lcdc, 6) == '0') {
        (0x9800..0x9BFF).map({ RAM.getByteAt(it) })
      } else {
        (0x9C00..0x9FFF).map({ RAM.getByteAt(it) })
      }
      val showWindow = (getBit(lcdc, 5) == '1')
      val BGandWindowMode = getBit(lcdc, 4)
      val BGandWindowTileData = if (BGandWindowMode == '0') {
        (0x8800..0x97FF).map({ RAM.getByteAt(it) })
      } else {
        (0x8000..0x8FFF).map({ RAM.getByteAt(it) })
      }
      val BGTileMap = if (getBit(lcdc, 3) == '0') {
        (0x9800..0x9BFF).map({ RAM.getByteAt(it) })
      } else {
        (0x9C00..0x9FFF).map({ RAM.getByteAt(it) })
      }
      val showBG = getBit(lcdc, 0) == '1'
      val spriteSize = getBit(lcdc, 2)
      val showSprites = (getBit(lcdc, 1) == '1')
      val stat = RAM.getByteAt(0xFF41)
      val scy = RAM.getByteAt(0xFF42)
      val scx = RAM.getByteAt(0xFF43)
      val ly = RAM.getByteAt(0xFF44)
      val lyc = RAM.getByteAt(0xFF45)
      val bgp = byteToPalette(RAM.getByteAt(0xFF47))
      val obp0 = byteToPalette(RAM.getByteAt(0xFF48))
      val obp1 = byteToPalette(RAM.getByteAt(0xFF49))
      val wy = RAM.getByteAt(0xFF4A)
      val wx = RAM.getByteAt(0xFF4B)
      val oam: List<Short> = (0xFE00..0xFE9F).map({ RAM.getByteAt(it) })
      val bg = BGTileMap.flatMap({ tileNr ->
        if (BGandWindowMode == '1') {
          (0..15 step 2).map({ i ->
            byteToColorData(
                BGandWindowTileData[tileNr + i],
                BGandWindowTileData[tileNr + i + 1],
                bgp)
          })
        } else {
          (0..15 step 2).map({ i ->
            byteToColorData(
                BGandWindowTileData[tileNr + i + 128],
                BGandWindowTileData[tileNr + i + 129],
                bgp)
          })
        }
      })
      val sprites = (0xFE00..0xFE9F step 4).map({ 
        {}
      })
    }
  }
}
