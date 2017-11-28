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
  private val stack: HashMap<Int, Int> = HashMap()

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
  fun Short.INC(): Short {
      setZero('0')
      setHalfCarry('0')
      setSubstract('0')
      val fourth = this.toString(2).get(4)
      var rthis = (this + 1).toShort()
      if (rthis > 255.toShort() || rthis == 0.toShort()) {
        rthis = 0.toShort()
        setZero('1')
      }
      val fourthAfter = rthis.toString(2).get(4)
      if (fourth != fourthAfter) {
        setHalfCarry('1')
      }
      return rthis
  }
  fun Short.DEC(): Short {
    val fourth = this.toString(2).get(4)
    var rthis = (this - 1).toShort()
    setZero('0')
    setHalfCarry('0')
    if (rthis < (0.toShort())) {
      rthis = 255.toShort()
    }
    if (rthis == 0.toShort()) {
      setZero('1')
    }
    val fourthAfter = rthis.toString(2).get(4)
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
      rthis = (0xff.toShort() - rthis).toShort()
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
  private fun rl(A: Short): Short {
    setZero('0')
    setHalfCarry('0')
    setSubstract('0')
    val a = A.toString(2).padStart(8, '0')
    val c = getCarry()
    setCarry(a.first())
    return (a.substring(1) + c).toShort(2)
  }
  private fun rlc(A: Short): Short {
    setZero('0')
    setHalfCarry('0')
    setSubstract('0')
    val a = A.toString(2).padStart(8, '0')
    setCarry(a.first())
    return (a.substring(1) + a.first()).toShort(2)
  }

  private fun rr(A: Short): Short {
    setZero('0')
    setHalfCarry('0')
    setSubstract('0')
    val a = A.toString(2).padStart(8, '0')
    val c = getCarry()
    setCarry(a.last())
    return (c + a.substring(1)).toShort(2)
  }
  private fun rrc(A: Short): Short {
    setZero('0')
    setHalfCarry('0')
    setSubstract('0')
    val a = A.toString(2).padStart(8, '0')
    setCarry(a.last())
    return (a.last() + a.substring(0, a.length - 1)).toShort(2)
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
  private val opcodes: Map<Int, Function<Unit>> = mapOf(
    0x00 to { println("NOP") }, //NOP
    0x01 to { BC = getNextWord() },
    0x02 to { ram.setByteAt(BC, A) }, //ld (BC), A
    0x03 to { BC = BC.INC() }, //inc BC
    0x04 to { B = B.INC() }, //inc B
    0x05 to { B = B.DEC() }, //dec B
    0x06 to { B = ram.getByteAt(PC++) }, //ld B, d8
    0x07 to { A = rlc(A) }, // RLCA
    0x08 to {
      val a = getNextWord() //ld (a16), SP
      ram.setWordAt(a, SP)
    },
    0x09 to { HL = (HL ADD BC) }, //ADD HL, BC    
    0x0a to { A = ram.getByteAt(BC) },
    0x0b to { BC = BC.DEC() }, //DEC BC
    0x0c to { C = C.INC() }, //INC C
    0x0d to { C = C.DEC() }, //DEC C
    0x0e to { C = ram.getByteAt(PC++) }, //ld C, d8
    0x0f to { A = rrc(A) },
    0x10 to { println("stop") },
    0x11 to { DE = getNextWord() }, //ld DE, d16
    0x12 to { ram.setByteAt(DE, A) }, //ld (DE), A
    0x13 to { DE = DE.INC() }, // inc DE 
    0x14 to { D = D.INC() }, //inc d
    0x15 to { D = D.DEC() }, //dec d
    0x16 to { D = ram.getByteAt(PC++) }, //ld D,d8
    0x17 to { A = rl(A) }, //rla
    0x18 to { PC = PC + ram.getByteAt(PC).toByte().toInt() }, //JR r8
    0x19 to { HL = (HL ADD DE) }, // ADD HL,DE
    0x1a to { A = ram.getByteAt(DE) }, //ld A,(DE)
    0x1b to { DE = DE.DEC() },
    0x1c to { E = E.INC() },
    0x1d to { E = E.DEC() },
    0x1e to { E = ram.getByteAt(PC++) },
    0x1f to { A = rr(A) },
    0x20 to { //JR NZ,d8
      if (getZero() == '0') {
        PC = PC + ram.getByteAt(PC).toByte().toInt()
      }
    },
    0x21 to { HL = getNextWord() },
    0x22 to {
      ram.setByteAt(HL, A)
      HL = HL.INC()
    },
    0x23 to { HL = HL.INC() },
    0x24 to { H = H.INC() },
    0x25 to { H = H.DEC() },
    0x26 to { H = ram.getByteAt(PC++) },
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
    },
    0x28 to {
      if (getZero() == '1') {
        PC = PC + ram.getByteAt(PC).toByte().toInt()
      }
    },
    0x29 to { HL = (HL ADD HL) },
    0x2a to {
      A = ram.getByteAt(HL)
      HL = HL.INC()
    },
    0x2b to { HL = HL.DEC() },
    0x2c to { L = L.INC() },
    0x2d to { L = L.DEC() },
    0x2e to { L = ram.getByteAt(PC++) },
    0x2f to {
      setHalfCarry('1')
      setSubstract('1')
      val a = 0xFFFFFF + A.toInt()
      A = a.inv().toShort()
    },
    0x30 to {
      if (getCarry() == '0') {
        PC = PC + ram.getByteAt(PC).toByte().toInt()
      }
    },
    0x31 to { SP = getNextWord() },
    0x32 to {
      ram.setByteAt(HL, A)
      HL = HL.DEC()
    },
    0x33 to { SP = SP.INC() },
    0x34 to { ram.setByteAt(HL, ram.getByteAt(HL).INC()) },
    0x35 to { ram.setByteAt(HL, ram.getByteAt(HL).DEC()) },
    0x36 to { ram.setByteAt(HL, ram.getByteAt(PC++)) },
    0x37 to {
      setCarry('1')
      setSubstract('0')
      setHalfCarry('0')
    },
    0x38 to {
      if (getCarry() == '1') {
        PC = PC + ram.getByteAt(PC)
      }
    },
    0x39 to { HL = (HL ADD SP) },
    0x3a to {
      A = ram.getByteAt(HL)
      HL = HL.DEC()
    },
    0x3b to { SP = SP.DEC() },
    0x3c to { A = A.INC() },
    0x3d to { A = A.DEC() },
    0x3e to { A = ram.getByteAt(PC++) },
    0x3f to {
      setHalfCarry('0')
      setSubstract('0')
      val a = getCarry()
      setCarry('0')
      if ( a == '0' ) {
        setCarry('1')
      }
    },
    0x40 to { B = B },
    0x41 to { B = C },
    0x42 to { B = D },
    0x43 to { B = E },
    0x44 to { B = H },
    0x45 to { B = L },
    0x46 to { B = ram.getByteAt(HL) },
    0x47 to { B = A },
    0x48 to { C = B },
    0x49 to { C = C },
    0x4a to { C = D },
    0x4b to { C = E },
    0x4c to { C = H },
    0x4d to { C = L },
    0x4e to { C = ram.getByteAt(HL) },
    0x4f to { C = A },
    0x50 to { D = B },
    0x51 to { D = C },
    0x52 to { D = D },
    0x53 to { D = E },
    0x54 to { D = H },
    0x55 to { D = L },
    0x56 to { D = ram.getByteAt(HL) },
    0x57 to { D = A },
    0x58 to { E = B },
    0x59 to { E = C },
    0x5a to { E = D },
    0x5b to { E = E },
    0x5c to { E = H },
    0x5d to { E = L },
    0x5e to { E = ram.getByteAt(HL) },
    0x5f to { E = A },
    0x60 to { H = B },
    0x61 to { H = C },
    0x62 to { H = D },
    0x63 to { H = E },
    0x64 to { H = H },
    0x65 to { H = L },
    0x66 to { H = ram.getByteAt(HL) },
    0x67 to { H = A },
    0x68 to { L = B },
    0x69 to { L = C },
    0x6a to { L = D },
    0x6b to { L = E },
    0x6c to { L = H },
    0x6d to { L = L },
    0x6e to { L = ram.getByteAt(HL) },
    0x6f to { L = A },
    0x70 to { ram.setByteAt(HL, B) },
    0x71 to { ram.setByteAt(HL, C) },
    0x72 to { ram.setByteAt(HL, D) },
    0x73 to { ram.setByteAt(HL, E) },
    0x74 to { ram.setByteAt(HL, H) },
    0x75 to { ram.setByteAt(HL, L) },
    0x76 to { println("HALT") },
    0x77 to { ram.setByteAt(HL, A) },
    0x78 to { A = B },
    0x79 to { A = C },
    0x7a to { A = D },
    0x7b to { A = E },
    0x7c to { A = H },
    0x7d to { A = L },
    0x7e to { A = ram.getByteAt(HL) },
    0x7f to { A = A },
    0x80 to { A = (A ADD B) },
    0x81 to { A = (A ADD C) },
    0x82 to { A = (A ADD D) },
    0x83 to { A = (A ADD E) },
    0x84 to { A = (A ADD H) },
    0x85 to { A = (A ADD L) },
    0x86 to { A = (A ADD ram.getByteAt(HL)) },
    0x87 to { A = (A ADD A) },
    0x88 to { A = (A ADC B) },
    0x89 to { A = (A ADC C) },
    0x8a to { A = (A ADC D) },
    0x8b to { A = (A ADC E) },
    0x8c to { A = (A ADC H) },
    0x8d to { A = (A ADC L) },
    0x8e to { A = (A ADC ram.getByteAt(HL)) },
    0x8f to { A = (A ADC A) },
    0x90 to { A = (A SUB B) },
    0x91 to { A = (A SUB C) },
    0x92 to { A = (A SUB D) },
    0x93 to { A = (A SUB E) },
    0x94 to { A = (A SUB H) },
    0x95 to { A = (A SUB L) },
    0x96 to { A = (A SUB ram.getByteAt(HL)) },
    0x97 to { A = (A SUB A) },
    0x98 to { A = (A SBC B) },
    0x99 to { A = (A SBC C) },
    0x9a to { A = (A SBC D) },
    0x9b to { A = (A SBC E) },
    0x9c to { A = (A SBC H) },
    0x9d to { A = (A SBC L) },
    0x9e to { A = (A SBC ram.getByteAt(HL)) },
    0x9f to { A = (A SBC A) },
    0xa0 to { A = (A AND B) },
    0xa1 to { A = (A AND C) },
    0xa2 to { A = (A AND D) },
    0xa3 to { A = (A AND E) },
    0xa4 to { A = (A AND H) },
    0xa5 to { A = (A AND L) },
    0xa6 to { A = (A AND ram.getByteAt(HL)) },
    0xa7 to { A = (A AND A) },
    0xa8 to { A = (A XOR B) },
    0xa9 to { A = (A XOR C) },
    0xaa to { A = (A XOR D) },
    0xab to { A = (A XOR E) },
    0xac to { A = (A XOR H) },
    0xad to { A = (A XOR L) },
    0xae to { A = (A XOR ram.getByteAt(HL)) },
    0xaf to { A = (A XOR A) },
    0xb0 to { A = (A OR B) },
    0xb1 to { A = (A OR C) },
    0xb2 to { A = (A OR D) },
    0xb3 to { A = (A OR E) },
    0xb4 to { A = (A OR H) },
    0xb5 to { A = (A OR L) },
    0xb6 to { A = (A OR ram.getByteAt(HL)) },
    0xb7 to { A = (A OR A) },
    0xb8 to {
      (A SUB B)
      Unit
    },
    0xb9 to {
      (A SUB C)
      Unit
    },
    0xba to {
      (A SUB D)
      Unit
    },
    0xbb to {
      (A SUB E)
      Unit
    },
    0xbc to {
      (A SUB H)
      Unit
    },
    0xbd to {
      (A SUB L)
      Unit
    },
    0xbe to {
      (A SUB ram.getByteAt(HL))
      Unit
    },
    0xbf to {
      (A SUB A)
      Unit
    },
    0xc0 to {
      if (getZero() == '0') {
        PC = checkNotNull(stack.get(SP))
        SP = SP + 2
      }
    },
    0xc1 to {
      BC = checkNotNull(stack.get(SP))
      stack.remove(SP)
      SP = SP + 2
    },
    0xc2 to {
      if (getZero() == '0') {
        PC = getNextWord()
      }
    },
    0xc3 to {
      PC = getNextWord()
    },
    0xc4 to {
      if (getZero() == '0') {
        stack.put(SP, PC)
        SP = SP - 2
        PC = getNextWord()
      }
    },
    0xc5 to {
      stack.put(SP, BC)
      SP = SP - 2
    },
    0xc6 to { A = A ADD ram.getByteAt(PC++) },
    0xc7 to {
      stack.put(SP, PC)
      SP = SP - 2
      PC = 0
    },
    0xc8 to {
      if (getZero() == '1') {
        PC = checkNotNull(stack.get(SP))
        SP = SP + 2
      }
    },
    0xc9 to {
      PC = checkNotNull(stack.get(SP))
      SP = SP + 2
    },
    0xca to {
      if (getZero() == '1') {
        PC = getNextWord()
      }
    },
    0xcb to {
      println("PREFIX")
    },
    0xcc to {
      if (getZero() == '1') {
        stack.put(SP, PC)
        SP = SP - 2
        PC = getNextWord()
      }
    },
    0xcd to {
      stack.put(SP, PC)
      SP = SP - 2
      PC = getNextWord()
    },
    0xce to { A = (A ADC ram.getByteAt(PC++)) },
    0xcf to {
      stack.put(SP, PC)
      SP = SP - 2
      PC = joinHighByteLowByte(0.toShort(), 0x8.toShort())
    }

  )
  fun tick() {
  }
}
