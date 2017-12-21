package de.prt.gb

object RAM {
  val ram = Array(65535, { 0.toShort() })
  fun getByteAt(addr: Int): Short {
    return ram[addr]
  }
  fun load(addr: Int, dat: List<Short>) {
    dat.forEachIndexed({ i: Int, d: Short -> ram[addr + i] = d })
  }
  fun setByteAt(addr: Int, value: Short, hardware: Boolean = false) {
    ram[addr] = value
  }
  fun setWordAt(addr: Int, value: Int) {
    val h = value and 0xFF00
    val l = value and 0xFF
    ram[addr] = h.toShort()
    ram[addr + 1] = l.toShort()
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
