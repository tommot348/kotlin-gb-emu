package de.prt.gb

class CPU() {
  private var A: Short = 0
  private var B: Short = 0
  private var C: Short = 0
  private var D: Short = 0
  private var E: Short = 0
  private var F: Short = 0
  private var H: Short = 0
  private var L: Short = 0
  private var SP: Int = 0
  private var PC: Int = 0

  private val ram = RAM()

  private fun splitHighByteLowByte(x: Int): Pair<Short, Short> {
    val b = x and 0b0000000011111111
    val a = x / 256
    return Pair(a.toShort(), b.toShort())
  }
  private fun joinHighByteLowByte(a: Short, b: Short): Int {
      return ((a * 256) + b).toInt()
  }

  private fun getCarry(): Char = F.toString(2).padStart(8, '0').get(3)
  private fun setCarry(c: Char) {
    when (c) {
      '0' -> F = (F.toInt() and 0b11100000).toShort()
      '1' -> F = (F.toInt() or 0b00010000).toShort()
    }
  }
  private fun getHalfCarry(): Char = F.toString(2).padStart(8, '0').get(2)
  private fun setHalfCarry(c: Char) {
    when (c) {
      '0' -> F = (F.toInt() and 0b11010000).toShort()
      '1' -> F = (F.toInt() or 0b00100000).toShort()
    }
  }
  private fun getSubstract(): Char = F.toString(2).padStart(8, '0').get(1)
  private fun setSubstract(c: Char) {
    when (c) {
      '0' -> F = (F.toInt() and 0b10110000).toShort()
      '1' -> F = (F.toInt() or 0b01000000).toShort()
    }
  }
  private fun getZero(): Char = F.toString(2).padStart(8, '0').get(0)
  private fun setZero(c: Char) {
    when (c) {
      '0' -> F = (F.toInt() and 0b01110000).toShort()
      '1' -> F = (F.toInt() or 0b10000000).toShort()
    }
  }

  private fun rl(A: Short): Short {
    val a = A.toString(2).padStart(8, '0')
    val c = getCarry()
    setCarry(a.first())
    return (a.substring(1) + c).toShort(2)
  }
  private fun rlc(A: Short): Short {
    val a = A.toString(2).padStart(8, '0')
    setCarry(a.first())
    return (a.substring(1) + a.first()).toShort(2)
  }

  private var BC: Int
    get() = joinHighByteLowByte(B, C)
    set(x: Int) {
      val (nb, nc) = splitHighByteLowByte(x)
      B = nb
      C = nc
    }

  private var DE: Int
    get() = joinHighByteLowByte(D, E)
    set(x: Int) {
      val (nb, nc) = splitHighByteLowByte(x)
      D = nb
      E = nc
    }

  private var HL: Int
    get() = joinHighByteLowByte(H, L)
    set(x: Int) {
      val (nb, nc) = splitHighByteLowByte(x)
      H = nb
      L = nc
    }
  private fun getNextWord(): Int {
    val a = ram.getByteAt(PC)
    ++PC
    val b = ram.getByteAt(PC)
    ++PC
    return joinHighByteLowByte(a, b)
  }
  private val stack: HashMap<Int, Int> = HashMap()
  private val opcodes: Map<Int, Function<Unit>> = mapOf(
    0x00 to { println("NOP") }, //NOP
    0x01 to { BC = getNextWord() },
    0x02 to { ram.setByteAt(BC, A) }, //ld (BC), A
    0x03 to { BC = BC + 1 // inc BC
      if (BC == 0) {
        setZero('1')
      }
    },
    0x04 to { B = (B + 1).toShort() }, //inc B
    0x05 to { B = (B - 1).toShort() }, //dec B
    0x06 to { B = ram.getByteAt(PC++) }, //ld B, d8
    0x07 to { A = rlc(A) }, // RLCA
    0x08 to { val a = getNextWord() //ld (a16), SP
      ram.setWordAt(a, SP)
    }
  )
  fun tick() {
  }
}
