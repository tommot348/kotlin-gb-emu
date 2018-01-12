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
      assertEquals(CPU.AF, 0b1111111100000000)
      CPU.B = 0b10101010
      assertEquals(CPU.BC, 0b1010101000000000)
      CPU.D = 0b01010101
      assertEquals(CPU.DE, 0b0101010100000000)
      CPU.H = 0b01010101
      assertEquals(CPU.HL, 0b0101010100000000)
      CPU.AF = 0b0000000011111111
      assertEquals(CPU.A, 0)
      assertEquals(CPU.F, 0b11111111)
      CPU.BC = 0b0000000011111111
      assertEquals(CPU.B, 0)
      assertEquals(CPU.C, 0b11111111)
      CPU.DE = 0b0000000011111111
      assertEquals(CPU.D, 0)
      assertEquals(CPU.E, 0b11111111)
      CPU.HL = 0b0000000011111111
      assertEquals(CPU.H, 0)
      assertEquals(CPU.L, 0b11111111)
    }
    @Test fun testALU() {
      assertEquals(CPU.INC(0), 1)
      assertEquals(CPU.INC(0.toShort()), 1)
      assertEquals(CPU.INC(255.toShort()), 0)
      assertEquals(CPU.INC(0xFFFF), 0)

      assertEquals(CPU.DEC(1.toShort()), 0)
      assertEquals(CPU.DEC(1), 0)
      assertEquals(CPU.DEC(0.toShort()), 255)
      assertEquals(CPU.DEC(0), 0xFFFF)

      assertEquals(CPU.ADD(0.toShort(), 3.toShort()), 3.toShort())
      assertEquals(CPU.ADD(255.toShort(), 3.toShort()), 2.toShort())
      assertEquals(CPU.SUB(0.toShort(), 3.toShort()), 253.toShort())
      assertEquals(CPU.SUB(3.toShort(), 3.toShort()), 0.toShort())

      CPU.setCarry(true)
      assertEquals(CPU.ADC(0, 1), 2)
      CPU.setCarry(true)
      assertEquals(CPU.ADC(255, 1), 1)
      CPU.setCarry(true)
      assertEquals(CPU.SBC(0, 1), 254)
      CPU.setCarry(true)
      assertEquals(CPU.SBC(2, 1), 0)

      assertEquals(CPU.AND(0b11000011, 0b111100), 0)
      assertEquals(CPU.AND(0b111111, 0b111100), 0b111100)
      assertEquals(CPU.OR(0b11000011, 0b111100), 0b11111111)
      assertEquals(CPU.OR(0, 0b111100), 0b111100)
      assertEquals(CPU.XOR(0b11110000, 0b00111100), 0b11001100)
      assertEquals(CPU.XOR(0b11111111, 0b11111111), 0)
    }
    @Test fun testRotateShift() {
      CPU.setCarry(false)
      assertEquals(CPU.rl(0b1), 0b10)
      assertEquals(CPU.rl(0b10000000), 0b0)
      assertEquals(CPU.rl(0b0), 0b1)
      assertEquals(CPU.rlc(0b1), 0b10)
      assertEquals(CPU.rlc(0b10000000), 0b1)
      assertEquals(CPU.rlc(0b0), 0)
      CPU.setCarry(false)
      assertEquals(CPU.rr(0b1), 0b0)
      assertEquals(CPU.rr(0b0), 0b10000000)
      assertEquals(CPU.rr(0b10000000), 0b01000000)
      assertEquals(CPU.rrc(0b1), 0b10000000)
      assertEquals(CPU.rrc(0b10000000), 0b01000000)
      assertEquals(CPU.rrc(0b0), 0)
      assertEquals(CPU.sla(1), 0b10)
      assertEquals(CPU.sra(0b10000000), 0b11000000)
      assertEquals(CPU.srl(0b10), 0b1)
    }
}
