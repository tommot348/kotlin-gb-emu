package de.prt.gb.ui
import javax.swing.SwingUtilities
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.BorderFactory
import java.awt.Dimension
import java.awt.Color
import java.awt.Graphics

private final class FrameBuffer : JPanel() {
  val dat = ArrayList<List<Int>>()
  init {
    setBorder(BorderFactory.createLineBorder(Color.black))
    setDoubleBuffered(true)
  }
  override fun getPreferredSize(): Dimension {
    return Dimension(640, 576)
  }
  override fun paintComponent(g: Graphics) {
    super.paintComponent(g)
    dat.forEachIndexed({ y, line ->
      line.forEachIndexed({ x, color ->
        when (color) {
          3 -> g.setColor(Color.BLACK)
          2 -> g.setColor(Color.DARK_GRAY)
          1 -> g.setColor(Color.LIGHT_GRAY)
          0 -> g.setColor(Color.WHITE)
        }
        g.fillRect(x * 4, y * 4, 4, 4)
      })
    })
  }
}
internal final class Display(name: String) : JFrame(name), IDisplay {
  private val fb = FrameBuffer()
  init {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    add(fb)
  }
  @Synchronized override fun update(lines: ArrayList<List<Int>>) {
    if ((! (fb.dat == lines)) && (lines.size != 0)) {
      fb.dat.removeAll({ true })
      fb.dat.addAll(lines)
      repaint()
    }
  }
  override fun showWindow() {
    SwingUtilities.invokeLater(Runnable() {
      setVisible(true)
    })
  }
  override fun hideWindow() {
    SwingUtilities.invokeLater(Runnable() {
      setVisible(false)
    })
  }
}