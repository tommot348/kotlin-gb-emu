package de.prt.gb.hardware.mbc
import de.prt.gb.hardware.RAM

class MBC1 : MemoryBankController {
  private val romBanks: ArrayList<List<Short>> = ArrayList<List<Short>>()
  private val ramBanks: ArrayList<Array<Short>> = ArrayList<Array<Short>>()
  private var mode = 0
  private var currentRomBank = 1
  private var currentRamBank = 0
  private var ramEnabled = true

  override fun load(dat: List<Short>) {
    val romSize = 32 * Math.pow(2.toDouble(), (dat[0x0148].toDouble())).toInt()
    val bankNr = romSize / 16
    val ramSize = when (dat[0x0149].toInt()) {
      0 -> 0
      1 -> 2
      2 -> 8
      3 -> 32
      else -> -1
    }
    (0..bankNr - 1).forEach({ nr ->
      romBanks.add(dat.slice((nr * 0x4000)..(((nr + 1) * 0x4000) - 1)))
      if (nr in listOf(20, 40, 60)) {
        romBanks.add(dat.slice((nr * 0x4000)..(((nr + 1) * 0x4000) - 1)))
      }
    })
    when (ramSize) {
      1 -> ramBanks.add(Array(2048, { 0.toShort() }))
      2 -> ramBanks.add(Array(8192, { 0.toShort() }))
      3 -> {
        ramBanks.add(Array(8192, { 0.toShort() }))
        ramBanks.add(Array(8192, { 0.toShort() }))
        ramBanks.add(Array(8192, { 0.toShort() }))
        ramBanks.add(Array(8192, { 0.toShort() }))
      }
    }
    RAM.load(0x0000, romBanks[0])
    RAM.load(0x4000, romBanks[1])
  }
  override fun setByteAt(addr: Int, value: Short) {
    when (addr) {
      in 0..0x1FFF -> ramEnabled = if (value.toInt() == 0xA) true else false
      in 0x2000..0x3FFF -> {
        val mask = 0b00011111
        val newRomBankNr = (currentRomBank and 0b01100000) or (value.toInt() and mask)
        when (newRomBankNr) {
          in listOf(0, 20, 40, 60) -> switchRomBank(newRomBankNr + 1)
          else -> switchRomBank(newRomBankNr)
        }
      }
      in 0x4000..0x5FFF -> {
        val mask = 0b00000011
        val temp = (value.toInt() and mask) shl 5
        if (mode == 0) {
          val newRomBankNr = (currentRomBank and 0b00011111) or temp
          switchRomBank(newRomBankNr)
        } else {
          switchRamBank(value.toInt() and mask)
        }
      }
      in 0x6000..0x7FFF -> mode = value.toInt()
      in 0xA000..0xBFFF -> if (ramEnabled) {
        RAM.setByteAt(addr, value, true)
        ramBanks[currentRamBank][addr] = value
      }
    }
  }
  override fun switchRomBank(nr: Int) {
    currentRomBank = if (nr > romBanks.size) romBanks.size - 1 else nr
    RAM.load(0x4000, romBanks[currentRomBank])
  }
  override fun switchRamBank(nr: Int) {
    currentRamBank = nr
    RAM.load(0xA000, ramBanks[currentRomBank].toList())
  }
}
