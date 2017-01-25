import numpy as np
import os
import sys
import subprocess

from matplotlib import pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

from .settings import Settings
from .util import which

# греческая mu в utf-8 кодировке
MU = '\u03BC'

# счетчик для фигур
_counter = 0

# установка семейства шрифтов для корректного отображения
plt.rc('font', family='Arial')

use_tex = which('tex') is not None
# если установлен Тех, то импользовать его при рендере меток
if use_tex:
    plt.rc('text', usetex=True)


def _get_lines(filename, skip=0):
    """
    :param filename: путь к файлу
    :param skip: сколько первых строк файла надо пропустить
    :return: строки файла, числа в которых готовы к преобразованию во float
    """
    with open(filename, mode='r') as f:
        for i in range(skip):
            f.readline()
        res = [s.replace(',', '.') for s in f]
    return res


def _read_vectors(filename):
    """
    Читает содержимое файла
    :param filename: путь к файлу
    :return:
        два numpy массива --- массив векторов в
        формате (x1, y1, z1, x2, y2, z2) и массив координат (x, y, z)
    """
    temp = np.array(
        [tuple(map(lambda s: float(s), line)) for line in
         list(map(lambda s: s.split(), _get_lines(filename)))])
    return temp[:, :6], temp[:, 6:]


def draw_all_hyst_plots(*, settings_fname, b_axis, m_axis, label=None, borders=None,
                        area=None):
    """
    Рисует три графика гистерезиса -- по одному на каждую ветвь и общий
    """
    data = (
        (None, '0_hyst_plot'),
        ('fst', '1_hyst_plot_fst'),
        ('neg', '2_hyst_plot_neg'),
        ('pos', '3_hyst_plot_pos')
    )
    for direction, name in data:
        draw_hyst_plot(
            settings_fname=settings_fname,
            b_axis=b_axis, m_axis=m_axis,
            label=label, borders=borders,
            area=area,
            direction=direction,
            name=name
        )


def draw_hyst_plot(*, settings_fname, b_axis, m_axis, label=None, borders=None,
                   direction=None, area=None, name=None):
    """
    Рисует петлю гистерезиса
    :param settings_fname: путь к файлу с настройками, для отображения
        их на графике
    :param b_axis: проекция поля ('x', 'y', 'z')
    :param m_axis: проекиця момента ('x', 'y', 'z')
    :param label: название графика
    :param borders: диапазон поля ([min_b, max_b])
    :param direction: множество, определяющее, какие ветви гистерезиса
        отрисовывать ({'fst', 'neg', 'pos'})
    :param area: массив, определяющий, какую часть частиц использовать.
        использовать только для систем, лежащих в плоскости.
        формат: [dn_x, dn_y, n_x, n_y] -- dn_x и dn_y -- сколько частиц
        отрезать с обеих сторон, n_x и n_y -- сколько частиц всего
    :param name: имя для скриншота
    """
    settings = Settings(settings_fname)
    data_folder = '{}/{}'.format(settings.dataFolder, settings.name)
    out_folder = '{}/out'.format(data_folder)
    pic_dir = data_folder + '/pictures'

    if direction is None:
        direction = {'fst', 'pos', 'neg'}
    axises = {'x': 0, 'y': 1, 'z': 2}

    def get_full_moment(file):
        """
        читает моменты частиц образца из файла и суммирует их
        """
        vs, points = _read_vectors(file)
        # суммировать все моменты
        if area is None:
            return sum(vs[:, 3] - vs[:, 0]), \
                   sum(vs[:, 4] - vs[:, 1]), \
                   sum(vs[:, 5] - vs[:, 2])
        else:
            # суммировать только моменты из центра образца,
            # в соответсвии с параметров filter
            dx, dy, nx, ny = area[0], area[1], area[2], area[3]
            left = [dx, dy, -int(2e10)]
            right = [nx - dx, ny - dy, int(2e10)]
            res = [0, 0, 0]
            for i in range(points.shape[0]):
                in_area = True
                for j in range(3):
                    if not (left[j] <= points[i, j] < right[j]):
                        in_area = False
                if in_area:
                    for j in range(3):
                        res[j] += vs[i, j + 3] - vs[i, j]
            return res[0], res[1], res[2]

    global _counter
    fig = plt.figure(0)
    _counter += 1

    # подписи на графике
    if use_tex:
        if label is None:
            label = r'$M_{}(B_{})$'.format(m_axis, b_axis)
        plt.xlabel(r'$B_{}, T$'.format(b_axis))
        plt.ylabel(r'$M_{}, \mu_B$'.format(m_axis))
    else:
        if label is None:
            label = 'M_{}(B_{})'.format(m_axis, b_axis)
        plt.xlabel('B_{}, T'.format(b_axis))
        plt.ylabel('M_{}, {}_B'.format(m_axis, MU))
    fig.canvas.set_window_title(label)

    min_b, max_b = np.inf, -np.inf
    min_m, max_m = np.inf, -np.inf
    for f in os.listdir(out_folder):
        try:
            s, n, sign, *b = f[:-4].split('_')
        except ValueError:
            continue
        if sign not in direction:
            continue
        b = float(b[axises[b_axis]])

        # суммарный момент образца в магнетонах бора
        m = get_full_moment('{}/{}'.format(out_folder, f))[axises[m_axis]] * settings.m

        # вычисление границ поля
        min_b, max_b = min(min_b, b), max(max_b, b)

        # вычисление границ момента
        min_m, max_m = min(min_m, m), max(max_m, m)

        color = {'pos': 'r', 'neg': 'b', 'fst': 'g'}
        plt.scatter(b, m, color=color[sign])

    # выравнивание границ поля и момента, чтобы график был симметричным по обоим осям
    max_b = max(abs(min_b), abs(max_b))
    min_b = -max_b
    max_m = max(abs(min_m), abs(max_m))
    min_m = -max_m

    # установка границ
    if borders is not None:
        min_b, max_b = borders[0], borders[1]
    axis = [min_b, max_b, min_m, max_m]
    plt.axis(list(map(lambda x: x * 1.1, axis)))

    # if settings_fname is not None:
    #     plt.text(min_b, max_m / 3, str(Settings(settings_fname)))
    if name is None:
        name = 'fig_{}'.format(_counter)
    plt.savefig('{}/{}.png'.format(pic_dir, name), format='png')
    plt.clf()


