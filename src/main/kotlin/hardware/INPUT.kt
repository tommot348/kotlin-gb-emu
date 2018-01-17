package de.prt.gb.hardware
import java.awt.event.KeyListener
import java.awt.event.KeyEvent
object Input : KeyListener {
  private var left = false
  private var right = false
  private var up = false
  private var down = false
  private var start = false
  private var select = false
  private var a = false
  private var b = false
  override fun keyPressed(e: KeyEvent) {
    when (e.getKeyCode()) {
      KeyEvent.VK_LEFT -> left = true
      KeyEvent.VK_RIGHT -> right = true
      KeyEvent.VK_UP -> up = true
      KeyEvent.VK_DOWN -> down = true
      KeyEvent.VK_ENTER -> start = true
      KeyEvent.VK_SPACE -> select = true
      KeyEvent.VK_A -> a = true
      KeyEvent.VK_Y -> b = true
    }
    if (true in listOf(a, b, select, start, up, down, left, right)) {
      val interruptFlags = RAM.getByteAt(0xFF0F)
      RAM.setByteAt(
            0xFF0F,
            (interruptFlags.toInt() or 0b00010000).toShort(),
            true)
    }
  }
  override fun keyReleased(e: KeyEvent) {
    when (e.getKeyCode()) {
      KeyEvent.VK_LEFT -> left = false
      KeyEvent.VK_RIGHT -> right = false
      KeyEvent.VK_UP -> up = false
      KeyEvent.VK_DOWN -> down = false
      KeyEvent.VK_ENTER -> start = false
      KeyEvent.VK_SPACE -> select = false
      KeyEvent.VK_A -> a = false
      KeyEvent.VK_Y -> b = false
      KeyEvent.VK_P -> println(CPU)
      KeyEvent.VK_S -> CPU.running = !CPU.running
    }
  }
  override fun keyTyped(e: KeyEvent) {}
  fun getState(mode: Int): Short =
    when (mode) {
      0b01 -> {
        val matrix = listOf(start, select, b, a).map({ if (it) 0 else 1 })
        "0001${matrix.joinToString("")}".toShort(2)
      }
      0b10 -> {
        val matrix = listOf(down, up, left, right).map({ if (it) 0 else 1 })
        "0010${matrix.joinToString("")}".toShort(2)
      }
      else -> 0b00001111.toShort()
    }
}
