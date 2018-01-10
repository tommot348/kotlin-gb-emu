package de.prt.gb
import kotlin.system.exitProcess
object CPU {
  private var A: Short = 0
  private var B: Short = 0
  private var C: Short = 0
  private var D: Short = 0
  private var E: Short = 0
  private var F: Short = 0
  private var H: Short = 0
  private var L: Short = 0
  private var PCh: Short = 0
  private var PCl: Short = 0
  private var SP: Int = 0
  private var running = true
  private var interrupts = true
  private var prefix = false
  private var time = 0

  fun setIP(p: Int) {
    PC = p
  }

  fun splitHighByteLowByte(x: Int): Pair<Short, Short> {
    val b = x and 0b0000000011111111
    val a = x / 256
    return Pair(a.toShort(), b.toShort())
  }
  fun joinHighByteLowByte(a: Short, b: Short): Int {
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
  fun Short.INC(): Short {
      setZero('0')
      setHalfCarry('0')
      setSubstract('0')
      val fourth = this.toString(2).padStart(8, '0').get(4)
      var rthis = (this + 1).toShort()
      if (rthis > 255.toShort() || rthis == 0.toShort()) {
        rthis = 0.toShort()
        setZero('1')
      }
      val fourthAfter = rthis.toString(2).padStart(8, '0').get(4)
      if (fourth != fourthAfter) {
        setHalfCarry('1')
      }
      return rthis
  }
  fun Short.DEC(): Short {
    val fourth = this.toString(2).padStart(8, '0').get(4)
    var rthis = (this - 1).toShort()
    setZero('0')
    setHalfCarry('0')
    if (rthis < (0.toShort())) {
      rthis = 255.toShort()
    }
    if (rthis == 0.toShort()) {
      setZero('1')
    }
    val fourthAfter = rthis.toString(2).padStart(8, '0').get(4)
    if (fourthAfter != fourth) {
      setHalfCarry('1')
    }
    setSubstract('1')
    return rthis
  }
  fun Int.INC(): Int {
    var rthis = this + 1
    if (rthis > 0xFFFF) {
      rthis = 0
    }
    return rthis
  }
  fun Int.DEC(): Int {
    var rthis = this - 1
    if (rthis < 0) {
      rthis = 0xFFFF
    }
    return rthis
  }

  infix fun Int.ADD(a: Int): Int {
      setHalfCarry('0')
      setSubstract('0')
      setCarry('0')
      val eighthHL = this.toString(2).padStart(16, '0').get(8)
      val eighthBC = a.toString(2).padStart(16, '0').get(8)
      var rthis = this + a
      val eighthAfter = rthis.toString(2).padStart(16, '0').get(8)
      if (rthis > 0xFFFF) {
        rthis = rthis - 0xFFFF
        setCarry('1')
      }
      when ("$eighthHL$eighthBC$eighthAfter") {
        "100", "010", "111", "110" -> setHalfCarry('1')
      }
      return rthis
  }
  infix fun Short.ADD(a: Short): Short {
    setHalfCarry('0')
    setSubstract('0')
    setCarry('0')
    setZero('0')
    val thirdHL = this.toString(2).padStart(8, '0').get(3)
    val thirdBC = a.toString(2).padStart(8, '0').get(3)
    var rthis = (this + a).toShort()
    val thirdAfter = rthis.toString(2).padStart(8, '0').get(3)
    if (rthis > 0xFF.toShort()) {
      rthis = (rthis - 0xFF.toShort()).toShort()
      setCarry('1')
    }
    when ("$thirdHL$thirdBC$thirdAfter") {
      "100", "010", "111", "110" -> setHalfCarry('1')
    }
    if (this == 0.toShort()) {
      setZero('1')
    }
    return rthis
  }
  infix fun Short.ADC(a: Short): Short {
    val c = if (getCarry() == '1') 1 else 0
    setHalfCarry('0')
    setSubstract('0')
    setCarry('0')
    setZero('0')
    val thirdHL = this.toString(2).padStart(8, '0').get(3)
    val thirdBC = a.toString(2).padStart(8, '0').get(3)
    var rthis = (this + a + c).toShort()
    val thirdAfter = rthis.toString(2).padStart(8, '0').get(3)
    if (rthis > 0xFF.toShort()) {
      rthis = (rthis - 0xFF.toShort()).toShort()
      setCarry('1')
    }
    when ("$thirdHL$thirdBC$thirdAfter") {
      "100", "010", "111", "110" -> setHalfCarry('1')
    }
    if (this == 0.toShort()) {
      setZero('1')
    }
    return rthis
  }
  infix fun Short.SUB(a: Short): Short {
    setHalfCarry('0')
    setSubstract('0')
    setCarry('0')
    setZero('1')
    val thirdHL = this.toString(2).padStart(8, '0').get(3)
    val thirdBC = a.toString(2).padStart(8, '0').get(3)
    var rthis = (this - a).toShort()
    val thirdAfter = rthis.toString(2).padStart(8, '0').get(3)
    if (rthis < 0.toShort()) {
      rthis = (0xff.toShort() + rthis).toShort()
      setCarry('1')
    }
    when ("$thirdHL$thirdBC$thirdAfter") {
      "100", "010", "111", "110" -> setHalfCarry('1')
    }
    if (this == 0.toShort()) {
      setZero('1')
    }
    return rthis
  }
  infix fun Short.SBC(a: Short): Short {
    val c = if (getCarry() == '1') 1 else 0
    setHalfCarry('0')
    setSubstract('0')
    setCarry('0')
    setZero('1')
    val thirdHL = this.toString(2).padStart(8, '0').get(3)
    val thirdBC = a.toString(2).padStart(8, '0').get(3)
    var rthis = (this - a - c).toShort()
    val thirdAfter = rthis.toString(2).padStart(8, '0').get(3)
    if (rthis < 0.toShort()) {
      rthis = (0xFF.toShort() - rthis).toShort()
      setCarry('1')
    }
    when ("$thirdHL$thirdBC$thirdAfter") {
      "100", "010", "111", "110" -> setHalfCarry('1')
    }
    if (this == 0.toShort()) {
      setZero('1')
    }
    return rthis
  }
  infix fun Short.AND(a: Short): Short {
    setZero('0')
    setSubstract('0')
    setHalfCarry('1')
    setCarry('0')
    val ret = (this.toInt() and a.toInt()).toShort()
    if (ret == 0.toShort()) {
      setZero('1')
    }
    return ret
  }
  infix fun Short.XOR(a: Short): Short {
    setZero('0')
    setSubstract('0')
    setHalfCarry('0')
    setCarry('0')
    val ret = (this.toInt() xor a.toInt()).toShort()
    if (ret == 0.toShort()) {
      setZero('1')
    }
    return ret
  }
  infix fun Short.OR(a: Short): Short {
    setZero('0')
    setSubstract('0')
    setHalfCarry('0')
    setCarry('0')
    val ret = (this.toInt() or a.toInt()).toShort()
    if (ret == 0.toShort()) {
      setZero('1')
    }
    return ret
  }
  private fun rl(a: Short): Short {
    setZero('0')
    setHalfCarry('0')
    setSubstract('0')
    val an = a.toString(2).padStart(8, '0')
    val c = getCarry()
    setCarry(an.first())
    val ret = (an.substring(1) + c).toShort(2)
    if (prefix) {
      if (ret.toInt() == 0) {
        setZero('1')
      }
    }
    return ret
  }
  private fun rlc(a: Short): Short {
    setZero('0')
    setHalfCarry('0')
    setSubstract('0')
    val an = a.toString(2).padStart(8, '0')
    setCarry(an.first())
    val ret = (an.substring(1) + an.first()).toShort(2)
    if (prefix) {
      if (ret.toInt() == 0) {
        setZero('1')
      }
    }
    return ret
  }

  private fun rr(a: Short): Short {
    setZero('0')
    setHalfCarry('0')
    setSubstract('0')
    val an = a.toString(2).padStart(8, '0')
    val c = getCarry()
    setCarry(an.last())
    val ret = (c + an.substring(1)).toShort(2)
    if (prefix) {
      if (ret.toInt() == 0) {
        setZero('1')
      }
    }
    return ret
  }
  private fun rrc(a: Short): Short {
    setZero('0')
    setHalfCarry('0')
    setSubstract('0')
    val an = a.toString(2).padStart(8, '0')
    setCarry(an.last())
    val ret = (an.last() + an.substring(0, an.length - 1)).toShort(2)
    if (prefix) {
      if (ret.toInt() == 0) {
        setZero('1')
      }
    }
    return ret
  }

  private fun sla(a: Short): Short {
    setZero('0')
    setHalfCarry('0')
    setSubstract('0')
    val an = a.toString(2).padStart(8, '0')
    setCarry(an.first())
    val ret = (an.substring(1) + "0").toShort(2)
    if (ret.toInt() == 0) {
      setZero('1')
    }
    return ret
  }
  private fun sra(a: Short): Short {
    setZero('0')
    setHalfCarry('0')
    setSubstract('0')
    val an = a.toString(2).padStart(8, '0')
    setCarry(an.last())
    val ret = (an.first() + an.substring(0, an.length - 1)).toShort(2)
    if (ret.toInt() == 0) {
      setZero('1')
    }
    return ret
  }
  private fun srl(a: Short): Short {
    setZero('0')
    setHalfCarry('0')
    setSubstract('0')
    val an = a.toString(2).padStart(8, '0')
    setCarry(an.last())
    val ret = ('0' + an.substring(0, an.length - 1)).toShort(2)
    if (ret.toInt() == 0) {
      setZero('1')
    }
    return ret
  }

  private fun swap(a: Short): Short {
    setZero('0')
    setHalfCarry('0')
    setSubstract('0')
    setCarry('0')
    val an = a.toString(2).padStart(8, '0')
    val ret = (an.substring(4) + an.substring(0, 4)).toShort(2)
    if (ret.toInt() == 0) {
      setZero('1')
    }
    return ret
  }

  fun BIT(a: Short, c: Int): Char {
    setHalfCarry('1')
    setSubstract('0')
    val an = a.toString(2).padStart(8, '0').get(7 - c)
    val ret = if (an == '1') '0' else '1'
    return ret
  }
  private fun SET(a: Short, c: Int): Short {
    val an = a.toString(2).padStart(8, '0')
    val ret = an.substring(0, 7 - c) + '1' + an.substring(c)
    return ret.toShort(2)
  }
  private fun RES(a: Short, c: Int): Short {
    val an = a.toString(2).padStart(8, '0')
    val ret = an.substring(0, 7 - c) + '1' + an.substring(c)
    return ret.toShort(2)
  }

  var PC: Int
    get() = joinHighByteLowByte(PCh, PCl)
    set(x: Int) {
      val (nb, nc) = splitHighByteLowByte(x)
      PCh = nb
      PCl = nc
    }
  private var AF: Int
    get() = joinHighByteLowByte(A, F)
    set(x: Int) {
      val (nb, nc) = splitHighByteLowByte(x)
      A = nb
      F = nc
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
    val a = RAM.getByteAt(PC)
    ++PC
    val b = RAM.getByteAt(PC)
    ++PC
    return joinHighByteLowByte(b, a)
  }
  private val opcodes: Map<Int, ()->Int> = mapOf(
    0x00 to { 4 }, //NOP
    0x01 to { BC = getNextWord(); 12 },
    0x02 to { RAM.setByteAt(BC, A); 8 }, //ld (BC), A
    0x03 to { BC = BC.INC(); 8 }, //inc BC
    0x04 to { B = B.INC(); 4 }, //inc B
    0x05 to { B = B.DEC(); 4 }, //dec B
    0x06 to { B = RAM.getByteAt(PC++); 8 }, //ld B, d8
    0x07 to { A = rlc(A); 4 }, // RLCA
    0x08 to {
      val a = getNextWord() //ld (a16), SP
      RAM.setWordAt(a, SP)
      20
    },
    0x09 to { HL = (HL ADD BC); 8 }, //ADD HL, BC    
    0x0a to { A = RAM.getByteAt(BC); 8 },
    0x0b to { BC = BC.DEC(); 8 }, //DEC BC
    0x0c to { C = C.INC(); 4 }, //INC C
    0x0d to { C = C.DEC(); 4 }, //DEC C
    0x0e to { C = RAM.getByteAt(PC++); 8 }, //ld C, d8
    0x0f to { A = rrc(A); 4 },
    0x10 to {
      running = false
      exitProcess(0)
    },
    0x11 to { DE = getNextWord(); 12 }, //ld DE, d16
    0x12 to { RAM.setByteAt(DE, A); 8 }, //ld (DE), A
    0x13 to { DE = DE.INC(); 8 }, // inc DE 
    0x14 to { D = D.INC(); 4 }, //inc d
    0x15 to { D = D.DEC(); 4 }, //dec d
    0x16 to { D = RAM.getByteAt(PC++); 8 }, //ld D,d8
    0x17 to { A = rl(A); 4 }, //rla
    0x18 to { PC = PC + RAM.getByteAt(PC).toByte().toInt() + 1; 12 }, //JR r8
    0x19 to { HL = (HL ADD DE); 8 }, // ADD HL,DE
    0x1a to { A = RAM.getByteAt(DE); 8 }, //ld A,(DE)
    0x1b to { DE = DE.DEC(); 8 },
    0x1c to { E = E.INC(); 4 },
    0x1d to { E = E.DEC(); 4 },
    0x1e to { E = RAM.getByteAt(PC++); 8 },
    0x1f to { A = rr(A); 4 },
    0x20 to { //JR NZ,d8
      if (getZero() == '0') {
        PC = PC + RAM.getByteAt(PC).toByte().toInt() + 1
        12
      } else {
        PC++
        8
      }
    },
    0x21 to { HL = getNextWord(); 12 },
    0x22 to {
      RAM.setByteAt(HL, A)
      HL = HL.INC()
      8
    },
    0x23 to { HL = HL.INC(); 8 },
    0x24 to { H = H.INC(); 4 },
    0x25 to { H = H.DEC(); 4 },
    0x26 to { H = RAM.getByteAt(PC++); 8 },
    0x27 to { //daa
      setCarry('0')
      val a = A.toString(2).substring(4).toInt(2)
      if (a > 9 || getHalfCarry() == '1') {
        A = (A + 0x06.toShort()).toShort()
      }
      val b = A.toString(2).substring(0, 4).toInt(2)
      if (b > 9 || getCarry() == '1') {
        A = (A + 0x60.toShort()).toShort()
        setCarry('1')
      }
      setHalfCarry('0')
      if ( A == 0.toShort()) {
        setZero('1')
      }
      if (A > 0xFF.toShort()) {
        A = (A - 0xFF.toShort()).toShort()
      }
      4
    },
    0x28 to { // JP Z, a8
      if (getZero() == '1') {
        PC = PC + RAM.getByteAt(PC).toByte().toInt() + 1
        16
      } else {
        PC++
        12
      }
    },
    0x29 to { HL = (HL ADD HL); 8 },
    0x2a to {
      A = RAM.getByteAt(HL)
      HL = HL.INC()
      8
    },
    0x2b to { HL = HL.DEC(); 8 },
    0x2c to { L = L.INC(); 4 },
    0x2d to { L = L.DEC(); 4 },
    0x2e to { L = RAM.getByteAt(PC++); 8 },
    0x2f to {
      setHalfCarry('1')
      setSubstract('1')
      val a = 0xFFFFFF + A.toInt()
      A = a.inv().toShort()
      4
    },
    0x30 to {
      if (getCarry() == '0') {
        PC = PC + RAM.getByteAt(PC).toByte().toInt() + 1
        12
      } else {
        PC++
        8
      }
    },
    0x31 to { SP = getNextWord(); 12 },
    0x32 to {
      RAM.setByteAt(HL, A)
      HL = HL.DEC()
      8
    },
    0x33 to { SP = SP.INC(); 8 },
    0x34 to { RAM.setByteAt(HL, RAM.getByteAt(HL).INC()); 12 },
    0x35 to { RAM.setByteAt(HL, RAM.getByteAt(HL).DEC()); 12 },
    0x36 to { RAM.setByteAt(HL, RAM.getByteAt(PC++)); 12 },
    0x37 to {
      setCarry('1')
      setSubstract('0')
      setHalfCarry('0')
      4
    },
    0x38 to {
      if (getCarry() == '1') {
        PC = PC + RAM.getByteAt(PC) + 1
        12
      } else {
        PC++
        8
      }
    },
    0x39 to { HL = (HL ADD SP); 8 },
    0x3a to {
      A = RAM.getByteAt(HL)
      HL = HL.DEC()
      8
    },
    0x3b to { SP = SP.DEC(); 8 },
    0x3c to { A = A.INC(); 4 },
    0x3d to { A = A.DEC(); 4 },
    0x3e to { A = RAM.getByteAt(PC++); 8 },
    0x3f to {
      setHalfCarry('0')
      setSubstract('0')
      val a = getCarry()
      setCarry('0')
      if ( a == '0' ) {
        setCarry('1')
      }
      4
    },
    0x40 to { B = B; 4 },
    0x41 to { B = C; 4 },
    0x42 to { B = D; 4 },
    0x43 to { B = E; 4 },
    0x44 to { B = H; 4 },
    0x45 to { B = L; 4 },
    0x46 to { B = RAM.getByteAt(HL); 8 },
    0x47 to { B = A; 4 },
    0x48 to { C = B; 4 },
    0x49 to { C = C; 4 },
    0x4a to { C = D; 4 },
    0x4b to { C = E; 4 },
    0x4c to { C = H; 4 },
    0x4d to { C = L; 4 },
    0x4e to { C = RAM.getByteAt(HL); 8 },
    0x4f to { C = A; 4 },
    0x50 to { D = B; 4 },
    0x51 to { D = C; 4 },
    0x52 to { D = D; 4 },
    0x53 to { D = E; 4 },
    0x54 to { D = H; 4 },
    0x55 to { D = L; 4 },
    0x56 to { D = RAM.getByteAt(HL); 8 },
    0x57 to { D = A; 4 },
    0x58 to { E = B; 4 },
    0x59 to { E = C; 4 },
    0x5a to { E = D; 4 },
    0x5b to { E = E; 4 },
    0x5c to { E = H; 4 },
    0x5d to { E = L; 4 },
    0x5e to { E = RAM.getByteAt(HL); 8 },
    0x5f to { E = A; 4 },
    0x60 to { H = B; 4 },
    0x61 to { H = C; 4 },
    0x62 to { H = D; 4 },
    0x63 to { H = E; 4 },
    0x64 to { H = H; 4 },
    0x65 to { H = L; 4 },
    0x66 to { H = RAM.getByteAt(HL); 8 },
    0x67 to { H = A; 4 },
    0x68 to { L = B; 4 },
    0x69 to { L = C; 4 },
    0x6a to { L = D; 4 },
    0x6b to { L = E; 4 },
    0x6c to { L = H; 4 },
    0x6d to { L = L; 4 },
    0x6e to { L = RAM.getByteAt(HL); 8 },
    0x6f to { L = A; 4 },
    0x70 to { RAM.setByteAt(HL, B); 8 },
    0x71 to { RAM.setByteAt(HL, C); 8 },
    0x72 to { RAM.setByteAt(HL, D); 8 },
    0x73 to { RAM.setByteAt(HL, E); 8 },
    0x74 to { RAM.setByteAt(HL, H); 8 },
    0x75 to { RAM.setByteAt(HL, L); 8 },
    0x76 to { running = false; 4 },
    0x77 to { RAM.setByteAt(HL, A); 8 },
    0x78 to { A = B; 4 },
    0x79 to { A = C; 4 },
    0x7a to { A = D; 4 },
    0x7b to { A = E; 4 },
    0x7c to { A = H; 4 },
    0x7d to { A = L; 4 },
    0x7e to { A = RAM.getByteAt(HL); 8 },
    0x7f to { A = A; 4 },
    0x80 to { A = (A ADD B); 4 },
    0x81 to { A = (A ADD C); 4 },
    0x82 to { A = (A ADD D); 4 },
    0x83 to { A = (A ADD E); 4 },
    0x84 to { A = (A ADD H); 4 },
    0x85 to { A = (A ADD L); 4 },
    0x86 to { A = (A ADD RAM.getByteAt(HL)); 8 },
    0x87 to { A = (A ADD A); 4 },
    0x88 to { A = (A ADC B); 4 },
    0x89 to { A = (A ADC C); 4 },
    0x8a to { A = (A ADC D); 4 },
    0x8b to { A = (A ADC E); 4 },
    0x8c to { A = (A ADC H); 4 },
    0x8d to { A = (A ADC L); 4 },
    0x8e to { A = (A ADC RAM.getByteAt(HL)); 8 },
    0x8f to { A = (A ADC A); 4 },
    0x90 to { A = (A SUB B); 4 },
    0x91 to { A = (A SUB C); 4 },
    0x92 to { A = (A SUB D); 4 },
    0x93 to { A = (A SUB E); 4 },
    0x94 to { A = (A SUB H); 4 },
    0x95 to { A = (A SUB L); 4 },
    0x96 to { A = (A SUB RAM.getByteAt(HL)); 8 },
    0x97 to { A = (A SUB A); 4 },
    0x98 to { A = (A SBC B); 4 },
    0x99 to { A = (A SBC C); 4 },
    0x9a to { A = (A SBC D); 4 },
    0x9b to { A = (A SBC E); 4 },
    0x9c to { A = (A SBC H); 4 },
    0x9d to { A = (A SBC L); 4 },
    0x9e to { A = (A SBC RAM.getByteAt(HL)); 8 },
    0x9f to { A = (A SBC A); 4 },
    0xa0 to { A = (A AND B); 4 },
    0xa1 to { A = (A AND C); 4 },
    0xa2 to { A = (A AND D); 4 },
    0xa3 to { A = (A AND E); 4 },
    0xa4 to { A = (A AND H); 4 },
    0xa5 to { A = (A AND L); 4 },
    0xa6 to { A = (A AND RAM.getByteAt(HL)); 8 },
    0xa7 to { A = (A AND A); 4 },
    0xa8 to { A = (A XOR B); 4 },
    0xa9 to { A = (A XOR C); 4 },
    0xaa to { A = (A XOR D); 4 },
    0xab to { A = (A XOR E); 4 },
    0xac to { A = (A XOR H); 4 },
    0xad to { A = (A XOR L); 4 },
    0xae to { A = (A XOR RAM.getByteAt(HL)); 8 },
    0xaf to { A = (A XOR A); 4 },
    0xb0 to { A = (A OR B); 4 },
    0xb1 to { A = (A OR C); 4 },
    0xb2 to { A = (A OR D); 4 },
    0xb3 to { A = (A OR E); 4 },
    0xb4 to { A = (A OR H); 4 },
    0xb5 to { A = (A OR L); 4 },
    0xb6 to { A = (A OR RAM.getByteAt(HL)); 8 },
    0xb7 to { A = (A OR A); 4 },
    0xb8 to { (A SUB B); 4 },
    0xb9 to { (A SUB C); 4 },
    0xba to { (A SUB D); 4 },
    0xbb to { (A SUB E); 4 },
    0xbc to { (A SUB H); 4 },
    0xbd to { (A SUB L); 4 },
    0xbe to { (A SUB RAM.getByteAt(HL)); 8 },
    0xbf to { (A SUB A); 4 },
    0xc0 to {
      if (getZero() == '0') {
        PCl = RAM.getByteAt(SP)
        PCh = RAM.getByteAt(SP + 1)
        SP = SP + 2
        20
      } else {
        PC++
        8
      }
    },
    0xc1 to {
      C = RAM.getByteAt(SP)
      B = RAM.getByteAt(SP + 1)
      SP = SP + 2
      12
    },
    0xc2 to {
      if (getZero() == '0') {
        PC = getNextWord()
        16
      } else {
        PC = PC + 2
        12
      }
    },
    0xc3 to { PC = getNextWord(); 16 },
    0xc4 to {
      if (getZero() == '0') {
        val nextPC = getNextWord()
        RAM.setByteAt(SP - 1, PCh)
        RAM.setByteAt(SP - 2, PCl)
        SP = SP - 2
        PC = nextPC
        24
      } else {
        PC = PC + 2
        12
      }
    },
    0xc5 to {
      RAM.setByteAt(SP - 1, B)
      RAM.setByteAt(SP - 2, C)
      SP = SP - 2
      16
    },
    0xc6 to { A = A ADD RAM.getByteAt(PC++); 8 },
    0xc7 to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = 0
      16
    },
    0xc8 to {
      if (getZero() == '1') {
        PCl = RAM.getByteAt(SP)
        PCh = RAM.getByteAt(SP + 1)
        SP = SP + 2
        20
      } else {
        PC++
        8
      }
    },
    0xc9 to {
      PCl = RAM.getByteAt(SP)
      PCh = RAM.getByteAt(SP + 1)
      SP = SP + 2
      16
    },
    0xca to {
      if (getZero() == '1') {
        PC = getNextWord()
        16
      } else {
        PC = PC + 2
        12
      }
    },
    0xcb to { prefix = true; 4 },
    0xcc to {
      if (getZero() == '1') {
        val nextPC = getNextWord()
        RAM.setByteAt(SP - 1, PCh)
        RAM.setByteAt(SP - 2, PCl)
        SP = SP - 2
        PC = nextPC
        24
      } else {
        PC = PC + 2
        12
      }
    },
    0xcd to {
      val nextPC = getNextWord()
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = nextPC
      24
    },
    0xce to { A = (A ADC RAM.getByteAt(PC++)); 8 },
    0xcf to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = joinHighByteLowByte(0.toShort(), 0x8.toShort())
      16
    },
    0xd0 to {
      if (getCarry() == '0') {
        PCl = RAM.getByteAt(SP)
        PCh = RAM.getByteAt(SP + 1)
        SP = SP + 2
        20
      } else {
        PC++
        8
      }
    },
    0xd1 to {
      E = RAM.getByteAt(SP)
      D = RAM.getByteAt(SP + 1)
      SP = SP + 2
      12
    },
    0xd2 to {
      if (getCarry() == '0') {
        PC = getNextWord()
        16
      } else {
        PC = PC + 2
        12
      }
    },
    0xd4 to {
      if (getCarry() == '0') {
        val nextPC = getNextWord()
        RAM.setByteAt(SP - 1, PCh)
        RAM.setByteAt(SP - 2, PCl)
        SP = SP - 2
        PC = nextPC
        24
      } else {
        PC = PC + 2
        12
      }
    },
    0xd5 to {
      RAM.setByteAt(SP - 1, D)
      RAM.setByteAt(SP - 2, E)
      SP = SP - 2
      16
    },
    0xd6 to { A = A SUB RAM.getByteAt(PC++); 8 },
    0xd7 to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = joinHighByteLowByte(0.toShort(), 0x10.toShort())
      16
    },
    0xd8 to {
      if (getCarry() == '1') {
        PCl = RAM.getByteAt(SP)
        PCh = RAM.getByteAt(SP + 1)
        SP = SP + 2
        20
      } else {
        PC++
        8
      }
    },
    0xd9 to {
      interrupts = true
      PCl = RAM.getByteAt(SP)
      PCh = RAM.getByteAt(SP + 1)
      SP = SP + 2
      16
    },
    0xda to {
      if (getCarry() == '1') {
        PC = getNextWord()
        16
      } else {
        PC = PC + 2
        12
      }
    },
    0xdc to {
      if (getCarry() == '1') {
        val nextPC = getNextWord()
        RAM.setByteAt(SP - 1, PCh)
        RAM.setByteAt(SP - 2, PCl)
        SP = SP - 2
        PC = nextPC
        24
      } else {
        PC = PC + 2
        12
      }
    },
    0xde to { A = (A SBC RAM.getByteAt(PC++)); 8 },
    0xdf to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = joinHighByteLowByte(0.toShort(), 0x18.toShort())
      16
    },
    0xe0 to { RAM.setByteAt(0x0000FF00 + RAM.getByteAt(PC++).toInt(), A); 12 },
    0xe1 to {
      L = RAM.getByteAt(SP)
      H = RAM.getByteAt(SP + 1)
      SP = SP + 2
      12
    },
    0xe2 to { RAM.setByteAt(0x0000FF00 + C.toInt(), A); 8 },
    0xe5 to {
      RAM.setByteAt(SP - 1, H)
      RAM.setByteAt(SP - 2, L)
      SP = SP - 2
      16
    },
    0xe6 to { A = A AND RAM.getByteAt(PC++); 8 },
    0xe7 to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = joinHighByteLowByte(0.toShort(), 0x20.toShort())
      16
    },
    0xe8 to { SP = SP + (RAM.getByteAt(PC++).toByte()).toInt(); 16 },
    0xe9 to { PC = HL; 4 },
    0xea to { RAM.setByteAt(getNextWord(), A); 16 },
    0xee to { A = (A XOR RAM.getByteAt(PC++)); 8 },
    0xef to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = joinHighByteLowByte(0.toShort(), 0x28.toShort())
      16
    },
    0xf0 to { A = RAM.getByteAt(0x0000FF00 + RAM.getByteAt(PC++).toInt()); 12 },
    0xf1 to {
      F = RAM.getByteAt(SP)
      A = RAM.getByteAt(SP + 1)
      SP = SP + 2
      12
    },
    0xf2 to { A = RAM.getByteAt(0x0000FF00 + C.toInt()); 8 },
    0xf3 to { interrupts = false; 4 },
    0xf5 to {
      RAM.setByteAt(SP - 1, A)
      RAM.setByteAt(SP - 2, F)
      SP = SP - 2
      16
    },
    0xf6 to { A = A OR RAM.getByteAt(PC++); 8 },
    0xf7 to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = joinHighByteLowByte(0.toShort(), 0x30.toShort())
      16
    },
    0xf8 to { HL = SP + (RAM.getByteAt(PC++).toByte()).toInt(); 12 },
    0xf9 to { SP = HL; 8 },
    0xfa to { A = RAM.getByteAt(getNextWord()); 16 },
    0xfb to { interrupts = true; 4 },
    0xfe to { A SUB RAM.getByteAt(PC++); 8 },
    0xff to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = joinHighByteLowByte(0.toShort(), 0x38.toShort())
      16
    }
  )
  private val prefixOpcodes: Map<Int, ()->Int> = mapOf(
    0x00 to { B = rlc(B); 4 },
    0x01 to { C = rlc(C); 4 },
    0x02 to { D = rlc(D); 4 },
    0x03 to { E = rlc(E); 4 },
    0x04 to { H = rlc(H); 4 },
    0x05 to { L = rlc(L); 4 },
    0x06 to { RAM.setByteAt(HL, rlc(RAM.getByteAt(HL))); 16 },
    0x07 to { A = rlc(A); 4 },
    0x08 to { B = rrc(B); 4 },
    0x09 to { C = rrc(C); 4 },
    0x0a to { D = rrc(D); 4 },
    0x0b to { E = rrc(E); 4 },
    0x0c to { H = rrc(H); 4 },
    0x0d to { L = rrc(L); 4 },
    0x0e to { RAM.setByteAt(HL, rrc(RAM.getByteAt(HL))); 16 },
    0x0f to { A = rrc(A); 4 },
    0x10 to { B = rl(B); 4 },
    0x11 to { C = rl(C); 4 },
    0x12 to { D = rl(D); 4 },
    0x13 to { E = rl(E); 4 },
    0x14 to { H = rl(H); 4 },
    0x15 to { L = rl(L); 4 },
    0x16 to { RAM.setByteAt(HL, rl(RAM.getByteAt(HL))); 16 },
    0x17 to { A = rl(A); 4 },
    0x18 to { B = rr(B); 4 },
    0x19 to { C = rr(C); 4 },
    0x1a to { D = rr(D); 4 },
    0x1b to { E = rr(E); 4 },
    0x1c to { H = rr(H); 4 },
    0x1d to { L = rr(L); 4 },
    0x1e to { RAM.setByteAt(HL, rr(RAM.getByteAt(HL))); 16 },
    0x1f to { A = rr(A); 4 },
    0x20 to { B = sla(B); 8 },
    0x21 to { C = sla(C); 8 },
    0x22 to { D = sla(D); 8 },
    0x23 to { E = sla(E); 8 },
    0x24 to { H = sla(H); 8 },
    0x25 to { L = sla(L); 8 },
    0x26 to { RAM.setByteAt(HL, sla(RAM.getByteAt(HL))); 16 },
    0x27 to { A = sla(A); 8 },
    0x28 to { B = sra(B); 8 },
    0x29 to { C = sra(C); 8 },
    0x2a to { D = sra(D); 8 },
    0x2b to { E = sra(E); 8 },
    0x2c to { H = sra(H); 8 },
    0x2d to { L = sra(L); 8 },
    0x2e to { RAM.setByteAt(HL, sra(RAM.getByteAt(HL))); 16 },
    0x2f to { A = sra(A); 8 },
    0x30 to { B = swap(B); 8 },
    0x31 to { C = swap(C); 8 },
    0x32 to { D = swap(D); 8 },
    0x33 to { E = swap(E); 8 },
    0x34 to { H = swap(H); 8 },
    0x35 to { L = swap(L); 8 },
    0x36 to { RAM.setByteAt(HL, swap(RAM.getByteAt(HL))); 16 },
    0x37 to { A = swap(A); 8 },
    0x38 to { B = srl(B); 8 },
    0x39 to { C = srl(C); 8 },
    0x3a to { D = srl(D); 8 },
    0x3b to { E = srl(E); 8 },
    0x3c to { H = srl(H); 8 },
    0x3d to { L = srl(L); 8 },
    0x3e to { RAM.setByteAt(HL, srl(RAM.getByteAt(HL))); 16 },
    0x3f to { A = srl(A); 8 },
    0x40 to { setZero(BIT(B, 0)); 8 },
    0x41 to { setZero(BIT(C, 0)); 8 },
    0x42 to { setZero(BIT(D, 0)); 8 },
    0x43 to { setZero(BIT(E, 0)); 8 },
    0x44 to { setZero(BIT(H, 0)); 8 },
    0x45 to { setZero(BIT(L, 0)); 8 },
    0x46 to { setZero(BIT(RAM.getByteAt(HL), 0)); 12 },
    0x47 to { setZero(BIT(A, 0)); 8 },
    0x48 to { setZero(BIT(B, 1)); 8 },
    0x49 to { setZero(BIT(C, 1)); 8 },
    0x4a to { setZero(BIT(D, 1)); 8 },
    0x4b to { setZero(BIT(E, 1)); 8 },
    0x4c to { setZero(BIT(H, 1)); 8 },
    0x4d to { setZero(BIT(L, 1)); 8 },
    0x4e to { setZero(BIT(RAM.getByteAt(HL), 1)); 12 },
    0x4f to { setZero(BIT(A, 1)); 8 },
    0x50 to { setZero(BIT(B, 2)); 8 },
    0x51 to { setZero(BIT(C, 2)); 8 },
    0x52 to { setZero(BIT(D, 2)); 8 },
    0x53 to { setZero(BIT(E, 2)); 8 },
    0x54 to { setZero(BIT(H, 2)); 8 },
    0x55 to { setZero(BIT(L, 2)); 8 },
    0x56 to { setZero(BIT(RAM.getByteAt(HL), 2)); 12 },
    0x57 to { setZero(BIT(A, 2)); 8 },
    0x58 to { setZero(BIT(B, 3)); 8 },
    0x59 to { setZero(BIT(C, 3)); 8 },
    0x5a to { setZero(BIT(D, 3)); 8 },
    0x5b to { setZero(BIT(E, 3)); 8 },
    0x5c to { setZero(BIT(H, 3)); 8 },
    0x5d to { setZero(BIT(L, 3)); 8 },
    0x5e to { setZero(BIT(RAM.getByteAt(HL), 3)); 12 },
    0x5f to { setZero(BIT(A, 3)); 8 },
    0x60 to { setZero(BIT(B, 4)); 8 },
    0x61 to { setZero(BIT(C, 4)); 8 },
    0x62 to { setZero(BIT(D, 4)); 8 },
    0x63 to { setZero(BIT(E, 4)); 8 },
    0x64 to { setZero(BIT(H, 4)); 8 },
    0x65 to { setZero(BIT(L, 4)); 8 },
    0x66 to { setZero(BIT(RAM.getByteAt(HL), 4)); 12 },
    0x67 to { setZero(BIT(A, 4)); 8 },
    0x68 to { setZero(BIT(B, 5)); 8 },
    0x69 to { setZero(BIT(C, 5)); 8 },
    0x6a to { setZero(BIT(D, 5)); 8 },
    0x6b to { setZero(BIT(E, 5)); 8 },
    0x6c to { setZero(BIT(H, 5)); 8 },
    0x6d to { setZero(BIT(L, 5)); 8 },
    0x6e to { setZero(BIT(RAM.getByteAt(HL), 5)); 12 },
    0x6f to { setZero(BIT(A, 5)); 8 },
    0x70 to { setZero(BIT(B, 6)); 8 },
    0x71 to { setZero(BIT(C, 6)); 8 },
    0x72 to { setZero(BIT(D, 6)); 8 },
    0x73 to { setZero(BIT(E, 6)); 8 },
    0x74 to { setZero(BIT(H, 6)); 8 },
    0x75 to { setZero(BIT(L, 6)); 8 },
    0x76 to { setZero(BIT(RAM.getByteAt(HL), 6)); 12 },
    0x77 to { setZero(BIT(A, 6)); 8 },
    0x78 to { setZero(BIT(B, 7)); 8 },
    0x79 to { setZero(BIT(C, 7)); 8 },
    0x7a to { setZero(BIT(D, 7)); 8 },
    0x7b to { setZero(BIT(E, 7)); 8 },
    0x7c to { setZero(BIT(H, 7)); 8 },
    0x7d to { setZero(BIT(L, 7)); 8 },
    0x7e to { setZero(BIT(RAM.getByteAt(HL), 7)); 12 },
    0x7f to { setZero(BIT(A, 7)); 8 },
    0x80 to { B = RES(B, 0); 8 },
    0x81 to { C = RES(C, 0); 8 },
    0x82 to { D = RES(D, 0); 8 },
    0x83 to { E = RES(E, 0); 8 },
    0x84 to { H = RES(H, 0); 8 },
    0x85 to { L = RES(L, 0); 8 },
    0x86 to { RAM.setByteAt(HL, RES(RAM.getByteAt(HL), 0)); 16 },
    0x87 to { A = RES(A, 0); 8 },
    0x88 to { B = RES(B, 1); 8 },
    0x89 to { C = RES(C, 1); 8 },
    0x8a to { D = RES(D, 1); 8 },
    0x8b to { E = RES(E, 1); 8 },
    0x8c to { H = RES(H, 1); 8 },
    0x8d to { L = RES(L, 1); 8 },
    0x8e to { RAM.setByteAt(HL, RES(RAM.getByteAt(HL), 1)); 16 },
    0x8f to { A = RES(A, 1); 8 },
    0x90 to { B = RES(B, 2); 8 },
    0x91 to { C = RES(C, 2); 8 },
    0x92 to { D = RES(D, 2); 8 },
    0x93 to { E = RES(E, 2); 8 },
    0x94 to { H = RES(H, 2); 8 },
    0x95 to { L = RES(L, 2); 8 },
    0x96 to { RAM.setByteAt(HL, RES(RAM.getByteAt(HL), 2)); 16 },
    0x97 to { A = RES(A, 2); 8 },
    0x98 to { B = RES(B, 3); 8 },
    0x99 to { C = RES(C, 3); 8 },
    0x9a to { D = RES(D, 3); 8 },
    0x9b to { E = RES(E, 3); 8 },
    0x9c to { H = RES(H, 3); 8 },
    0x9d to { L = RES(L, 3); 8 },
    0x9e to { RAM.setByteAt(HL, RES(RAM.getByteAt(HL), 3)); 16 },
    0x9f to { A = RES(A, 3); 8 },
    0xa0 to { B = RES(B, 4); 8 },
    0xa1 to { C = RES(C, 4); 8 },
    0xa2 to { D = RES(D, 4); 8 },
    0xa3 to { E = RES(E, 4); 8 },
    0xa4 to { H = RES(H, 4); 8 },
    0xa5 to { L = RES(L, 4); 8 },
    0xa6 to { RAM.setByteAt(HL, RES(RAM.getByteAt(HL), 4)); 16 },
    0xa7 to { A = RES(A, 4); 8 },
    0xa8 to { B = RES(B, 5); 8 },
    0xa9 to { C = RES(C, 5); 8 },
    0xaa to { D = RES(D, 5); 8 },
    0xab to { E = RES(E, 5); 8 },
    0xac to { H = RES(H, 5); 8 },
    0xad to { L = RES(L, 5); 8 },
    0xae to { RAM.setByteAt(HL, RES(RAM.getByteAt(HL), 5)); 16 },
    0xaf to { A = RES(A, 5); 8 },
    0xb0 to { B = RES(B, 6); 8 },
    0xb1 to { C = RES(C, 6); 8 },
    0xb2 to { D = RES(D, 6); 8 },
    0xb3 to { E = RES(E, 6); 8 },
    0xb4 to { H = RES(H, 6); 8 },
    0xb5 to { L = RES(L, 6); 8 },
    0xb6 to { RAM.setByteAt(HL, RES(RAM.getByteAt(HL), 6)); 16 },
    0xb7 to { A = RES(A, 6); 8 },
    0xb8 to { B = RES(B, 7); 8 },
    0xb9 to { C = RES(C, 7); 8 },
    0xba to { D = RES(D, 7); 8 },
    0xbb to { E = RES(E, 7); 8 },
    0xbc to { H = RES(H, 7); 8 },
    0xbd to { L = RES(L, 7); 8 },
    0xbe to { RAM.setByteAt(HL, RES(RAM.getByteAt(HL), 7)); 16 },
    0xbf to { A = RES(A, 7); 8 },
    0xc0 to { B = SET(B, 0); 8 },
    0xc1 to { C = SET(C, 0); 8 },
    0xc2 to { D = SET(D, 0); 8 },
    0xc3 to { E = SET(E, 0); 8 },
    0xc4 to { H = SET(H, 0); 8 },
    0xc5 to { L = SET(L, 0); 8 },
    0xc6 to { RAM.setByteAt(HL, SET(RAM.getByteAt(HL), 0)); 16 },
    0xc7 to { A = SET(A, 0); 8 },
    0xc8 to { B = SET(B, 1); 8 },
    0xc9 to { C = SET(C, 1); 8 },
    0xca to { D = SET(D, 1); 8 },
    0xcb to { E = SET(E, 1); 8 },
    0xcc to { H = SET(H, 1); 8 },
    0xcd to { L = SET(L, 1); 8 },
    0xce to { RAM.setByteAt(HL, SET(RAM.getByteAt(HL), 1)); 16 },
    0xcf to { A = SET(A, 1); 8 },
    0xd0 to { B = SET(B, 2); 8 },
    0xd1 to { C = SET(C, 2); 8 },
    0xd2 to { D = SET(D, 2); 8 },
    0xd3 to { E = SET(E, 2); 8 },
    0xd4 to { H = SET(H, 2); 8 },
    0xd5 to { L = SET(L, 2); 8 },
    0xd6 to { RAM.setByteAt(HL, SET(RAM.getByteAt(HL), 2)); 16 },
    0xd7 to { A = SET(A, 2); 8 },
    0xd8 to { B = SET(B, 3); 8 },
    0xd9 to { C = SET(C, 3); 8 },
    0xda to { D = SET(D, 3); 8 },
    0xdb to { E = SET(E, 3); 8 },
    0xdc to { H = SET(H, 3); 8 },
    0xdd to { L = SET(L, 3); 8 },
    0xde to { RAM.setByteAt(HL, SET(RAM.getByteAt(HL), 3)); 16 },
    0xdf to { A = SET(A, 3); 8 },
    0xe0 to { B = SET(B, 4); 8 },
    0xe1 to { C = SET(C, 4); 8 },
    0xe2 to { D = SET(D, 4); 8 },
    0xe3 to { E = SET(E, 4); 8 },
    0xe4 to { H = SET(H, 4); 8 },
    0xe5 to { L = SET(L, 4); 8 },
    0xe6 to { RAM.setByteAt(HL, SET(RAM.getByteAt(HL), 4)); 16 },
    0xe7 to { A = SET(A, 4); 8 },
    0xe8 to { B = SET(B, 5); 8 },
    0xe9 to { C = SET(C, 5); 8 },
    0xea to { D = SET(D, 5); 8 },
    0xeb to { E = SET(E, 5); 8 },
    0xec to { H = SET(H, 5); 8 },
    0xed to { L = SET(L, 5); 8 },
    0xee to { RAM.setByteAt(HL, SET(RAM.getByteAt(HL), 5)); 16 },
    0xef to { A = SET(A, 5); 8 },
    0xf0 to { B = SET(B, 6); 8 },
    0xf1 to { C = SET(C, 6); 8 },
    0xf2 to { D = SET(D, 6); 8 },
    0xf3 to { E = SET(E, 6); 8 },
    0xf4 to { H = SET(H, 6); 8 },
    0xf5 to { L = SET(L, 6); 8 },
    0xf6 to { RAM.setByteAt(HL, SET(RAM.getByteAt(HL), 6)); 16 },
    0xf7 to { A = SET(A, 6); 8 },
    0xf8 to { B = SET(B, 7); 8 },
    0xf9 to { C = SET(C, 7); 8 },
    0xfa to { D = SET(D, 7); 8 },
    0xfb to { E = SET(E, 7); 8 },
    0xfc to { H = SET(H, 7); 8 },
    0xfd to { L = SET(L, 7); 8 },
    0xfe to { RAM.setByteAt(HL, SET(RAM.getByteAt(HL), 7)); 16 },
    0xff to { A = SET(A, 7); 8 }
  )
  private fun INT(addr: Int) {
    interrupts = false
    RAM.setByteAt(SP - 1, PCh)
    RAM.setByteAt(SP - 2, PCl)
    SP = SP - 2
    PC = addr
  }
  fun handleInterrupts() {
    if (interrupts) {
      val ints = RAM.getByteAt(0xFF0F).toInt()
      val intsEnabled = RAM.getByteAt(0xFFFF).toInt()
      println("Interrupt ${ints.toString(2)} ${intsEnabled.toString(2)}")
      if ((ints and intsEnabled) > 0) {
        when {
          (ints and 0b1) == 1 -> {
            RAM.setByteAt(0xFF0F, (ints and 0b11111110).toShort())
            INT(0x40)
          }
          (ints and 0b10) == 0b10 -> {
            RAM.setByteAt(0xFF0F, (ints and 0b11111101).toShort())
            INT(0x48)
          }
          (ints and 0b100) == 0b100 -> {
            RAM.setByteAt(0xFF0F, (ints and 0b11111011).toShort())
            INT(0x50)
          }
          (ints and 0b1000) == 0b1000 -> {
            RAM.setByteAt(0xFF0F, (ints and 0b11110111).toShort())
            INT(0x58)
          }
          (ints and 0b10000) == 0b10000 -> {
            RAM.setByteAt(0xFF0F, (ints and 0b11101111).toShort())
            INT(0x60)
          }
        }
      }
    }
  }

  override fun toString() = """
CPU state
PC: ${PC.toString(16)}
SP: ${SP.toString(16)}
A: ${A.toString(16)} B: ${B.toString(16)} C: ${C.toString(16)} D: ${D.toString(16)}
E: ${E.toString(16)} H: ${H.toString(16)} L: ${L.toString(16)}
F: ${F.toString(2).padStart(8, '0')}
HL: ${HL.toString(16).padStart(4, '0')} = $HL
BC: ${BC.toString(16)}
Running: $running
Int: $interrupts
Prefix: $prefix
CurrOp: ${RAM.getByteAt(PC).toString(16).padStart(2, '0')}
"""

  fun tick(): Int {
    if (running) {
      val op = RAM.getByteAt(PC++)
      if (prefix) {
        time += prefixOpcodes.get(op.toInt())!!()
        prefix = false
      } else {
        time += opcodes.get(op.toInt())!!()
      }
    }
    return time
  }
}
