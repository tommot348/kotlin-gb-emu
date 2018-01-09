package de.prt.gb
import javax.swing.SwingUtilities
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.BorderFactory
import java.awt.Dimension
import java.awt.Color
import java.awt.Graphics

private class FrameBuffer : JPanel() {
  var dat = listOf(0)
  init {
    setBorder(BorderFactory.createLineBorder(Color.black))
  }
  override fun getPreferredSize(): Dimension {
    return Dimension(320, 288)
  }
  override fun paintComponent(g: Graphics) {
    super.paintComponent(g)
    dat.forEachIndexed({ i, it ->
      when (it) {
        3 -> g.setColor(Color.BLACK)
        2 -> g.setColor(Color.DARK_GRAY)
        1 -> g.setColor(Color.LIGHT_GRAY)
        0 -> g.setColor(Color.WHITE)
      }
      g.fillRect((i % 160) * 2, (i / 160) * 2, 2, 2)
    })
  }
}
class Display : JFrame("Test") {
  private val fb = FrameBuffer()
  init {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    add(fb)
  }
  fun update(dat: List<Int>) {
    fb.dat = dat
    repaint()
  }
  fun showWindow() {
    SwingUtilities.invokeLater(Runnable() {
      setVisible(true)
    })
  }
  fun hideWindow() {
    SwingUtilities.invokeLater(Runnable() {
      setVisible(false)
    })
  }
}
