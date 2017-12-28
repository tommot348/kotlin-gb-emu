package de.prt.gb
private data class Sprite(
  val x: Int,
  val y: Int,
  val flipX: Boolean,
  val flipY: Boolean,
  val above: Boolean,
  val palette: List<Int>,
  val dat: List<Int>)
object GPU {
  val stateClocks = mapOf(0 to 201,
                          1 to 4560,
                          2 to 77,
                          3 to 169)
  var state = 2
  var lastClock = 0
  var clocksTillNextState: Int = stateClocks[state] ?: 0

  private fun getBit(of: Short, nr: Int): Char {
    return of.toString(2).padStart(8, '0').get(7 - nr)
  }
  private fun byteToPalette(input: Short): List<Int> {
    val ps = input.toString(2).padStart(8, '0')
    return listOf(ps.substring(6).toInt(2),
                  ps.substring(4, 6).toInt(2),
                  ps.substring(2, 4).toInt(2),
                  ps.substring(0, 2).toInt(2))
  }
  private fun byteToPatternData(least: Short, most: Short): List<Int> {
    val binaryLeast = least.toString(2).padStart(8, '0')
    val binaryMost = most.toString(2).padStart(8, '0')
    return (7 downTo 0).map({ "${binaryMost[it]}${binaryLeast[it]}".toInt(2) })
  }
  private fun getBGData(
      BGTileMap: List<Short>,
      BGandWindowTileData: List<Short>,
      BGandWindowMode: Char
  ): List<Int> {
    return BGTileMap.flatMap({ tileNr ->
      if (BGandWindowMode == '1') {
        (0..15 step 2).flatMap({ i ->
          byteToPatternData(
              BGandWindowTileData[tileNr + i],
              BGandWindowTileData[tileNr + i + 1])
        })
      } else {
        (0..15 step 2).flatMap({ i ->
          byteToPatternData(
              BGandWindowTileData[tileNr + i + 128],
              BGandWindowTileData[tileNr + i + 129])
        })
      }
    })
  }
  private fun getSpriteList(
      tileData: List<Short>,
      obp0: List<Int>,
      obp1: List<Int>,
      spriteSize: Char
  ): List<Map<String, Any>> {
    return (0xFE00..0xFE9F step 4).map({
      val attrib = RAM.getByteAt(it + 3)
      val palette =
        if (getBit(attrib, 5) == '0') {
          obp0
        } else {
          obp1
        }
      val tileNr = RAM.getByteAt(it + 2)
      val dat = if (spriteSize == '0') {
        (0..15 step 2).flatMap({ i ->
          byteToPatternData(
            tileData[tileNr + i],
            tileData[tileNr + i + 1])
        })
      } else {
        val upper = (tileNr.toInt() and 0xFE)
        val lower = (tileNr.toInt() or 0x1)
        listOf(upper, lower).flatMap({ curr ->
          (0..15 step 2).flatMap({ i ->
            byteToPatternData(
              tileData[curr + i],
              tileData[curr + i + 1])
          })
        })
      }
      Sprite(
        y = RAM.getByteAt(it),
        x = RAM.getByteAt(it + 1),
        above = (getBit(attrib, 7) == '0'),
        flipY = (getBit(attrib, 6) == '1'),
        flipX = (getBit(attrib, 5) == '1'),
        palette = palette,
        dat = dat
      )
    })
  }
  var bg: List<Int> = listOf(0)
  var window: List<Int> = listOf(0)
  var sprites: List<Sprite> = listOf(mapOf("" to 0))
  var lines: List<List<Int>>? = null

