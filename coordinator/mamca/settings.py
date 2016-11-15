import math
from collections import OrderedDict


class Settings:
    def __init__(self, filename=None):
        self._d = OrderedDict()
        if filename is None:
            self._d['x'] = 4        # количество клеток по x
            self._d['y'] = 4        # количество клеток по y
            self._d['z'] = 1        # количество клеток по z
            self._d['n'] = 4        # число частиц в кольце

            self._d['d'] = 10       # диаметр кольца
            self._d['r'] = 1        # радиус частицы
            self._d['off'] = 0      # расстояние между клетками

            self._d['ms'] = 4.53e5  # константа диполь-диполь
            self._d['kan'] = 8e4    # константа анизотропии
            self._d['jex'] = 0.1    # константа обмена
            self._d['m'] = 1        # значение момента

            self._d['t'] = 0.01     # температура
            self._d['ot'] = 0       # расположение осей анизотропии
            # 0 -- рандом в 3D, 1 -- рандом в 2D, 2 -- заданная ось
            # отклонение оси анизотропии от оси z и оси x соответственно
            self._d['ot_theta'] = math.pi / 2
            self._d['ot_phi'] = 0

            self._d['b_x'] = 0      # поле по x
            self._d['b_y'] = 0      # поле по y
            self._d['b_z'] = 0      # поле по z

            self._d['prec'] = 7
            self._d['load'] = False
        else:
            with open(filename, mode='r') as f:
                def next_int():
                    s = f.readline()
                    while s == '\n':
                        s = f.readline()
                    return int(s.split()[2])

                def next_float():
                    s = f.readline()
                    while s == '\n':
                        s = f.readline()
                    return float(s.split()[2])

                def next_bool():
                    s = f.readline()
                    while s == '\n':
                        s = f.readline()
                    b = s.split()[2]
                    return b == 'True' or b == 'true'

                self._d['x'] = next_int()  # количество клеток по x
                self._d['y'] = next_int()  # количество клеток по y
                self._d['z'] = next_int()  # количество клеток по z
                self._d['n'] = next_int()  # число частиц в кольце

                self._d['d'] = next_float()  # диаметр кольца
                self._d['r'] = next_float()  # радиус частицы
                self._d['off'] = next_float()  # расстояние между клетками

                self._d['ms'] = next_float()  # константа диполь-диполь
                self._d['kan'] = next_float()  # константа анизотропии
                self._d['jex'] = next_float()  # константа обмена
                self._d['m'] = next_float()  # значение момента

                self._d['t'] = next_float()  # температура
                self._d['ot'] = next_int()  # расположение осей анизотропии
                # 0 -- рандом в 3D, 1 -- рандом в 2D, 2 -- заданная ось
                # отклонение оси анизотропии от оси z и оси x соответственно
                self._d['ot_theta'] = next_float()
                self._d['ot_phi'] = next_float()

                self._d['b_x'] = next_float()  # поле по x
                self._d['b_y'] = next_float()  # поле по y
                self._d['b_z'] = next_float()  # поле по z

                self._d['prec'] = next_int()
                self._d['load'] = next_bool()

    @property
    def x(self):
        return self._d['x']

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
    def off(self):
        return self._d['off']

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

    def save_settings(self, filename):
        # проверка на то, что все частицы помещаются в окружность
        if self.n * 2 * self.r > self.d * math.pi:
            raise ValueError('{} particles with {} radius can not be placed'
                             'on the ring of '
                             '{} diameter'.format(self.n, self.r, self.d))
        with open(filename, mode='w') as f:
            for key, value in self._d.items():
                f.write('{} = {}\n'.format(key, value))

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
