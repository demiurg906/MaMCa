import json
import math
from collections import OrderedDict


class Settings:
    def __init__(self, filename=None):
        self._d = OrderedDict()

        self._d['x'] = 1  # количество клеток по x
        self._d['y'] = 1  # количество клеток по y
        self._d['z'] = 1  # количество клеток по z
        self._d['n'] = 40  # число частиц в кольце

        self._d['r'] = 1.0  # диаметр кольца [нм]
        self._d['d'] = 60.0  # радиус частицы [нм]
        self._d['offset'] = 4/0  # расстояние между клетками [нм]

        self._d['m'] = 1.0  # значение момента [ядерный магнетон, шт]
        self._d['kan'] = 1.0e-2  # константа анизотропии [иДж], основной порядок -- 10^2 - 10^3
        self._d['jex'] = 1.0e2  # константа обмена [Тл^2 / иДж], основной порядок -- 10^-2 - 10^-3

        # расстояния, на которых чувтствуются взаимодействия:
        self._d['dipol_distance'] = 30.0  # диполь-дипольное, [нм]
        self._d['exchange_distance'] = 3.0  # обменное, [нм]

        self._d['viscosity'] = 1  # коэффициент вязкости, <= 1
        self._d['t'] = 0.01  # температура [К]

        self._d['ot'] = 0  # расположение осей анизотропии
        # 0 -- рандом в 3D, 1 -- рандом в 2D, 2 -- заданная ось
        # отклонение оси анизотропии от оси z и оси x соответственно
        self._d['ot_theta'] = 90.0  # [градус]
        self._d['ot_phi'] = 0  # [градус]

        self._d['b_x'] = 0  # поле по x [Тл]
        self._d['b_y'] = 0  # поле по y [Тл]
        self._d['b_z'] = 0  # поле по z [Тл]

        self._d['precision'] = 7  # точность (количтество шагов симуляции)
        self._d['load'] = False  # загружать ли предыдущее состояние
        self._d['jsonPath'] = 'sample.json'
        if filename is not None:
            with open(filename) as f:
                d = json.load(f)
                for key, value in d.items():
                    self._d[key] = value

    @property
    def x(self):
        return self._d['x']

    @x.setter
    def x(self, value):
        self._d['x'] = value

    @property
    def y(self):
        return self._d['y']

    @property
    def z(self):
        return self._d['z']

    @property
    def n(self):
        return self._d['n']

    @property
    def d(self):
        return self._d['d']

    @property
    def r(self):
        return self._d['r']

    @property
    def offset(self):
        return self._d['offset']

    @property
    def kan(self):
        return self._d['kan']

    @property
    def jex(self):
        return self._d['jex']

    @property
    def m(self):
        return self._d['m']

    @property
    def dipol_distance(self):
        return self._d['dipol_distance']

    @property
    def exchange_distance(self):
        return self._d['exchange_distance']

    @property
    def t(self):
        return self._d['t']

    @property
    def ot(self):
        return self._d['ot']

    @property
    def ot_theta(self):
        return self._d['ot_theta']

    @property
    def ot_phi(self):
        return self._d['ot_phi']

    @property
    def b_x(self):
        return self._d['b_x']

    @property
    def b_y(self):
        return self._d['b_y']

    @property
    def b_z(self):
        return self._d['b_z']

    @property
    def prec(self):
        return self._d['prec']

    @property
    def load(self):
        return self._d['load']

    @property
    def jsonPath(self):
        return self._d['jsonPath']

    def save_settings(self, filename):
        # проверка на то, что все частицы помещаются в окружность
        if self.n * 2 * self.r > self.d * math.pi:
            raise ValueError('{} particles with {} radius can not be placed'
                             'on the ring of '
                             '{} diameter'.format(self.n, self.r, self.d))
        with open(filename, mode='w') as f:
            json.dump(self._d, f)

    def __getitem__(self, key):
        return self._d[key]

    def __setitem__(self, key, value):
        self._d[key] = value

    def __str__(self):
        anisotropy = ''
        if self.ot == 0:
            anisotropy += 'random in 3D'
        elif self.ot == 1:
            anisotropy += 'random in 2D'
        else:
            anisotropy += 'theta={:.2f}, phi={:.2f}'.format(self.ot_theta, self.ot_phi)

        return 'n = {}\noff = {}\n' \
               'B = ({:.1e}, {:.1e}, {:.1e})\n\n' \
               'dipol (m) = {:.2e}\nanisotropy (kan) = {:.2e}\n' \
               'exchange (jex) = {:.2e}\n' \
               '\nanisotropy: {}'.format(self.n,
                                         self.offset, self.b_x,
                                         self.b_y, self.b_z,
                                         self.m,
                                         self.kan, self.jex,
                                         anisotropy
                                         )
