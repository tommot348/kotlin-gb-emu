package main

import de.prt.gb.CPU

fun main(args: Array<String>) {
  val cpu = CPU()
  val a = 0b10000000.toString(2).padStart(8, '0') //RLCA
  var F = 0b00000000.toShort()
  when (a.first()) {
    '0' -> F = (F.toInt() and 0b11100000).toShort()
    '1' -> F = (F.toInt() or 0b00010000).toShort()
  }
  val A = (a.substring(1) + a.first()).toShort(2)
  println(A.toString(2))
}
