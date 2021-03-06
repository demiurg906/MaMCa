﻿package org.physics.mamca

//----------------------
// ФИЗИЧЕСКИЕ КОНСТАНТЫ
//----------------------

// Пи
const val PI = Math.PI
const val PI2 = 2 * PI

// магнитная постоянная
const val MU_0 = 4 * PI *1.0e-7 // [Гн / м]

// коэффициент для диполь-дипольного взаимодействия
const val DIPOL_CONST = MU_0 / (4 * PI) * 1e27 // с учетом того, что расстояния измеряются в [нм], а не в [м]

// магнетон бора
const val MU_B = 927.40096820e-26 // [Дж / Тл]

// постоянная больцмана
const val K = 1.3806485279e-23

//-----------------------
// КОЭФФИЦИЕНТЫ ПЕРЕВОДА
//-----------------------

//  эВ -> Дж
const val EV_TO_DJ = 1.602176620898e-19

//  Дж -> эВ
const val DJ_TO_EV = 1 / EV_TO_DJ

//  c -> нс
const val S_TO_NS = 1e9

//  нc -> с
const val NS_TO_S = 1 / S_TO_NS

//  нм -> м
const val NM_TO_M = 1e-9

//  Гс -> Тл
const val OE_TO_TESLA = 1e-4

//  Тд -> Гс
const val TESLA_TO_OE = 1 / OE_TO_TESLA

// 1 % -> 0.01
const val PERCENT_COEFFICIENT = 0.01

//---------------------
// СЛУЖЕБНЫЕ КОНСТАНТЫ
//---------------------

// точность сравнение чисел с плавающей запятой
const val DELTA = 1e-20

// число символов после запятой для вывода
const val DIGITS = 2

// число символов после запятой для отправки в математику
const val MATH_DIGITS = 10