def create_momenta_gif(*, settings_fname: str):
    """
    Создает гифку из состояний образца между скачками моментов
    :param settings_fname: путь к файлу с настройками
    :return:
    """
    settings = Settings(settings_fname)
    data_folder = '{}/{}/pictures'.format(settings.dataFolder, settings.name)
    momenta_template = '{}/moments/momenta*.png'.format(data_folder)
    # поиск утилиты convert
    # под windows есть системная утилита convert,
    # поэтому запуск осуществляется посредством 'magick convert'
    if 'win' in sys.platform:
        exe = which('magick')
        if exe is not None:
            exe += ' convert'
    else:
        exe = which('convert')
    if exe is None:
        print('You need install ImageMagick (http://www.imagemagick.org) for creating gifs')
        print('magick must be in your classpath')
        return
    exe += ' -delay 60 -loop 0 "{}" "{}/momenta.gif"'.format(momenta_template, data_folder)
    subprocess.run(exe, stdout=sys.stdout, stderr=sys.stderr, )


def draw_all_3d_vectors_plots(*, settings_fname: str = None, borders: list = None,
                              negative_borders: bool = True, label: str = None,
                              scale: float = 1):
    """
    Рисует графики состояний до и после оптимизации
    """
    settings = Settings(settings_fname)
    data_folder = '{}/{}/out'.format(settings.dataFolder, settings.name)
    for file in os.listdir(data_folder):
        if file.startswith('momenta'):
            if settings.hysteresis:
                _, _, _, bx, by, bz = file[:-4].split('_')
                text = 'B({}, {}, {})'.format(bx, by, bz)
            else:
                _, _, _, t = file[:-4].split('_')
                text = 't = {} s'.format(t)
            draw_3d_vectors_plot(
                settings_fname=settings_fname,
                borders=borders,
                negative_borders=negative_borders,
                label=label,
                text=text,
                scale=scale,
                momenta_filename=file
            )


