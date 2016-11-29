import json
import math
from collections import OrderedDict


class Settings:
    def __init__(self, filename=None):
        self._d = OrderedDict()

        self._d['x'] = 4        # количество клеток по x
        self._d['y'] = 4        # количество клеток по y
        self._d['z'] = 1        # количество клеток по z
        self._d['r'] = 1        # число частиц в кольце

        self._d['n'] = 4       # диаметр кольца
        self._d['d'] = 10        # радиус частицы
        self._d['offset'] = 0      # расстояние между клетками

        self._d['ms'] = 4.53e5  # константа диполь-диполь
        self._d['kan'] = 8e4    # константа анизотропии
        self._d['jex'] = 0.1    # константа обмена
        self._d['m'] = 1        # значение момента
        self._d['viscosity'] = 1  # коэффициент вязкости, <= 1

        self._d['t'] = 0.01     # температура
        self._d['ot'] = 0       # расположение осей анизотропии
        # 0 -- рандом в 3D, 1 -- рандом в 2D, 2 -- заданная ось
        # отклонение оси анизотропии от оси z и оси x соответственно
        self._d['ot_theta'] = math.pi / 2
        self._d['ot_phi'] = 0

        self._d['b_x'] = 0      # поле по x
        self._d['b_y'] = 0      # поле по y
        self._d['b_z'] = 0      # поле по z

        self._d['precision'] = 7
        self._d['load'] = False
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
    def ms(self):
        return self._d['ms']

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
        return '(x, y, z) = ({}, {}, {})\nn = {}\noff = {}\n\n' \
               'B = ({:.1e}, {:.1e}, {:.1e})\n\n' \
               'dipol (ms) = {:.2e}\nanizotropy (kan) = {:.2e}\n' \
               'exchange (jex) = {:.2e}'.format(self._d['x'], self._d['y'],
                                                self._d['z'], self._d['n'],
                                                self._d['off'], self._d['b_x'],
                                                self._d['b_y'], self._d['b_z'],
                                                self._d['ms'],
                                                self._d['kan'], self._d['jex']
                                                )
