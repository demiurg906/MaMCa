package org.physics.mamca

const val PI = Math.PI
const val PI2 = 2 * PI

// точность сравнение чисел с плавающей запятой
const val DELTA = 1e-20

// отночительная точность, при которой считаем, что изменение энергии системы несущественно
// считается, как отношение разности энергий до и после к энергии до
const val RELATIVE_ENERGY_PRECISION = 0.1

// число символов после запятой для вывода
const val DIGITS = 2

// число символов после запятой для отправки в математику
const val MATH_DIGITS = 10

// магнитная постоянная
const val MU_0 = 4 * PI *1.0e-7 // [Гн / м]

// коэффициент для диполь-дипольного взаимодействия
const val DIPOL_CONST = 1e-7 // MU_0 / (4 * PI)

// магнетон бора
const val MU_B = 927.40096820e-26 // [Дж / Тл]

// коэффициент перевода электрон-вольт в джоули
const val EV_TO_DJ = 1.602176620898e-19