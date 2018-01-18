package de.prt.gb.hardware
// import kotlin.system.exitProcess

object CPU {
  internal var A: Short = 0
  internal var B: Short = 0
  internal var C: Short = 0
  internal var D: Short = 0
  internal var E: Short = 0
  private var _F: Short = 0
  internal var F: Short
    get() = _F
    set(f: Short) {
      _F = (f.toInt() and 0b11110000).toShort()
    }
  internal var H: Short = 0
  internal var L: Short = 0
  internal var PCh: Short = 0
  internal var PCl: Short = 0
  internal var SP: Int = 0
  internal var running = true
  internal var interrupts = true
  internal var prefix = false
  internal var time = 0
  internal var print = false
  internal var op = 0

  fun splitHighByteLowByte(x: Int): Pair<Short, Short> {
    val b = x and 0b0000000011111111
    val a = (x and 0b1111111100000000) shr 8
    return Pair(a.toShort(), b.toShort())
  }
  internal fun joinHighByteLowByte(a: Short, b: Short): Int {
      return ((a * 256) + b).toInt()
  }

  internal fun getCarry(): Boolean = F.toString(2).padStart(8, '0').get(3) == '1'
  internal fun setCarry(c: Boolean) {
    when (c) {
      false -> F = (F.toInt() and 0b11100000).toShort()
      true -> F = (F.toInt() or 0b00010000).toShort()
    }
  }
  internal fun getHalfCarry(): Boolean = F.toString(2).padStart(8, '0').get(2) == '1'
  internal fun setHalfCarry(c: Boolean) {
    when (c) {
      false -> F = (F.toInt() and 0b11010000).toShort()
      true -> F = (F.toInt() or 0b00100000).toShort()
    }
  }
  internal fun getSubstract(): Boolean = F.toString(2).padStart(8, '0').get(1) == '1'
  internal fun setSubstract(c: Boolean) {
    when (c) {
      false -> F = (F.toInt() and 0b10110000).toShort()
      true -> F = (F.toInt() or 0b01000000).toShort()
    }
  }
  internal fun getZero(): Boolean = F.toString(2).padStart(8, '0').get(0) == '1'
  internal fun setZero(c: Boolean) {
    when (c) {
      false -> F = (F.toInt() and 0b01110000).toShort()
      true -> F = (F.toInt() or 0b10000000).toShort()
    }
  }
  internal fun INC(a: Short): Short {
      setZero(false)
      setHalfCarry(false)
      setSubstract(false)
      val fourth = a.toString(2).padStart(8, '0').get(4)
      var rthis = (a + 1).toShort()
      if (rthis > 255.toShort() || rthis == 0.toShort()) {
        rthis = 0.toShort()
        setZero(true)
      }
      val fourthAfter = rthis.toString(2).padStart(8, '0').get(4)
      if (fourth != fourthAfter) {
        setHalfCarry(true)
      }
      return rthis
  }
  internal fun DEC(a: Short): Short {
    val fourth = a.toString(2).padStart(8, '0').get(4)
    var rthis = (a - 1).toShort()
    setZero(false)
    setHalfCarry(false)
    if (rthis < (0.toShort())) {
      rthis = 255.toShort()
    }
    if (rthis == 0.toShort()) {
      setZero(true)
    }
    val fourthAfter = rthis.toString(2).padStart(8, '0').get(4)
    if (fourthAfter != fourth) {
      setHalfCarry(true)
    }
    setSubstract(true)
    return rthis
  }
  internal fun INC(a: Int): Int {
    var rthis = a + 1
    return rthis and 0xFFFF
  }
  internal fun DEC(a: Int): Int {
    var rthis = a - 1
    return rthis and 0xFFFF
  }

