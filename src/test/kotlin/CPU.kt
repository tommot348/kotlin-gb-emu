package de.prt.gb.hardware
import kotlin.test.assertEquals
import org.junit.Test

import de.prt.gb.hardware.*

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
      CPU.A = 0b11111111.toShort()
      assertEquals(CPU.AF, 0b1111111100000000)
      CPU.B = 0b10101010.toShort()
      assertEquals(CPU.BC, 0b1010101000000000)
      CPU.D = 0b01010101.toShort()
      assertEquals(CPU.DE, 0b0101010100000000)
      CPU.H = 0b01010101.toShort()
      assertEquals(CPU.HL, 0b0101010100000000)
      CPU.AF = 0b0000000011111111
      assertEquals(CPU.A, 0.toShort())
      assertEquals(CPU.F, 0b11111111.toShort())
      CPU.BC = 0b0000000011111111
      assertEquals(CPU.B, 0.toShort())
      assertEquals(CPU.C, 0b11111111.toShort())
      CPU.DE = 0b0000000011111111
      assertEquals(CPU.D, 0.toShort())
      assertEquals(CPU.E, 0b11111111.toShort())
      CPU.HL = 0b0000000011111111
      assertEquals(CPU.H, 0.toShort())
      assertEquals(CPU.L, 0b11111111.toShort())
    }
    @Test fun testArithmetic() {
      assertEquals(CPU.A.INC(), 1.toShort())
      assertEquals(255.toShort().INC(), 0.toShort())
      assertEquals(1.toShort().DEC(), 0.toShort())
      assertEquals(0.toShort().DEC(), 255.toShort())
    }
}
