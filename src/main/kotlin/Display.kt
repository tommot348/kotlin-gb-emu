package de.prt.gb
import javax.swing.SwingUtilities
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.BorderFactory
import java.awt.Dimension
import java.awt.Color
import java.awt.Graphics

class FrameBuffer : JPanel() {
  var dat: List<Int>
  init {
    setBorder(BorderFactory.createLineBorder(Color.black))
  }
  override fun getPreferredSize(): Dimension {
    return Dimension(160, 144)
  }
  override fun paintComponent(g: Graphics) {
    super.paintComponent(g)
  }
}
class Display : JFrame("Test") {
  val fb = FrameBuffer()
  init {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    add(fb)
  }
  fun showWindow() {
    SwingUtilities.invokeLater(Runnable() {
      show()
    })
  }
  fun hideWindow() {
    SwingUtilities.invokeLater(Runnable() {
      hide()
    })
  }
}