  internal fun ADD(a: Int, b: Int): Int {
      setHalfCarry(false)
      setSubstract(false)
      setCarry(false)
      val halfCarry = ((a and 0b11111111) + (b and 0b11111111)) > 0b11111111
      setHalfCarry(halfCarry)
      var rthis = a + b
      if (rthis > 0xFFFF || rthis < 0) {
        rthis = rthis and 0xFFFF
        setCarry(true)
      }

      return rthis
  }
  internal fun ADD(a: Short, b: Short): Short {
    setHalfCarry(false)
    setSubstract(false)
    setCarry(false)
    setZero(false)
    val halfCarry = ((a.toInt() and 0b1111) + (b.toInt() and 0b1111)) > 0b1111
    setHalfCarry(halfCarry)
    var rthis = (b + a)
    if (rthis > 0xFF) {
      rthis = (rthis and 0xFF)
      setCarry(true)
    }
    if (rthis == 0) {
      setZero(true)
    }
    return rthis.toShort()
  }
  internal fun ADC(a: Short, b: Short): Short {
    val c = if (getCarry()) 1 else 0
    setHalfCarry(false)
    setSubstract(false)
    setCarry(false)
    setZero(false)
    val halfCarry = ((a.toInt() and 0b1111) + (b.toInt() and 0b1111) + c) > 0b1111
    setHalfCarry(halfCarry)
    var rthis = a + b + c
    if (rthis > 0xFF) {
      rthis = (rthis and 0xFF)
      setCarry(true)
    }
    if (rthis == 0) {
      setZero(true)
    }
    return rthis.toShort()
  }
  internal fun SUB(a: Short, b: Short): Short {
    setHalfCarry(false)
    setSubstract(true)
    setCarry(false)
    setZero(false)
    val halfCarry = ((a.toInt() and 0b1111) - (b.toInt() and 0b1111)) < 0
    setHalfCarry(halfCarry)
    var rthis = (a - b)
    if (rthis < 0) {
      rthis = (rthis and 0xFF)
      setCarry(true)
    }
    if (rthis == 0) {
      setZero(true)
    }
    return rthis.toShort()
  }
  internal fun SBC(a: Short, b: Short): Short {
    val c = if (getCarry()) 1 else 0
    setHalfCarry(false)
    setSubstract(true)
    setCarry(false)
    setZero(false)
    val halfCarry = (((a.toInt() and 0b1111) - (b.toInt() and 0b1111)) - c) < 0
    setHalfCarry(halfCarry)
    var rthis = ((a - b) - c)
    if (rthis < 0) {
      rthis = (rthis and 0xFF)
      setCarry(true)
    }
    if (rthis == 0) {
      setZero(true)
    }
    return rthis.toShort()
  }
  internal fun AND(a: Short, b: Short): Short {
    setZero(false)
    setSubstract(false)
    setHalfCarry(true)
    setCarry(false)
    val ret = (b.toInt() and a.toInt()).toShort()
    if (ret == 0.toShort()) {
      setZero(true)
    }
    return ret
  }
  internal fun XOR(a: Short, b: Short): Short {
    setZero(false)
    setSubstract(false)
    setHalfCarry(false)
    setCarry(false)
    val ret = (b.toInt() xor a.toInt()).toShort()
    if (ret == 0.toShort()) {
      setZero(true)
    }
    return ret
  }
  internal fun OR(a: Short, b: Short): Short {
    setZero(false)
    setSubstract(false)
    setHalfCarry(false)
    setCarry(false)
    val ret = (b.toInt() or a.toInt()).toShort()
    if (ret == 0.toShort()) {
      setZero(true)
    }
    return ret
  }
  internal fun rl(a: Short): Short {
    setZero(false)
    setHalfCarry(false)
    setSubstract(false)
    var newA = (a.toInt() shl 1)
    val c = if (getCarry()) 1 else 0
    newA = newA + c
    if (newA > 0xFF) {
      setCarry(true)
      newA = newA and 0xFF
    }
    if (prefix) {
      if (newA == 0) {
        setZero(true)
      }
    }
    return newA.toShort()
  }
  internal fun rlc(a: Short): Short {
    setZero(false)
    setHalfCarry(false)
    setSubstract(false)
    var newA = (a.toInt() shl 1)
    println(newA.toString(2))
    if (newA > 0xFF) {
      setCarry(true)
      newA++
    }
    newA = newA and 0xFF
    if (prefix) {
      if (newA == 0) {
        setZero(true)
      }
    }
    return newA.toShort()
  }

