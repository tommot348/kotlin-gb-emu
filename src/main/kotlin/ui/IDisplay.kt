package de.prt.gb.ui

interface IDisplay {
  fun hideWindow()
  fun showWindow()
  fun update(lines: ArrayList<List<Int>>)
}

