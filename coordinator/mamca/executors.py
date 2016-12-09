import math
import os
import sys
import subprocess

from functools import wraps

from .default_names import get_default_settings_file, get_default_mamca_path, \
    get_default_out_folder
from .settings import Settings


#
def pre_clean(func):
    """
    Декоратор, подготавлявающий выходную папку.
        если ее нет, и очищает ее, если в ней что-то есть
    """
    @wraps(func)
    def inner(*args, **kwargs):
        if kwargs.get('settings_fname', None) is None:
            kwargs['settings_fname'] = get_default_settings_file()
        if kwargs.get('out_folder', None) is None:
            kwargs['out_folder'] = get_default_out_folder()
        if kwargs.get('mamca_path', None) is None:
            kwargs['mamca_path'] = get_default_mamca_path()
        try:
            out = kwargs['out_folder']
        except KeyError:
            out = get_default_out_folder()
        if not os.path.exists(out):
            os.mkdir(out)
        settings = Settings(kwargs['settings_fname'])
        if settings.load:
            sample_file = settings.jsonPath.split('/')[-1]
        else:
            sample_file = ''
        _clear_out_folder(out, sample_file)
        func(*args, **kwargs)
        # dumpedState = '{}/sample.json'.format(out)
        # if os.path.exists(dumpedState):
        #     os.remove(dumpedState)
    return inner


@pre_clean
def single_run(settings_fname=None, out_folder=None, mamca_path=None):
    """
    Запускает однократное моделирование
    :param settings_fname: путь к файлу с настройками
    :param out_folder: путь к выходной папке
    :param mamca_path: путь к исполняемому файлу моделирующей программы
    """
    print('Single run just started')
    subprocess.run(
        'java -jar {} {} {}'.format(mamca_path, settings_fname, out_folder),
        stdout=sys.stdout, stderr=sys.stderr)
    print('Single run has finished')