  internal fun rr(a: Short): Short {
    setZero(false)
    setHalfCarry(false)
    setSubstract(false)
    val carry = ((a % 2) == 1)
    var newA = (a.toInt() shr 1)
    val c = if (getCarry()) 0b10000000 else 0
    setCarry(carry)
    newA = newA + c
    if (prefix) {
      if (newA == 0) {
        setZero(true)
      }
    }
    return newA.toShort()
  }
  internal fun rrc(a: Short): Short {
    setZero(false)
    setHalfCarry(false)
    setSubstract(false)
    val carry = ((a % 2) == 1)
    var newA = (a.toInt() shr 1)
    if (carry) {
      newA = newA + 0b10000000
    }
    setCarry(carry)
    if (prefix) {
      if (newA == 0) {
        setZero(true)
      }
    }
    return newA.toShort()
  }

  internal fun sla(a: Short): Short {
    setZero(false)
    setHalfCarry(false)
    setSubstract(false)
    val an = a.toString(2).padStart(8, '0')
    setCarry(an.first() == '1')
    val ret = (an.substring(1) + "0").toShort(2)
    if (ret.toInt() == 0) {
      setZero(true)
    }
    return ret
  }
  internal fun sra(a: Short): Short {
    setZero(false)
    setHalfCarry(false)
    setSubstract(false)
    val an = a.toString(2).padStart(8, '0')
    setCarry(an.last() == '1')
    val ret = (an.first() + an.substring(0, an.length - 1)).toShort(2)
    if (ret.toInt() == 0) {
      setZero(true)
    }
    return ret
  }
  internal fun srl(a: Short): Short {
    setZero(false)
    setHalfCarry(false)
    setSubstract(false)
    val an = a.toString(2).padStart(8, '0')
    setCarry(an.last() == '1')
    val ret = ('0' + an.substring(0, an.length - 1)).toShort(2)
    if (ret.toInt() == 0) {
      setZero(true)
    }
    return ret
  }

  internal fun swap(a: Short): Short {
    setZero(false)
    setHalfCarry(false)
    setSubstract(false)
    setCarry(false)
    val an = a.toString(2).padStart(8, '0')
    val ret = (an.substring(4) + an.substring(0, 4)).toShort(2)
    if (ret.toInt() == 0) {
      setZero(true)
    }
    return ret
  }

  fun BIT(a: Short, c: Int): Char {
    setHalfCarry(true)
    setSubstract(false)
    val an = a.toString(2).padStart(8, '0').get(7 - c)
    val ret = if (an == '1') '0' else '1'
    return ret
  }
  internal fun SET(a: Short, c: Int): Short {
    val an = a.toString(2).padStart(8, '0')
    val ret = an.substring(0, 7 - c) + '1' + an.substring(7 - c + 1)
    return ret.toShort(2)
  }
  internal fun RES(a: Short, c: Int): Short {
    val an = a.toString(2).padStart(8, '0')
    val ret = an.substring(0, 7 - c) + '0' + an.substring(7 - c + 1)
    return ret.toShort(2)
  }

  internal var PC: Int
    get() = joinHighByteLowByte(PCh, PCl)
    set(x: Int) {
      val (nb, nc) = splitHighByteLowByte(x)
      PCh = nb
      PCl = nc
    }
  internal var AF: Int
    get() = joinHighByteLowByte(A, F)
    set(x: Int) {
      val (nb, nc) = splitHighByteLowByte(x)
      A = nb
      F = nc
    }
  internal var BC: Int
    get() = joinHighByteLowByte(B, C)
    set(x: Int) {
      val (nb, nc) = splitHighByteLowByte(x)
      B = nb
      C = nc
    }

  internal var DE: Int
    get() = joinHighByteLowByte(D, E)
    set(x: Int) {
      val (nb, nc) = splitHighByteLowByte(x)
      D = nb
      E = nc
    }

