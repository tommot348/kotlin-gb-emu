package de.prt.gb.ui
import de.prt.gb.hardware.RAM
import java.awt.event.KeyListener
import java.awt.event.KeyEvent
final class Input : KeyListener, IInput {
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
    }
  }
  override fun keyTyped(e: KeyEvent) {}
  override fun getState(mode: Int): Short =
    when (mode) {
      0b01 -> {
        val matrix = listOf(start, select, b, a).map({ if (it) 0 else 1 })
        "1101${matrix.joinToString("")}".toShort(2)
      }
      0b10 -> {
        val matrix = listOf(down, up, left, right).map({ if (it) 0 else 1 })
        "1110${matrix.joinToString("")}".toShort(2)
      }
      else -> 0b11111111.toShort()
    }
}