@pre_clean
def hysteresis_log_run(k=3, n=None, settings_fname=None, out_folder=None,
                       mamca_path=None, min_log_scale=0.1, prec=1):
    """
    Запускает моделирование петли гистерезиса с показательным шагом
    :param k: количество линейных шагов
    :param n: количество нелинейных шагов
    :param settings_fname: путь к файлу с настройками
    :param out_folder: путь к выходной папке
    :param mamca_path: путь к исполняемому файлу моделирующей программы
    :param min_log_scale: доля линейной области от всего диапазона поля
    :param prec: точность вычисления
    """
    if n is None:
        s = 1 + 4 / k
        n = round(2.5 * math.log(1 / min_log_scale, s))
    number_of_steps = 2 * (n + k) - 1
    print('Hysteresis cycle started')
    print('There will be {} steps'.format(int(5 * number_of_steps / 2)))
    s = Settings(settings_fname)
    s['load'] = False
    temp_settings_fname = out_folder + '/temp_settings.txt'

    max_bx = s['b_x']
    max_by = s['b_y']
    max_bz = s['b_z']

    min_log_bx = s['b_x'] * min_log_scale
    min_log_by = s['b_y'] * min_log_scale
    min_log_bz = s['b_z'] * min_log_scale

    bx_lin_step = min_log_bx / k
    by_lin_step = min_log_by / k
    bz_lin_step = min_log_bz / k

    if min_log_bx == 0:
        min_log_bx = 1
    if min_log_by == 0:
        min_log_by = 1
    if min_log_bz == 0:
        min_log_bz = 1

    bx_log_step = (s['b_x'] / min_log_bx) ** (1 / (n - 1))
    by_log_step = (s['b_y'] / min_log_by) ** (1 / (n - 1))
    bz_log_step = (s['b_z'] / min_log_bz) ** (1 / (n - 1))

    if bx_log_step == 0:
        bx_log_step = 1
    if by_log_step == 0:
        by_log_step = 1
    if bz_log_step == 0:
        bz_log_step = 1

    def step(i, inc):
        if i < n - 1:
            s['visc'] = 0.5
            s['b_x'] /= bx_log_step
            s['b_y'] /= by_log_step
            s['b_z'] /= bz_log_step
        elif n - 1 <= i < n - 1 + 2 * k:
            s['visc'] = 0.05
            op = lambda x, y: x + y if inc else x - y
            s['b_x'] = op(s['b_x'], bx_lin_step)
            s['b_y'] = op(s['b_y'], by_lin_step)
            s['b_z'] = op(s['b_z'], bz_lin_step)
        else:
            s['visc'] = 0.5
            s['b_x'] *= bx_log_step
            s['b_y'] *= by_log_step
            s['b_z'] *= bz_log_step

    s['b_x'] = 0
    s['b_y'] = 0
    s['b_z'] = 0
    s.save_settings(temp_settings_fname)
    s['prec'] = prec
    s['load'] = True

    print('Magnetic field began to increase')
    print('Wait {} steps'.format(number_of_steps // 2))
    for i in range(number_of_steps // 2, number_of_steps):
        subprocess.run(
            '{} -s {} -o {} -f {}'.format(mamca_path, temp_settings_fname,
                                          out_folder,
                                          'hyst,{},{},{},{}'.format('fst',
                                                                    s['b_x'],
                                                                    s['b_y'],
                                                                    s['b_z'])),
            stdout=sys.stdout, stderr=sys.stderr)
        step(i, True)
        s.save_settings(temp_settings_fname)
        print('{} of {} steps completed'.format(i + 1 - number_of_steps // 2,
                                                number_of_steps // 2))

    s['b_x'] = max_bx
    s['b_y'] = max_by
    s['b_z'] = max_bz
    s.save_settings(temp_settings_fname)

    print('Magnetic field began to decline')
    print('Wait {} steps'.format(number_of_steps))
    for i in range(number_of_steps):
        subprocess.run(
            '{} -s {} -o {} -f {}'.format(mamca_path, temp_settings_fname,
                                          out_folder,
                                          'hyst,{},{},{},{}'.format('neg',
                                                                    s['b_x'],
                                                                    s['b_y'],
                                                                    s['b_z'])),
            stdout=sys.stdout, stderr=sys.stderr)
        step(i, False)
        s.save_settings(temp_settings_fname)
        print('{} of {} steps completed'.format(i + 1, number_of_steps))

    s['b_x'] = -max_bx
    s['b_y'] = -max_by
    s['b_z'] = -max_bz
    s.save_settings(temp_settings_fname)

    print('Magnetic field began to increase')
    print('Wait another {} steps'.format(number_of_steps))
    for i in range(number_of_steps):
        subprocess.run(
            '{} -s {} -o {} -f {}'.format(mamca_path, temp_settings_fname,
                                          out_folder,
                                          'hyst,{},{},{},{}'.format('pos',
                                                                    s['b_x'],
                                                                    s['b_y'],
                                                                    s['b_z'])),
            stdout=sys.stdout, stderr=sys.stderr)
        step(i, True)
        s.save_settings(temp_settings_fname)
        print('{} of {} steps completed'.format(i + 1, number_of_steps))

    os.remove(temp_settings_fname)
    print('Generation of hysteresis cycle complete')


@pre_clean
def hysteresis_run(hyst_steps, *, settings_fname=None, out_folder=None,
                   mamca_path=None):
    """
    Запускает моделирование петли гистерезиса с линейным шагом (deprecated)
    :param hyst_steps: количество шагов поля на диапазоне от 0 до B_max
    :param settings_fname: путь к файлу с настройками
    :param out_folder: путь к выходной папке
    :param mamca_path: путь к исполняемому файлу моделирующей программы
    :return:
    """
    number_of_steps = 2 * hyst_steps + 1
    print('Hysteresis cycle started')
    print('There will be {} steps'.format(2 * number_of_steps - 1))
    s = Settings(settings_fname)
    s['load'] = False
    temp_settings_fname = 'temp_' + settings_fname
    s.save_settings(temp_settings_fname)
    s['load'] = True
    bx_step = s['b_x'] / hyst_steps
    by_step = s['b_y'] / hyst_steps
    bz_step = s['b_z'] / hyst_steps
    print('Magnetic field began to decline')
    print('Wait {} steps'.format(number_of_steps))
    for i in range(number_of_steps):
        subprocess.run(
            '{} -s {} -o {} -f {}'.format(mamca_path, temp_settings_fname,
                                          out_folder,
                                          'hyst,{},{},{},{}'.format('neg',
                                                                    s['b_x'],
                                                                    s['b_y'],
                                                                    s['b_z'])),
            stdout=sys.stdout, stderr=sys.stderr)
        s['b_x'] -= bx_step
        s['b_y'] -= by_step
        s['b_z'] -= bz_step
        s.save_settings(temp_settings_fname)
        print('{} of {} steps completed'.format(i + 1, number_of_steps))
    print('Magnetic field began to increase')
    print('Wait another {} steps'.format(number_of_steps - 1))
    for i in range(2 * hyst_steps):
        subprocess.run(
            '{} -s {} -o {} -f {}'.format(mamca_path, temp_settings_fname,
                                          out_folder,
                                          'hyst,{},{},{},{}'.format('pos',
                                                                    s['b_x'],
                                                                    s['b_y'],
                                                                    s['b_z'])),
            stdout=sys.stdout, stderr=sys.stderr)
        s['b_x'] += bx_step
        s['b_y'] += by_step
        s['b_z'] += bz_step
        s.save_settings(temp_settings_fname)
        print('{} of {} steps completed'.format(i + 1, number_of_steps - 1))
    os.remove(temp_settings_fname)
    print('Generation of hysteresis cycle complete')


@pre_clean
def cycle_one_parameter(par_name, start, end, step, addition=True,
                        settings_fname=None, out_folder=None, mamca_path=None):
    """
    Запускает набор симуляций с одним изменяющимся параметром
    :param par_name: имя изменяющегося параметра
    :param start: начальное значение параметра
    :param end: конечное значение параметра
    :param step: шаг изменения параметра
    :param(bool) addition: переменная, определяющая, как изменялся параметр.
        Если True, то new_value = old_value + step
        Если False, то new_value = old_value * step
    :param settings_fname: путь к файлу с настройками
    :param out_folder: путь к выходной папке
    :param mamca_path: путь к исполняемому файлу моделирующей программы
    :return:
    """
    def op(x, y):
        return x + y if addition else x * y

    print('Cycle with {} parameter started'.format(par_name))
    s = Settings(settings_fname)
    temp_settings_fname = 'temp_' + settings_fname
    s[par_name] = start
    s.save_settings(temp_settings_fname)
    while s[par_name] <= end:
        print('Now value = {}. End value = {}'.format(s[par_name], end))
        subprocess.run(
            '{} -s {} -o {} -f {}'.format(mamca_path, temp_settings_fname,
                                          out_folder,
                                          '{},{}'.format(par_name,
                                                         s[par_name])),
            stdout=sys.stdout, stderr=sys.stderr)
        s[par_name] = op(s[par_name], step)
        s.save_settings(temp_settings_fname)
    os.remove(temp_settings_fname)
    print('Generation complete')


def _clear_out_folder(path=None, sample_file=""):
    """
    Очищает выходную папку
    :param path: путь к папке
    """
    if path is None:
        path = get_default_out_folder()
    for f in os.listdir(path):
        if f != sample_file:
            os.remove('{}/{}'.format(path, f))
