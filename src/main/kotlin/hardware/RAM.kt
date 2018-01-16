package de.prt.gb.hardware

internal object CARTRIDGE {
  private var name = ""
  private var type = ""
  private var mode = 0
  private val romBanks: ArrayList<List<Short>> = ArrayList<List<Short>>()
  private val ramBanks: ArrayList<Array<Short>> = ArrayList<Array<Short>>()
  private var currentRomBank = 1
  private var currentRamBank = 0
  private var ramEnabled = true

  fun load(dat: List<Short>) {
    val title = dat.slice(0x0134..0x0143).map({ it.toChar() }).toString()
    name = title
    type = when (dat[0x0147].toInt()) {
      0 -> "ROM"
      1 -> "MBC1"
      2 -> "MBC1+RAM"
      3 -> "MBC1+RAM+BATTERY"
      // 5 -> "MBC2"
      // 6 -> "MBC2+BATTERY"
      8 -> "ROM+RAM"
      9 -> "ROM+RAM+BATTERY"
      // 0xF -> "MBC3+TIMER+BATTERY"
      // 0x10 -> "MBC3+TIMER+RAM+BATTERY"
      // 0x11 -> "MBC3"
      // 0x12 -> "MBC3+RAM"
      // 0x13 -> "MBC3+RAM+BATTERY"
      else -> "unsupported"
    }
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
    RAM.load(0, romBanks[0])
    RAM.load(0x4000, romBanks[1])
  }
  fun switchRomBank(nr: Int) {
    currentRomBank = if (nr > romBanks.size) romBanks.size - 1 else nr
    RAM.load(0x4000, romBanks[currentRomBank])
  }
  fun switchRamBank(nr: Int) {
    currentRamBank = nr
  }

  fun setByteAt(addr: Int, value: Short) {
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
      in 0xA000..0xBFFF -> if (ramEnabled) ramBanks[currentRamBank][addr] = value
    }
  }
}
internal object BIOS {
  private val bios = ArrayList<Short>()
  fun load(dat: List<Short>) {
    bios.addAll(dat)
  }
  fun getByteAt(addr: Int): Short {
    return bios[addr]
  }
}

internal object RAM {
  private val ram = Array(65536, { 0.toShort() })
  var biosMapped = true
  fun getByteAt(addr: Int): Short {
    return when (addr) {
      in 0..0xFF -> if (biosMapped) BIOS.getByteAt(addr) else ram[addr]
      in 0xE000..0xFDFF -> ram[addr - 0x2000]
      0xFF00 -> {
        val mode = (0b00110000 and ram[0xFF00].toInt()) shr 4
        val state = Input.getState(mode)
        state
      }
      else -> ram[addr]
    }
  }
  fun load(addr: Int, dat: List<Short>) {
    dat.forEachIndexed({ i: Int, d: Short -> ram[addr + i] = d })
  }
  fun setByteAt(addr: Int, value: Short, hardware: Boolean = false) {
    if (hardware) {
      ram[addr] = value
    } else {
      when (addr) {
        in 0..0x7FFF -> CARTRIDGE.setByteAt(addr, value)
        0xFF04 -> ram[0xFF04] = 0
        0xFF44 -> ram[0xFF44] = 0
        0xFF46 -> ((value * 0x100)..((value * 0x100) + 0x9F)).forEachIndexed({ i, curr ->
          ram[0xFE00 + i] = ram[curr]
        })
        0xFF50 -> biosMapped = false
        else -> ram[addr] = value
      }
    }
  }
  fun setWordAt(addr: Int, value: Int) {
    val h = value and 0xFF00
    val l = value and 0xFF
    setByteAt(addr, h.toShort())
    setByteAt(addr + 1, l.toShort())
  }
  override fun toString() = ram.map({ it.toString(16).padStart(2, '0') }).reduceIndexed({
    i, prev, curr ->
      if ((i!= 0) && ((i + 1).rem(16) == 0)) {
        prev+'\n'+curr
      } else {
        prev + curr
      }
    })
}
