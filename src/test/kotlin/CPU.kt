package de.prt.gb.hardware
import kotlin.test.assertEquals
import org.junit.Test

class TestCPU {
    @Test fun testHelpers() {
      assertEquals(
          Pair(0b11111111.toShort(), 0.toShort()),
          CPU.splitHighByteLowByte(0b1111111100000000))
      assertEquals(
          0b1111111100000000,
          CPU.joinHighByteLowByte(0b11111111.toShort(), 0.toShort()))
    }
    @Test fun testRegisters() {
      CPU.A = 0b11111111
      CPU.F = 0
      assertEquals(0b1111111100000000, CPU.AF)
      CPU.B = 0b10101010
      assertEquals(0b1010101000000000, CPU.BC)
      CPU.D = 0b01010101
      assertEquals(0b0101010100000000, CPU.DE)
      CPU.H = 0b01010101
      assertEquals(0b0101010100000000, CPU.HL)
      CPU.AF = 0b0000000011111111
      assertEquals(0, CPU.A)
      assertEquals(0b11110000, CPU.F)
      CPU.BC = 0b0000000011111111
      assertEquals(0, CPU.B)
      assertEquals(0b11111111, CPU.C)
      CPU.DE = 0b0000000011111111
      assertEquals(0, CPU.D)
      assertEquals(0b11111111, CPU.E)
      CPU.HL = 0b0000000011111111
      assertEquals(0, CPU.H)
      assertEquals(0b11111111, CPU.L)
      CPU.SP = 0xFFFE
      assertEquals(0xFF, CPU.SPh)
      assertEquals(0xFE, CPU.SPl)
      CPU.SP = 0xEFFE
      assertEquals(0xEF, CPU.SPh)
      assertEquals(0xFE, CPU.SPl)
    }
    @Test fun testALU() {
      assertEquals(1, CPU.INC_16(0, 0))
      assertEquals(1, CPU.INC(0.toShort()))
      assertEquals(0, CPU.INC(255.toShort()))
      assertEquals(0, CPU.INC_16(0xFF, 0xFF))

      assertEquals(0, CPU.DEC(1.toShort()))
      assertEquals(0, CPU.DEC_16(0, 1))
      assertEquals(255, CPU.DEC(0.toShort()))
      assertEquals(0xFFFF, CPU.DEC_16(0, 0))

      assertEquals(3.toShort(), CPU.ADD(0.toShort(), 3.toShort()))
      assertEquals(2.toShort(), CPU.ADD(255.toShort(), 3.toShort()))
      assertEquals(254.toShort(), CPU.ADD(255.toShort(), 255.toShort()))
      assertEquals(253.toShort(), CPU.SUB(0.toShort(), 3.toShort()))
      assertEquals(0.toShort(), CPU.SUB(3.toShort(), 3.toShort()))

      CPU.setCarry(true)
      assertEquals(2, CPU.ADC(0, 1))
      CPU.setCarry(true)
      assertEquals(255.toShort(), CPU.ADC(255.toShort(), 255.toShort()))
      CPU.setCarry(true)
      assertEquals(1, CPU.ADC(255, 1))
      CPU.setCarry(true)
      assertEquals(254, CPU.SBC(0, 1))
      CPU.setCarry(true)
      assertEquals(0, CPU.SBC(2, 1))

      assertEquals(0, CPU.AND(0b11000011, 0b111100))
      assertEquals(0b111100, CPU.AND(0b111111, 0b111100))
      assertEquals(0b11111111, CPU.OR(0b11000011, 0b111100))
      assertEquals(0b111100, CPU.OR(0, 0b111100))
      assertEquals(0b11001100, CPU.XOR(0b11110000, 0b00111100))
      assertEquals(0, CPU.XOR(0b11111111, 0b11111111))
      CPU.HL_ADD(1, 1)
      assertEquals(0x0101, CPU.HL)
      CPU.HL_ADD(0xFF, 0xFF)
      assertEquals(0x0100, CPU.HL)
      assertEquals(true, CPU.getHalfCarry())
      assertEquals(true, CPU.getCarry())
      CPU.HL = 0xFFFF
      CPU.HL_ADD(0xFF, 0xFF)
      assertEquals(0xFFFE, CPU.HL)
      CPU.HL = 0
      val (h, l) = CPU.SP_ADD_OFFSET(1.toByte())
      CPU.SPh = h
      CPU.SPl = l
      assertEquals(0x1, CPU.SP)
      val (nh, nl) = CPU.SP_ADD_OFFSET((-1).toByte())
      CPU.SPh = nh
      CPU.SPl = nl
      assertEquals(0, CPU.SP)
    }
    @Test fun testRotateShift() {
      CPU.setCarry(false)
      assertEquals(0b10, CPU.rl(0b1))
      assertEquals(0b0, CPU.rl(0b10000000))
      assertEquals(0b1, CPU.rl(0b0))
      CPU.setCarry(true)
      assertEquals(0b11, CPU.rl(0b1))
      assertEquals(false, CPU.getCarry())
      assertEquals(0b10, CPU.rlc(0b1))
      assertEquals(0b1, CPU.rlc(0b10000000))
      assertEquals(0, CPU.rlc(0b0))
      CPU.setCarry(false)
      assertEquals(0b0, CPU.rr(0b1))
      assertEquals(0b10000000, CPU.rr(0b0))
      assertEquals(0b01000000, CPU.rr(0b10000000))
      assertEquals(0b00100000, CPU.rr(0b01000000))
      assertEquals(0b10000000, CPU.rrc(0b1))
      assertEquals(0b01000000, CPU.rrc(0b10000000))
      assertEquals(0, CPU.rrc(0b0))
      assertEquals(0b10, CPU.sla(1))
      assertEquals(0, CPU.sla(0b10000000))
      assertEquals(0b11000000, CPU.sra(0b10000000))
      assertEquals(0b00100000, CPU.sra(0b01000000))
      assertEquals(0b1, CPU.srl(0b10))
      assertEquals(0b0, CPU.srl(0b1))
    }
    @Test fun testBitTwiddling() {
      assertEquals(0b1111, CPU.swap(0b11110000))
      assertEquals(false, CPU.BIT(0b10000000, 7))
      assertEquals(0b10000000, CPU.SET(0, 7))
      assertEquals(0, CPU.RES(0b10000000, 7))
    }
}
