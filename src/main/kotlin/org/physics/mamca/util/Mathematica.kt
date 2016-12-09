package org.physics.mamca.util

import com.wolfram.jlink.Expr
import com.wolfram.jlink.KernelLink
import com.wolfram.jlink.MathLinkFactory
import java.util.*

object Mathematica {
    private val m: KernelLink

    init {
        val prop = Properties()
        prop.load(Thread.currentThread().contextClassLoader.getResourceAsStream("config.properties"))
        val pathToMathematica = prop.getProperty("MATHEMATICA_PATH")
        m = MathLinkFactory.createKernelLink("-linkmode launch -linkname '$pathToMathematica'") // подключаем ядро
        m.discardAnswer()
    }

    fun findRoots(expr: String): List<Double> {
        val formattedExpr = expr.replace("e", "*^", true)
        m.evaluate("x /. NSolve[$formattedExpr, x, Reals]")
        m.waitForAnswer()
        val exprRoots = m.expr
        val roots: List<Double> = exprRoots.args().map(Expr::asDouble)
        return roots
    }
}