package de.prt.gb.hardware
private data class Sprite(
  val x: Short,
  val y: Short,
  val flipX: Boolean,
  val flipY: Boolean,
  val above: Boolean,
  val palette: List<Int>,
  val dat: List<Int>)
object GPU {
  internal val display = Display()
  internal val stateClocks = mapOf(0 to 201,
                          1 to 4560,
                          2 to 77,
                          3 to 169)
  internal var state = 1
  internal var lastClock = 0
  internal var clocksTillNextState: Int = stateClocks[state] ?: 0

  init {
    display.showWindow()
  }

  internal fun getBit(of: Short, nr: Int): Char {
    return of.toString(2).padStart(8, '0').get(7 - nr)
  }
  internal fun byteToPalette(input: Short): List<Int> {
    val ps = input.toString(2).padStart(8, '0')
    return listOf(("${ps[7]}${ps[6]}".toInt(2)),
                  ("${ps[5]}${ps[4]}".toInt(2)),
                  ("${ps[3]}${ps[2]}".toInt(2)),
                  ("${ps[1]}${ps[0]}".toInt(2)))
  }
  internal fun byteToPatternData(least: Short, most: Short): List<Int> {
    val binaryLeast = least.toString(2).padStart(8, '0')
    val binaryMost = most.toString(2).padStart(8, '0')
    val dat = (0..7).map({ "${binaryMost[it]}${binaryLeast[it]}".toInt(2) })
    return dat
  }
  internal fun getBGData(
      tileMap: List<List<Short>>,
      tileData: List<List<List<Int>>>,
      mode: Char
  ): List<List<Int>> =
    tileMap.flatMap({ line ->
      val tileLine = line.map({
        if (mode == '0') {
          tileData[it.toInt() + 128]
        } else {
          tileData[it.toInt()]
        }
      })
      (0..7).map({ nr ->
        tileLine.fold(ArrayList<Int>(), { prev, curr: List<List<Int>> ->
          prev.addAll(curr[nr])
          prev
        })
      })
    })

  private fun getSpriteList(
      tileData: List<Short>,
      obp0: List<Int>,
      obp1: List<Int>,
      spriteSize: Char
  ): List<Sprite> {
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

  internal fun getTileNrs(addr: Int, signed: Boolean): List<List<Short>> =
    (0..31).map({ y ->
      (0..31).map({ x ->
        val a = RAM.getByteAt(addr + (y * 32) + x)
        if (signed) {
          a.toByte().toShort()
        } else {
          a
        }
      })
    })

  internal fun getTiles(addr: Int): List<List<List<Int>>> =
    (0..255).map({ tileNr ->
      (0..15 step 2).map({ part ->
        byteToPatternData(
            RAM.getByteAt(addr + (tileNr * 16) + part),
            RAM.getByteAt(addr + (tileNr * 16) + part + 1))
      })
    })

  internal var bg: ArrayList<List<Int>> = ArrayList<List<Int>>()
  internal var window: List<List<Int>> = ArrayList<List<Int>>()
  private var sprites: List<Sprite>? = null

  internal fun getLine(y: Short, lcdc: Short): List<Int> {
    val scy = RAM.getByteAt(0xFF42)
    val scx = RAM.getByteAt(0xFF43)
    val bgp = byteToPalette(RAM.getByteAt(0xFF47))
    val wy = RAM.getByteAt(0xFF4A)
    val wx = RAM.getByteAt(0xFF4B)
    val showWindow = (getBit(lcdc, 5) == '1')
    val showBG = getBit(lcdc, 0) == '1'
    val showSprites = (getBit(lcdc, 1) == '1')
    // step get line from background
    val realY = (y + scy) % 255
    val tempLine = if (showBG) bg[realY] else (0..255).map({ 0 })
    val line = tempLine.mapIndexed({ i, _ ->
      val realX = (i + scx) % 255
      tempLine[realX]
    }).slice(0..159)
    // possibly overwrite with window
    val withWindow = if (showWindow && y >= wy) {
      line.mapIndexed({ x, column ->
        if (x >= wx) {
            window[y.toInt()][x]
          } else {
            column
          }
        })
      } else {
        line
      }
    // possibly overwrite with sprite
    return withWindow.map({ bgp[it] })
    /*return withWindow.mapIndexed({ i, curr ->
      val spriteSize = if (getBit(lcdc, 2) == '0') 8 else 16
      val spritesOnLine = sprites?.filter({ it.y >= y && it.y < y + spriteSize })
      if (showSprites) {
        if (spritesOnLine != null && spritesOnLine.size > 0) {
          val sprite = spritesOnLine.filter({ it.x >= i && it.x < i + 8 }).firstOrNull()
          if (sprite != null) {
            if (sprite.above) {
              sprite.palette[sprite.dat[(y - sprite.y) + (i - sprite.x)]]
            } else {
              if (!(withWindow[i] in 1..3)) {
                sprite.palette[sprite.dat[(y - sprite.y) + (i - sprite.x)]]
              } else {
                bgp[curr]
              }
            }
          }
        }
      }
    })*/
  }
  val lines = ArrayList<List<Int>>()
  fun tick(clock: Int) {
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
            lines.add(getLine(ly, lcdc))
            state = 2
          } else {
            RAM.setByteAt(0xFF44, (ly + 1).toShort(), true)
            if (ly.toInt() == 144) {
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
            }
            if (ly > 153) {
              RAM.setByteAt(0xFF44, 0.toShort(), true)
              //clear
              display.update(lines)
              lines.removeAll({ true })
              state = 1
            } else {
              state = 2
            }
          }
          1 -> {
            val BGandWindowMode = getBit(lcdc, 4)
            val WindowTileMap = if (getBit(lcdc, 6) == '0') {
              getTileNrs(0x9800, BGandWindowMode == '0')
            } else {
              getTileNrs(0x9C00, BGandWindowMode == '0')
            }
            val BGandWindowTileData = if (BGandWindowMode == '0') {
              getTiles(0x8800)
            } else {
              getTiles(0x8000)
            }
            val BGTileMap = if (getBit(lcdc, 3) == '0') {
              getTileNrs(0x9800, BGandWindowMode == '0')
            } else {
              getTileNrs(0x9C00, BGandWindowMode == '0')
            }
            val spriteTileData = (0x8000..0x8FFF).map({ RAM.getByteAt(it) })
            val spriteSize = getBit(lcdc, 2)
            val obp0 = byteToPalette(RAM.getByteAt(0xFF48))
            val obp1 = byteToPalette(RAM.getByteAt(0xFF49))
            bg.removeAll({ true })
            bg.addAll(getBGData(BGTileMap, BGandWindowTileData, BGandWindowMode))
            window = getBGData(WindowTileMap, BGandWindowTileData, BGandWindowMode)
            sprites = getSpriteList(spriteTileData, obp0, obp1, spriteSize)
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
            state = 0
          }
        }
        clocksTillNextState = stateClocks[state] ?: 0
        statStr = statStr.substring(0, 6) + state.toString(2)
      }
    }
    lastClock = clock
    RAM.setByteAt(0xFF41, (statStr).toShort(2), true)
  }
}
