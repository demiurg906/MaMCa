"""
That file generated automatically, don't change it
"""

import json
from collections import OrderedDict


class Settings:
    def __init__(self, filename=None):
        self._d = OrderedDict()
        self._d['x'] = 1
        self._d['y'] = 1
        self._d['z'] = 1
        self._d['n'] = 50
        self._d['r'] = 1.5
        self._d['d'] = 60.0
        self._d['offset'] = 4.0
        self._d['m'] = 800.0
        self._d['kan'] = 0.0165
        self._d['jex'] = 5.0
        self._d['dipolDistance'] = 30.0
        self._d['exchangeDistance'] = 4.0
        self._d['viscosity'] = 0.9
        self._d['t'] = 0.0
        self._d['loc'] = 0
        self._d['loc_theta'] = 90.0
        self._d['loc_phi'] = 0.0
        self._d['ot'] = 2
        self._d['ot_theta'] = 90.0
        self._d['ot_phi'] = 0.0
        self._d['b_x'] = 0.0
        self._d['b_y'] = 0.0
        self._d['b_z'] = 0.0
        self._d['time'] = 1.0
        self._d['timeStep'] = 100
        self._d['name'] = 'default'
        self._d['precision'] = 7
        self._d['load'] = False
        self._d['jsonPath'] = './resources/out/sample.json'
        self._d['hysteresis'] = False
        self._d['hysteresisSteps'] = 7
        self._d['hysteresisLogScale'] = 0.1
        self._d['dataFolder'] = './resources/data'
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

    @y.setter
    def y(self, value):
        self._d['y'] = value

    @property
    def z(self):
        return self._d['z']

    @z.setter
    def z(self, value):
        self._d['z'] = value

    @property
    def n(self):
        return self._d['n']

    @n.setter
    def n(self, value):
        self._d['n'] = value

    @property
    def r(self):
        return self._d['r']

    @r.setter
    def r(self, value):
        self._d['r'] = value

    @property
    def d(self):
        return self._d['d']

    @d.setter
    def d(self, value):
        self._d['d'] = value

    @property
    def offset(self):
        return self._d['offset']

    @offset.setter
    def offset(self, value):
        self._d['offset'] = value

    @property
    def m(self):
        return self._d['m']

    @m.setter
    def m(self, value):
        self._d['m'] = value

    @property
    def kan(self):
        return self._d['kan']

    @kan.setter
    def kan(self, value):
        self._d['kan'] = value

    @property
    def jex(self):
        return self._d['jex']

    @jex.setter
    def jex(self, value):
        self._d['jex'] = value

    @property
    def dipolDistance(self):
        return self._d['dipolDistance']

    @dipolDistance.setter
    def dipolDistance(self, value):
        self._d['dipolDistance'] = value

    @property
    def exchangeDistance(self):
        return self._d['exchangeDistance']

    @exchangeDistance.setter
    def exchangeDistance(self, value):
        self._d['exchangeDistance'] = value

    @property
    def viscosity(self):
        return self._d['viscosity']

    @viscosity.setter
    def viscosity(self, value):
        self._d['viscosity'] = value

    @property
    def t(self):
        return self._d['t']

    @t.setter
    def t(self, value):
        self._d['t'] = value

    @property
    def loc(self):
        return self._d['loc']

    @loc.setter
    def loc(self, value):
        self._d['loc'] = value

    @property
    def loc_theta(self):
        return self._d['loc_theta']

    @loc_theta.setter
    def loc_theta(self, value):
        self._d['loc_theta'] = value

    @property
    def loc_phi(self):
        return self._d['loc_phi']

    @loc_phi.setter
    def loc_phi(self, value):
        self._d['loc_phi'] = value

    @property
    def ot(self):
        return self._d['ot']

    @ot.setter
    def ot(self, value):
        self._d['ot'] = value

    @property
    def ot_theta(self):
        return self._d['ot_theta']

    @ot_theta.setter
    def ot_theta(self, value):
        self._d['ot_theta'] = value

    @property
    def ot_phi(self):
        return self._d['ot_phi']

    @ot_phi.setter
    def ot_phi(self, value):
        self._d['ot_phi'] = value

    @property
    def b_x(self):
        return self._d['b_x']

    @b_x.setter
    def b_x(self, value):
        self._d['b_x'] = value

    @property
    def b_y(self):
        return self._d['b_y']

    @b_y.setter
    def b_y(self, value):
        self._d['b_y'] = value

    @property
    def b_z(self):
        return self._d['b_z']

    @b_z.setter
    def b_z(self, value):
        self._d['b_z'] = value

    @property
    def time(self):
        return self._d['time']

    @time.setter
    def time(self, value):
        self._d['time'] = value

    @property
    def timeStep(self):
        return self._d['timeStep']

    @timeStep.setter
    def timeStep(self, value):
        self._d['timeStep'] = value

    @property
    def name(self):
        return self._d['name']

    @name.setter
    def name(self, value):
        self._d['name'] = value

    @property
    def precision(self):
        return self._d['precision']

    @precision.setter
    def precision(self, value):
        self._d['precision'] = value

    @property
    def load(self):
        return self._d['load']

    @load.setter
    def load(self, value):
        self._d['load'] = value

    @property
    def jsonPath(self):
        return self._d['jsonPath']

    @jsonPath.setter
    def jsonPath(self, value):
        self._d['jsonPath'] = value

    @property
    def hysteresis(self):
        return self._d['hysteresis']

    @hysteresis.setter
    def hysteresis(self, value):
        self._d['hysteresis'] = value

    @property
    def hysteresisSteps(self):
        return self._d['hysteresisSteps']

    @hysteresisSteps.setter
    def hysteresisSteps(self, value):
        self._d['hysteresisSteps'] = value

    @property
    def hysteresisLogScale(self):
        return self._d['hysteresisLogScale']

    @hysteresisLogScale.setter
    def hysteresisLogScale(self, value):
        self._d['hysteresisLogScale'] = value

    @property
    def dataFolder(self):
        return self._d['dataFolder']

    @dataFolder.setter
    def dataFolder(self, value):
        self._d['dataFolder'] = value

    def __getitem__(self, key):
        return self._d[key]

    def __setitem__(self, key, value):
        self._d[key] = value

    def save_settings(self, filename):
        with open(filename, mode='w') as f:
            json.dump(self._d, f)

    def __str__(self):
        return ''

