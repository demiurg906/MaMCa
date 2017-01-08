import math
import os
import sys
import subprocess

from functools import wraps

from .default_names import get_default_settings_file, get_default_mamca_path, \
    get_default_out_folder, JAVA_PATH
from .settings import Settings


def exe(mamca_path, settings_fname, out_folder, momenta_filename='momenta.txt'):
    return [JAVA_PATH, '-jar', mamca_path, settings_fname, out_folder, momenta_filename]


def pre_clean(func):
    """
    Декоратор, подготавлявающий выходную папку.
        если ее нет, и очищает ее, если в ней что-то есть
    """

    @wraps(func)
    def inner(*args, **kwargs):
        if kwargs.get('settings_fname', None) is None:
            kwargs['settings_fname'] = get_default_settings_file()
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
        # _clear_out_folder(out, sample_file)
        func(*args, **kwargs)
        # dumpedState = '{}/sample.json'.format(out)
        # if os.path.exists(dumpedState):
        #     os.remove(dumpedState)

    return inner


@pre_clean
def single_run(settings_fname=None, mamca_path=None):
    """
    Запускает однократное моделирование
    :param settings_fname: путь к файлу с настройками
    :param out_folder: путь к выходной папке
    :param mamca_path: путь к исполняемому файлу моделирующей программы
    """
    print('Single run just started')
    subprocess.run(
        'java -jar {} -s {}'.format(mamca_path, settings_fname),
        # exe(mamca_path, settings_fname, out_folder),
        stdout=sys.stdout, stderr=sys.stderr, )
    print('Single run has finished')


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
