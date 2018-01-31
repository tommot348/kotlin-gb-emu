package de.prt.gb.ui
import javax.swing.SwingUtilities
import javax.swing.JFrame

abstract class ShowableFrame(name: String) : JFrame(name) {
  fun showWindow() {
    SwingUtilities.invokeLater {
      setVisible(true)
    }
  }
  fun hideWindow() {
    SwingUtilities.invokeLater {
      setVisible(false)
    }
  }
}