def draw_3d_vectors_plot(*, settings_fname: str = None, borders: list = None,
                         negative_borders: bool = True, label: str = None,
                         text: str = None, scale: float = 1,
                         momenta_filename: str
                         ):
    """
    Рисует трехмерный график веторов
    :param settings_fname: путь к файлу с настройками, для отображения
        их на графике
    :param borders: массив границ графика.
        формат: либо [x, y, z] либо [x1, x2, y1, y2, z1, z2]
    :param(bool) negative_borders: если формат массива borders [x, y, z],
        то этот параметр определяет границы.
        Если True, то границы будут [-x, x, -y, y, -z, z],
        если False, то [0, x, 0, y, 0, z]
    :param(str) label: название графика
    :param(str) text: текст для отображения на графике
    :param(float) scale: масштаб стрелочек
    :param(str) momenta_filename: путь к файлу с данными
    """
    settings = Settings(settings_fname)
    data_folder = '{}/{}'.format(settings.dataFolder, settings.name)
    filename = '{}/out/{}'.format(data_folder, momenta_filename)
    name = momenta_filename[:-4]
    pic_dir = '{}/pictures/moments'.format(data_folder)

    global _counter
    fig = plt.figure(0)
    _counter += 1
    if label is not None:
        fig.canvas.set_window_title(str(label))
    vectors, points = _read_vectors(filename)
    ax = fig.add_subplot(111, projection='3d')
    if borders is None:
        # минимумы и максимумы координат
        mins = points.min(axis=0)
        maxs = points.max(axis=0)
        # максимальное расстояние между крайними точками
        l = max(maxs - mins)
        # положение минимумов и максимумов на осях
        # все расстояния равны максимальному
        x_min = mins[0]
        x_max = mins[0] + l

        y_min = mins[1]
        y_max = mins[1] + l
        # TODO: т.к. пока все образцы плоские, z вычисляется таким образом
        # для трехмерных образцов необходимо скорректировать
        z_min = -l
        z_max = mins[2] + l

        # коэффициенты "растяжения"
        x_k, y_k, z_k = 1.1, 1.1, 1.1
        ax.set_xlim3d(x_min * x_k, x_max * x_k)
        ax.set_ylim3d(y_min * y_k, y_max * y_k)
        ax.set_zlim3d(z_min * z_k, z_max * z_k)
    else:
        if len(borders) == 3:
            k = -1 if negative_borders else 0
            ax.set_xlim3d(borders[0] * k, borders[0])
            ax.set_ylim3d(borders[1] * k, borders[1])
            ax.set_zlim3d(borders[2] * k, borders[2])
        else:
            ax.set_xlim3d(borders[0], borders[1])
            ax.set_ylim3d(borders[2], borders[3])
            ax.set_zlim3d(borders[4], borders[5])

    # подписи осей
    ax.set_xlabel('x, nm')
    ax.set_ylabel('y, nm')
    ax.set_zlabel('z, nm')

    # координаты стрелок моментов
    x1, y1, z1 = vectors[:, 0], vectors[:, 1], vectors[:, 2]
    x2, y2, z2 = vectors[:, 3], vectors[:, 4], vectors[:, 5]
    u, v, w = x2 - x1, y2 - y1, z2 - z1

    # scaling моментов
    # уебался, пока разбирался, как масштабировать эти чертовы стрелочки
    # вроде как криво работает в версиях matplotlib'а выше 1.5.8 (и, возможно, ниже)
    k = (scale - 1) / 2
    dx, dy, dz = u * k, v * k, w * k
    x1 -= dx
    y1 -= dy
    z1 -= dz
    x2 += dx
    y2 += dy
    z2 += dz
    u, v, w = x2 - x1, y2 - y1, z2 - z1

    # рисование моментов
    ax.quiver(x1, y1, z1, u, v, w, length=scale * 2, pivot='tail', arrow_length_ratio=0.2)

    # рисование частиц
    x, y, z = points[:, 0], points[:, 1], points[:, 2]
    ax.scatter(x, y, z, c='r')

    if text is not None:
        ax.text2D(0.05, 0.65, text, transform=ax.transAxes)

    if name is None:
        name = 'fig_{}'.format(_counter)
    plt.savefig('{}/{}.png'.format(pic_dir, name), format='png')
    plt.clf()