  private fun getLine(y: Int, lcdc: Short): List<Int> {
    val scy = RAM.getByteAt(0xFF42)
    val scx = RAM.getByteAt(0xFF43)
    val bgp = byteToPalette(RAM.getByteAt(0xFF47))
    val wy = RAM.getByteAt(0xFF4A)
    val wx = RAM.getByteAt(0xFF4B)
    val showWindow = (getBit(lcdc, 5) == '1')
    val showBG = getBit(lcdc, 0) == '1'
    val showSprites = (getBit(lcdc, 1) == '1')
    // step get line from background
    val line = bg.slice((y * 160)..(y + 160 + 160))
    // possibly overwrite with window
    val withWindow = line.mapIndexed({ i, curr ->
      if (scy <= y) {
        if (scx <= i) {
          window[(y - scy) * 160 + (i - scx)]
        } else {
          curr
        }
      } else {
        curr
      }
    })
    // possibly overwrite with sprite
    return withWindow.mapIndexed({ i, curr ->
      val spriteSize = if (getBit(lcdc, 2) == '0') 8 else 16
      val spritesOnLine = sprites.filter({ it.y >= y && it.y < y + spriteSize })
    })
  }

  fun tick(clock: Int): List<Int> {
    val lcdc = RAM.getByteAt(0xFF40)
    val stat = RAM.getByteAt(0xFF41)
    var statStr = stat.toString(2).padStart(8, '0')
    val interruptFlags = RAM.getByteAt(0xFF0F)
    if (getBit(lcdc, 7) == '1') {
      clocksTillNextState -= (clock - lastClock)
      val ly = RAM.getByteAt(0xFF44)
      val lyc = RAM.getByteAt(0xFF45)
      if (ly == lyc) {
        statStr = statStr.substring(0, 5) + "1" + statStr.substring(6)
        if (getBit(stat, 6) == '1') {
          RAM.setByteAt(
              0xFF0F,
              (interruptFlags.toInt() or 0b10).toShort(),
              true)
        }
      }

      if (clocksTillNextState <= 0) {
        when (state) {
          0 -> if (ly < 144) {
            RAM.setByteAt(0xFF44, (ly + 1).toShort(), true)
            if (getBit(stat, 5) == '1') {
              RAM.setByteAt(
                  0xFF0F,
                  (interruptFlags.toInt() or 0b10).toShort(),
                  true)
            }
            //add line

            state = 2
          } else {
            RAM.setByteAt(0xFF44, 0.toShort(), true)
            RAM.setByteAt(
                0xFF0F,
                (interruptFlags.toInt() or 0b00000001).toShort(),
                true)
            if (getBit(stat, 4) == '1') {
              RAM.setByteAt(
                  0xFF0F,
                  (interruptFlags.toInt() or 0b11).toShort(),
                  true)
            }
            //send to display
            //clear
            state = 1
          }
          1 -> {
            if (getBit(stat, 5) == '1') {
              RAM.setByteAt(
                  0xFF0F,
                  (interruptFlags.toInt() or 0b10).toShort(),
                  true)
            }
            state = 2
          }
          2 -> { state = 3 }
          3 -> {
            if (getBit(stat, 3) == '1') {
              RAM.setByteAt(
                  0xFF0F,
                  (interruptFlags.toInt() or 0b10).toShort(),
                  true)
            }
            val WindowTileMap = if (getBit(lcdc, 6) == '0') {
              (0x9800..0x9BFF).map({ RAM.getByteAt(it) })
            } else {
              (0x9C00..0x9FFF).map({ RAM.getByteAt(it) })
            }
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
            val spriteTileData = (0x8000..0x8FFF).map({ RAM.getByteAt(it) })
            val spriteSize = getBit(lcdc, 2)
            val obp0 = byteToPalette(RAM.getByteAt(0xFF48))
            val obp1 = byteToPalette(RAM.getByteAt(0xFF49))
            bg = getBGData(BGTileMap, BGandWindowTileData, BGandWindowMode)
            window = getBGData(BGTileMap, BGandWindowTileData, BGandWindowMode)
            sprites = getSpriteList(spriteTileData, obp0, obp1, spriteSize)
            state = 0
          }
        }
        clocksTillNextState = stateClocks[state] ?: 0
        statStr = statStr.substring(0, 6) + state.toString(2)
      }
    }
    lastClock = clock
    RAM.setByteAt(0xFF41, (statStr).toShort(2), true)
    return listOf(0)
  }
}
