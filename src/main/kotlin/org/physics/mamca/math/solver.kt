package org.physics.mamca.math

fun solveEquation(a: Double, b: Double, c: Double): List<Double> {
    val tau = 108*a*b*c
    val alpha = Math.pow(2.0, -1.0/3.0) * Math.pow(
            tau + Math.sqrt(-4*Math.pow(3*sqr(a)-12*sqr(b)-3*sqr(c), 3.0) +
                    sqr(tau)), 1.0/3.0)
    val beta = (sqr(a)-4*sqr(b)-sqr(c)) / (b * alpha) + alpha / (3 * b)
    val gamma = (c - a) / b
    val delta = Math.sqrt(sqr(gamma)/4 + beta)
    val kappa = sqr(gamma)/2 - beta
    val theta = (Math.pow(gamma, 3.0) + 8*(a+c)/b)/(4*delta)
    val phi = Math.sqrt(kappa + theta) / 2
    val chi = Math.sqrt(kappa - theta) / 2
    val mu = -gamma / 4

    return listOf(
            mu - delta / 2 - phi,
            mu - delta / 2 + phi,
            mu + delta / 2 - chi,
            mu + delta / 2 + chi
    ).filterNot(Double::isNaN)
}