  internal var HL: Int
    get() = joinHighByteLowByte(H, L)
    set(x: Int) {
      val (nb, nc) = splitHighByteLowByte(x)
      H = nb
      L = nc
    }
  internal fun getNextWord(): Int {
    val a = RAM.getByteAt(PC)
    ++PC
    val b = RAM.getByteAt(PC)
    ++PC
    return joinHighByteLowByte(b, a)
  }
  internal val opcodes: Map<Int, ()->Int> = mapOf(
    0x00 to { 4 }, //NOP
    0x01 to { BC = getNextWord(); 12 },
    0x02 to { RAM.setByteAt(BC, A); 8 }, //ld (BC), A
    0x03 to { BC = INC(BC); 8 }, //inc BC
    0x04 to { B = INC(B); 4 }, //inc B
    0x05 to { B = DEC(B); 4 }, //dec B
    0x06 to { B = RAM.getByteAt(PC++); 8 }, //ld B, d8
    0x07 to { A = rlc(A); 4 }, // RLCA
    0x08 to {
      val a = getNextWord() //ld (a16), SP
      RAM.setWordAt(a, SP)
      20
    },
    0x09 to { HL = ADD(HL, BC); 8 }, //ADD HL, BC    
    0x0a to { A = RAM.getByteAt(BC); 8 },
    0x0b to { BC = DEC(BC); 8 }, //DEC BC
    0x0c to { C = INC(C); 4 }, //INC C
    0x0d to { C = DEC(C); 4 }, //DEC C
    0x0e to { C = RAM.getByteAt(PC++); 8 }, //ld C, d8
    0x0f to { A = rrc(A); 4 },
    0x10 to {
      running = false
      //exitProcess(0)
      4
    },
    0x11 to { DE = getNextWord(); 12 }, //ld DE, d16
    0x12 to { RAM.setByteAt(DE, A); 8 }, //ld (DE), A
    0x13 to { DE = INC(DE); 8 }, // inc DE 
    0x14 to { D = INC(D); 4 }, //inc d
    0x15 to { D = DEC(D); 4 }, //dec d
    0x16 to { D = RAM.getByteAt(PC++); 8 }, //ld D,d8
    0x17 to { A = rl(A); 4 }, //rla
    0x18 to {
      PC = PC + RAM.getByteAt(PC).toByte().toInt() + 1
      if (PC > 0xFFFF || PC < 0) {
        PC = PC and 0xFFFF
        println(PC)
      }
      12
    }, //JR r8
    0x19 to { HL = ADD(HL, DE); 8 }, // ADD HL,DE
    0x1a to { A = RAM.getByteAt(DE); 8 }, //ld A,(DE)
    0x1b to { DE = DEC(DE); 8 },
    0x1c to { E = INC(E); 4 },
    0x1d to { E = DEC(E); 4 },
    0x1e to { E = RAM.getByteAt(PC++); 8 },
    0x1f to { A = rr(A); 4 },
    0x20 to { //JR NZ,d8
      if (!getZero()) {
        PC = PC + RAM.getByteAt(PC).toByte().toInt() + 1
        if (PC > 0xFFFF || PC < 0) {
          PC = PC and 0xFFFF
          println(PC)
        }
        12
      } else {
        PC++
        8
      }
    },
    0x21 to { HL = getNextWord(); 12 },
    0x22 to {
      RAM.setByteAt(HL, A)
      HL = INC(HL)
      8
    },
    0x23 to { HL = INC(HL); 8 },
    0x24 to { H = INC(H); 4 },
    0x25 to { H = DEC(H); 4 },
    0x26 to { H = RAM.getByteAt(PC++); 8 },
    0x27 to { //daa
      var a = A.toInt() and 0b1111
      if (a > 9) {
        A = (A + 6).toShort()
      }
      var b = (A.toInt() and 0b11110000) shr 4
      if (b > 9) {
        A = (A + 0x60).toShort()
      }
      if (A > 0xFF) {
        setCarry(true)
        A = (A.toInt() and 0xFF).toShort()
      }
      if (A == 0.toShort()) {
        setZero(true)
      } else {
        setZero(false)
      }
      setHalfCarry(false)
      4
    },
    0x28 to { // JR Z,n 
      if (getZero()) {
        val offset = RAM.getByteAt(PC).toByte().toInt()
        PC = PC + offset + 1
        if (PC > 0xFFFF || PC < 0) {
          PC = PC and 0xFFFF
        }
        12
      } else {
        PC++
        8
      }
    },
    0x29 to { HL = ADD(HL, HL); 8 },
    0x2a to {
      A = RAM.getByteAt(HL)
      HL = INC(HL)
      8
    },
    0x2b to { HL = DEC(HL); 8 },
    0x2c to { L = INC(L); 4 },
    0x2d to { L = DEC(L); 4 },
    0x2e to { L = RAM.getByteAt(PC++); 8 },
    0x2f to {
      setHalfCarry(true)
      setSubstract(true)
      val a = A.toInt()
        .toString(2).padStart(8, '0')
        .map({ if (it == '0') '1' else '0' }).joinToString("")
        .toInt(2).toShort()
      A = a
      4
    },
    0x30 to {
      if (!getCarry()) {
        PC = PC + RAM.getByteAt(PC).toByte().toInt() + 1
        if (PC > 0xFFFF || PC < 0) {
          PC = PC and 0xFFFF
        }
        12
      } else {
        PC++
        8
      }
    },
    0x31 to { SP = getNextWord(); 12 },
    0x32 to {
      RAM.setByteAt(HL, A)
      HL = DEC(HL)
      8
    },
    0x33 to { SP = INC(SP); 8 },
    0x34 to { RAM.setByteAt(HL, INC(RAM.getByteAt(HL))); 12 },
    0x35 to { RAM.setByteAt(HL, DEC(RAM.getByteAt(HL))); 12 },
    0x36 to { RAM.setByteAt(HL, RAM.getByteAt(PC++)); 12 },
    0x37 to {
      setCarry(true)
      setSubstract(false)
      setHalfCarry(false)
      4
    },
    0x38 to {
      if (getCarry()) {
        PC = PC + RAM.getByteAt(PC).toByte().toInt() + 1
        if (PC > 0xFFFF || PC < 0) {
          PC = PC and 0xFFFF
        }
        12
      } else {
        PC++
        8
      }
    },
    0x39 to { HL = ADD(HL, SP); 8 },
    0x3a to {
      A = RAM.getByteAt(HL)
      HL = DEC(HL)
      8
    },
    0x3b to { SP = DEC(SP); 8 },
    0x3c to { A = INC(A); 4 },
    0x3d to { A = DEC(A); 4 },
    0x3e to { A = RAM.getByteAt(PC++); 8 },
    0x3f to {
      setHalfCarry(false)
      setSubstract(false)
      val a = getCarry()
      setCarry(false)
      if ( !a ) {
        setCarry(true)
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
    0x80 to { A = ADD(A, B); 4 },
    0x81 to { A = ADD(A, C); 4 },
    0x82 to { A = ADD(A, D); 4 },
    0x83 to { A = ADD(A, E); 4 },
    0x84 to { A = ADD(A, H); 4 },
    0x85 to { A = ADD(A, L); 4 },
    0x86 to { A = ADD(A, RAM.getByteAt(HL)); 8 },
    0x87 to { A = ADD(A, A); 4 },
    0x88 to { A = ADC(A, B); 4 },
    0x89 to { A = ADC(A, C); 4 },
    0x8a to { A = ADC(A, D); 4 },
    0x8b to { A = ADC(A, E); 4 },
    0x8c to { A = ADC(A, H); 4 },
    0x8d to { A = ADC(A, L); 4 },
    0x8e to { A = ADC(A, RAM.getByteAt(HL)); 8 },
    0x8f to { A = ADC(A, A); 4 },
    0x90 to { A = SUB(A, B); 4 },
    0x91 to { A = SUB(A, C); 4 },
    0x92 to { A = SUB(A, D); 4 },
    0x93 to { A = SUB(A, E); 4 },
    0x94 to { A = SUB(A, H); 4 },
    0x95 to { A = SUB(A, L); 4 },
    0x96 to { A = SUB(A, RAM.getByteAt(HL)); 8 },
    0x97 to { A = SUB(A, A); 4 },
    0x98 to { A = SBC(A, B); 4 },
    0x99 to { A = SBC(A, C); 4 },
    0x9a to { A = SBC(A, D); 4 },
    0x9b to { A = SBC(A, E); 4 },
    0x9c to { A = SBC(A, H); 4 },
    0x9d to { A = SBC(A, L); 4 },
    0x9e to { A = SBC(A, RAM.getByteAt(HL)); 8 },
    0x9f to { A = SBC(A, A); 4 },
    0xa0 to { A = AND(A, B); 4 },
    0xa1 to { A = AND(A, C); 4 },
    0xa2 to { A = AND(A, D); 4 },
    0xa3 to { A = AND(A, E); 4 },
    0xa4 to { A = AND(A, H); 4 },
    0xa5 to { A = AND(A, L); 4 },
    0xa6 to { A = AND(A, RAM.getByteAt(HL)); 8 },
    0xa7 to { A = AND(A, A); 4 },
    0xa8 to { A = XOR(A, B); 4 },
    0xa9 to { A = XOR(A, C); 4 },
    0xaa to { A = XOR(A, D); 4 },
    0xab to { A = XOR(A, E); 4 },
    0xac to { A = XOR(A, H); 4 },
    0xad to { A = XOR(A, L); 4 },
    0xae to { A = XOR(A, RAM.getByteAt(HL)); 8 },
    0xaf to { A = XOR(A, A); 4 },
    0xb0 to { A = OR(A, B); 4 },
    0xb1 to { A = OR(A, C); 4 },
    0xb2 to { A = OR(A, D); 4 },
    0xb3 to { A = OR(A, E); 4 },
    0xb4 to { A = OR(A, H); 4 },
    0xb5 to { A = OR(A, L); 4 },
    0xb6 to { A = OR(A, RAM.getByteAt(HL)); 8 },
    0xb7 to { A = OR(A, A); 4 },
    0xb8 to { SUB(A, B); 4 },
    0xb9 to { SUB(A, C); 4 },
    0xba to { SUB(A, D); 4 },
    0xbb to { SUB(A, E); 4 },
    0xbc to { SUB(A, H); 4 },
    0xbd to { SUB(A, L); 4 },
    0xbe to { SUB(A, RAM.getByteAt(HL)); 8 },
    0xbf to { SUB(A, A); 4 },
    0xc0 to {
      if (!getZero()) {
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
      if (!getZero()) {
        PC = getNextWord()
        16
      } else {
        PC = PC + 2
        12
      }
    },
    0xc3 to { PC = getNextWord(); 16 },
    0xc4 to {
      if (!getZero()) {
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
    0xc6 to { A = ADD(A, RAM.getByteAt(PC++)); 8 },
    0xc7 to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = 0
      16
    },
    0xc8 to {
      if (getZero()) {
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
      if (getZero()) {
        PC = getNextWord()
        16
      } else {
        PC = PC + 2
        12
      }
    },
    0xcb to { prefix = true; 4 },
    0xcc to {
      if (getZero()) {
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
    0xce to { A = ADC(A, RAM.getByteAt(PC++)); 8 },
    0xcf to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = 0x0008
      16
    },
    0xd0 to {
      if (!getCarry()) {
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
      if (!getCarry()) {
        PC = getNextWord()
        16
      } else {
        PC = PC + 2
        12
      }
    },
    0xd4 to {
      if (!getCarry()) {
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
    0xd6 to { A = SUB(A, RAM.getByteAt(PC++)); 8 },
    0xd7 to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = 0x10
      16
    },
    0xd8 to {
      if (getCarry()) {
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
      if (getCarry()) {
        PC = getNextWord()
        16
      } else {
        PC = PC + 2
        12
      }
    },
    0xdc to {
      if (getCarry()) {
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
    0xde to { A = SBC(A, RAM.getByteAt(PC++)); 8 },
    0xdf to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = 0x18
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
    0xe6 to { A = AND(A, RAM.getByteAt(PC++)); 8 },
    0xe7 to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = 0x20
      16
    },
    0xe8 to {
      val offset = RAM.getByteAt(PC++).toByte()
      val uoffset = RAM.getByteAt(PC++)
      val (_, l) = splitHighByteLowByte(SP)
      ADD(l, uoffset)
      SP = ADD(SP, offset.toInt())
      setZero(false)
      setSubstract(false)
      16
    },
    0xe9 to { PC = HL; 4 },
    0xea to { RAM.setByteAt(getNextWord(), A); 16 },
    0xee to { A = XOR(A, RAM.getByteAt(PC++)); 8 },
    0xef to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = 0x28
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
    0xf6 to { A = OR(A, RAM.getByteAt(PC++)); 8 },
    0xf7 to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = 0x30
      16
    },
    0xf8 to {
      val offset = RAM.getByteAt(PC++).toByte()
      val uoffset = RAM.getByteAt(PC++)
      val (_, l) = splitHighByteLowByte(SP)
      HL = ADD(SP, offset.toInt())
      ADD(l, uoffset)
      setZero(false)
      setSubstract(false)
      12
    },
    0xf9 to { SP = HL; 8 },
    0xfa to {
      val addr = getNextWord()
      val newA = RAM.getByteAt(addr)
      A = newA
      16
    },
    0xfb to { interrupts = true; 4 },
    0xfe to { SUB(A, RAM.getByteAt(PC++)); 8 },
    0xff to {
      RAM.setByteAt(SP - 1, PCh)
      RAM.setByteAt(SP - 2, PCl)
      SP = SP - 2
      PC = 0x0038
      16
    }
  )
  internal val prefixOpcodes: Map<Int, ()->Int> = mapOf(
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
    0x40 to { setZero(BIT(B, 0) == '1'); 8 },
    0x41 to { setZero(BIT(C, 0) == '1'); 8 },
    0x42 to { setZero(BIT(D, 0) == '1'); 8 },
    0x43 to { setZero(BIT(E, 0) == '1'); 8 },
    0x44 to { setZero(BIT(H, 0) == '1'); 8 },
    0x45 to { setZero(BIT(L, 0) == '1'); 8 },
    0x46 to { setZero(BIT(RAM.getByteAt(HL), 0) == '1'); 12 },
    0x47 to { setZero(BIT(A, 0) == '1'); 8 },
    0x48 to { setZero(BIT(B, 1) == '1'); 8 },
    0x49 to { setZero(BIT(C, 1) == '1'); 8 },
    0x4a to { setZero(BIT(D, 1) == '1'); 8 },
    0x4b to { setZero(BIT(E, 1) == '1'); 8 },
    0x4c to { setZero(BIT(H, 1) == '1'); 8 },
    0x4d to { setZero(BIT(L, 1) == '1'); 8 },
    0x4e to { setZero(BIT(RAM.getByteAt(HL), 1) == '1'); 12 },
    0x4f to { setZero(BIT(A, 1) == '1'); 8 },
    0x50 to { setZero(BIT(B, 2) == '1'); 8 },
    0x51 to { setZero(BIT(C, 2) == '1'); 8 },
    0x52 to { setZero(BIT(D, 2) == '1'); 8 },
    0x53 to { setZero(BIT(E, 2) == '1'); 8 },
    0x54 to { setZero(BIT(H, 2) == '1'); 8 },
    0x55 to { setZero(BIT(L, 2) == '1'); 8 },
    0x56 to { setZero(BIT(RAM.getByteAt(HL), 2) == '1'); 12 },
    0x57 to { setZero(BIT(A, 2) == '1'); 8 },
    0x58 to { setZero(BIT(B, 3) == '1'); 8 },
    0x59 to { setZero(BIT(C, 3) == '1'); 8 },
    0x5a to { setZero(BIT(D, 3) == '1'); 8 },
    0x5b to { setZero(BIT(E, 3) == '1'); 8 },
    0x5c to { setZero(BIT(H, 3) == '1'); 8 },
    0x5d to { setZero(BIT(L, 3) == '1'); 8 },
    0x5e to { setZero(BIT(RAM.getByteAt(HL), 3) == '1'); 12 },
    0x5f to { setZero(BIT(A, 3) == '1'); 8 },
    0x60 to { setZero(BIT(B, 4) == '1'); 8 },
    0x61 to { setZero(BIT(C, 4) == '1'); 8 },
    0x62 to { setZero(BIT(D, 4) == '1'); 8 },
    0x63 to { setZero(BIT(E, 4) == '1'); 8 },
    0x64 to { setZero(BIT(H, 4) == '1'); 8 },
    0x65 to { setZero(BIT(L, 4) == '1'); 8 },
    0x66 to { setZero(BIT(RAM.getByteAt(HL), 4) == '1'); 12 },
    0x67 to { setZero(BIT(A, 4) == '1'); 8 },
    0x68 to { setZero(BIT(B, 5) == '1'); 8 },
    0x69 to { setZero(BIT(C, 5) == '1'); 8 },
    0x6a to { setZero(BIT(D, 5) == '1'); 8 },
    0x6b to { setZero(BIT(E, 5) == '1'); 8 },
    0x6c to { setZero(BIT(H, 5) == '1'); 8 },
    0x6d to { setZero(BIT(L, 5) == '1'); 8 },
    0x6e to { setZero(BIT(RAM.getByteAt(HL), 5) == '1'); 12 },
    0x6f to { setZero(BIT(A, 5) == '1'); 8 },
    0x70 to { setZero(BIT(B, 6) == '1'); 8 },
    0x71 to { setZero(BIT(C, 6) == '1'); 8 },
    0x72 to { setZero(BIT(D, 6) == '1'); 8 },
    0x73 to { setZero(BIT(E, 6) == '1'); 8 },
    0x74 to { setZero(BIT(H, 6) == '1'); 8 },
    0x75 to { setZero(BIT(L, 6) == '1'); 8 },
    0x76 to { setZero(BIT(RAM.getByteAt(HL), 6) == '1'); 12 },
    0x77 to { setZero(BIT(A, 6) == '1'); 8 },
    0x78 to { setZero(BIT(B, 7) == '1'); 8 },
    0x79 to { setZero(BIT(C, 7) == '1'); 8 },
    0x7a to { setZero(BIT(D, 7) == '1'); 8 },
    0x7b to { setZero(BIT(E, 7) == '1'); 8 },
    0x7c to { setZero(BIT(H, 7) == '1'); 8 },
    0x7d to { setZero(BIT(L, 7) == '1'); 8 },
    0x7e to { setZero(BIT(RAM.getByteAt(HL), 7) == '1'); 12 },
    0x7f to { setZero(BIT(A, 7) == '1'); 8 },
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
  internal fun INT(addr: Int) {
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
      //println("Interrupt ${ints.toString(2)} ${intsEnabled.toString(2)}")
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

  @Synchronized override fun toString() = """
CPU state
PC: ${(PC - 1).toString(16)}
SP: ${SP.toString(16)}
A: ${A.toString(16)} B: ${B.toString(16)} C: ${C.toString(16)} D: ${D.toString(16)}
E: ${E.toString(16)} H: ${H.toString(16)} L: ${L.toString(16)}
F:          ${F.toString(2).padStart(8, '0')}
HL: ${HL.toString(16).padStart(4, '0')} = $HL
BC: ${BC.toString(16)}
Running: $running
Int: $interrupts
Prefix: $prefix
CurrentOP: ${op.toString(16)}
"""

  fun tick(): Int {
    if (running) {
      op = RAM.getByteAt(PC++).toInt()
      if (print) {
        println(CPU)
        print("RAM Adresse (Hex) eingeben zum inspizieren: ")
        val printOp = readLine()
        if (printOp?.trim() != "") {
          val addr = printOp?.toInt(16) ?: 0
          println("Addresse: ${addr.toString(16)}: ${RAM.getByteAt(addr).toInt().toString(16)}")
        }
      }
      if (prefix) {
        time += prefixOpcodes.get(op)!!()
        prefix = false
      } else {
        time += opcodes.get(op)!!()
      }
    } else {
      time++
    }
    return time
  }
}
