package de.prt.gb.hardware
internal object SERIAL {
  fun out(dat: Short) {
    print(dat.toChar())
  }
}
