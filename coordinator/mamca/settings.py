"""
That file generated automatically, don't change it
"""

import json
from collections import OrderedDict


class Settings:
    def __init__(self, filename=None):
        self._d = OrderedDict()
        self._d['x'] = 70
        self._d['y'] = 70
        self._d['z'] = 1
        self._d['n'] = 1
        self._d['r'] = 1.25
        self._d['d'] = 0.0
        self._d['offset_x'] = 3.0
        self._d['offset_y'] = 3.0
        self._d['offset_z'] = 3.0
        self._d['m'] = 456.0
        self._d['kan'] = 80000.0
        self._d['jex'] = 5.0
        self._d['dipolDistance'] = 30.0
        self._d['exchangeDistance'] = 3.1
        self._d['viscosity'] = 0.5
        self._d['t'] = 0.0
        self._d['loc'] = 0
        self._d['loc_theta'] = 90.0
        self._d['loc_phi'] = 0.0
        self._d['ot'] = 0
        self._d['ot_theta'] = 90.0
        self._d['ot_phi'] = 0.0
        self._d['b_x'] = 0.0
        self._d['b_y'] = 0.0
        self._d['b_z'] = 0.0
        self._d['time'] = 1.0
        self._d['timeStep'] = 100
        self._d['cyclicBoundaries'] = False
        self._d['name'] = 'default'
        self._d['precision'] = 10000
        self._d['relative_precision'] = 0.01
        self._d['load'] = False
        self._d['jsonPath'] = './resources/data/default/out/sample.json'
        self._d['hysteresis'] = False
        self._d['hysteresisSteps'] = 7
        self._d['hysteresisDenseSteps'] = 2
        self._d['hysteresisDenseMultiplier'] = 2
        self._d['is2dPlot'] = True
        self._d['xAxis'] = 'x'
        self._d['yAxis'] = 'y'
        self._d['borders'] = True
        self._d['leftX'] = 20
        self._d['rightX'] = 40
        self._d['leftY'] = 20
        self._d['rightY'] = 40
        self._d['dataFolder'] = '../data'
        self._d['isParallel'] = False
        self._d['memory'] = 6144
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
    def offset_x(self):
        return self._d['offset_x']

    @offset_x.setter
    def offset_x(self, value):
        self._d['offset_x'] = value

    @property
    def offset_y(self):
        return self._d['offset_y']

    @offset_y.setter
    def offset_y(self, value):
        self._d['offset_y'] = value

    @property
    def offset_z(self):
        return self._d['offset_z']

    @offset_z.setter
    def offset_z(self, value):
        self._d['offset_z'] = value

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
    def cyclicBoundaries(self):
        return self._d['cyclicBoundaries']

    @cyclicBoundaries.setter
    def cyclicBoundaries(self, value):
        self._d['cyclicBoundaries'] = value

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
    def relative_precision(self):
        return self._d['relative_precision']

    @relative_precision.setter
    def relative_precision(self, value):
        self._d['relative_precision'] = value

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
    def hysteresisDenseSteps(self):
        return self._d['hysteresisDenseSteps']

    @hysteresisDenseSteps.setter
    def hysteresisDenseSteps(self, value):
        self._d['hysteresisDenseSteps'] = value

    @property
    def hysteresisDenseMultiplier(self):
        return self._d['hysteresisDenseMultiplier']

    @hysteresisDenseMultiplier.setter
    def hysteresisDenseMultiplier(self, value):
        self._d['hysteresisDenseMultiplier'] = value

    @property
    def is2dPlot(self):
        return self._d['is2dPlot']

    @is2dPlot.setter
    def is2dPlot(self, value):
        self._d['is2dPlot'] = value

    @property
    def xAxis(self):
        return self._d['xAxis']

    @xAxis.setter
    def xAxis(self, value):
        self._d['xAxis'] = value

    @property
    def yAxis(self):
        return self._d['yAxis']

    @yAxis.setter
    def yAxis(self, value):
        self._d['yAxis'] = value

    @property
    def borders(self):
        return self._d['borders']

    @borders.setter
    def borders(self, value):
        self._d['borders'] = value

    @property
    def leftX(self):
        return self._d['leftX']

    @leftX.setter
    def leftX(self, value):
        self._d['leftX'] = value

    @property
    def rightX(self):
        return self._d['rightX']

    @rightX.setter
    def rightX(self, value):
        self._d['rightX'] = value

    @property
    def leftY(self):
        return self._d['leftY']

    @leftY.setter
    def leftY(self, value):
        self._d['leftY'] = value

    @property
    def rightY(self):
        return self._d['rightY']

    @rightY.setter
    def rightY(self, value):
        self._d['rightY'] = value

    @property
    def dataFolder(self):
        return self._d['dataFolder']

    @dataFolder.setter
    def dataFolder(self, value):
        self._d['dataFolder'] = value

    @property
    def isParallel(self):
        return self._d['isParallel']

    @isParallel.setter
    def isParallel(self, value):
        self._d['isParallel'] = value

    @property
    def memory(self):
        return self._d['memory']

    @memory.setter
    def memory(self, value):
        self._d['memory'] = value

    def __getitem__(self, key):
        return self._d[key]

    def __setitem__(self, key, value):
        self._d[key] = value

    def save_settings(self, filename):
        with open(filename, mode='w') as f:
            json.dump(self._d, f)

    def __str__(self):
        return ''

