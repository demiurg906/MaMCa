import numpy as np
import os
import shutil

from functools import cmp_to_key, wraps
from matplotlib import pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

from mamca.settings import Settings

# счетчик для фигур
_counter = 0


def _clear_folder(folder):
    """
    Удаляет содержимое папки
    :param folder: путь к папке
    :return:
    """
    for the_file in os.listdir(folder):
        file_path = os.path.join(folder, the_file)
        try:
            if os.path.isfile(file_path):
                os.unlink(file_path)
            elif os.path.isdir(file_path):
                shutil.rmtree(file_path)
        except Exception as e:
            print(e)


def prepare_dir_wrapper(func):
    """
    Декоратор, подготавливающий папку со скриншотами
    """

    @wraps(func)
    def inner(*args, **kwargs):
        if 'save' in kwargs:
            save = kwargs['save']
            if save:
                out = kwargs['pic_dir']
                if not os.path.exists(out):
                    os.mkdir(out)
        func(*args, **kwargs)

    return inner


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


@prepare_dir_wrapper
def draw_hyst_plot(folder, b_axis, m_axis, label=None, borders=None,
                   direction=None, filter=None, settings_fname=None,
                   save=False, pic_dir='Screenshots', name=None):
    """
    Рисует петлю гистерезиса
    :param folder: папка с данными о гистерезисе
    :param b_axis: проекция поля ('x', 'y', 'z')
    :param m_axis: проекиця момента ('x', 'y', 'z')
    :param label: название графика
    :param borders: диапазон поля ([min_b, max_b])
    :param direction: множество, определяющее, какие ветви гистерезиса
        отрисовывать ({'fst', 'neg', 'pos'})
    :param filter: массив, определяющий, какую часть частиц использовать.
        использовать только для систем, лежащих в плоскости.
        формат: [dn_x, dn_y, n_x, n_y] -- dn_x и dn_y -- сколько частиц
        отрезать с обеих сторон, n_x и n_y -- сколько частиц всего
    :param settings_fname: путь к файлу с настройками, для отображения
        их на графике
    :param(bool) save: сохранять ли график в файл
    :param pic_dir: путь к папке для сохранения скриншотов
    :param name: имя для скриншота
    """
    if direction is None:
        direction = {'fst', 'pos', 'neg'}
    axises = {'x': 0, 'y': 1, 'z': 2}

    def get_full_moment(file):
        vs, points = _read_vectors(file)
        if filter is None:
            return sum(vs[:, 3] - vs[:, 0]), \
                   sum(vs[:, 4] - vs[:, 1]), \
                   sum(vs[:, 5] - vs[:, 2])
        else:
            dx, dy, nx, ny = filter[0], filter[1], filter[2], filter[3]
            left = [dx, dy, -int(2e10)]
            right = [nx - dx, ny - dy, int(2e10)]
            res = [0, 0, 0]
            for i in range(points.shape[0]):
                b = True
                for j in range(3):
                    if not (left[j] <= points[i, j] < right[j]):
                        b = False
                if b:
                    for j in range(3):
                        res[j] += vs[i, j + 3] - vs[i, j]
            return res[0], res[1], res[2]

    global _counter
    fig = plt.figure(_counter)
    _counter += 1
    if label is None:
        label = 'M_{}(B_{})'.format(m_axis, b_axis)
    fig.canvas.set_window_title(label)
    plt.xlabel('B_{}, magnetic field'.format(b_axis))
    plt.ylabel('M_{}, magnetic moment'.format(m_axis))

    min_b, max_b = np.inf, -np.inf
    min_m, max_m = np.inf, -np.inf
    for f in os.listdir(folder):
        try:
            s, sign, *b = f[:-4].split(',')
        except ValueError:
            continue
        if sign not in direction:
            continue
        b = float(b[axises[b_axis]])
        m = get_full_moment('{}/{}'.format(folder, f))[axises[m_axis]]
        min_b, max_b = min(min_b, b), max(max_b, b)
        min_m, max_m = min(min_m, m), max(max_m, m)

        color = {'pos': 'r', 'neg': 'b', 'fst': 'g'}
        plt.scatter(b, m, color=color[sign])
    if borders is not None:
        min_b, max_b = borders[0], borders[1]
    axis = [min_b, max_b, min_m, max_m]
    plt.axis(list(map(lambda x: x * 1.1, axis)))
    if settings_fname is not None:
        plt.text(min_b, max_m / 3, str(Settings(settings_fname)))
    if save:
        if name is None:
            name = 'fig_{}'.format(_counter)
        plt.savefig('{}/{}.png'.format(pic_dir, name), format='png')
        plt.clf()


def create_hysteresis_gif(out_folder, pic_folder, borders=None,
                          negative_borders=False, clear=True,
                          show=False, is3d=True, scale=1):
    """
    Рисует набор трехмерных графиков для цикла гистерезиса
    :param out_folder: путь к папке с данными о гистерезисе
    :param pic_folder: путь к папке для сохранения графиков
    :param borders: границы графиков в формате borders функции
        draw_3d_vectors_plot
    :param(bool) negative_borders: параметр для draw_3d_vectors_plot
    :param(bool) clear: очищать ли папку со скриншотами перед сохранением
    :param(bool) show: отображать ли окна с графиками
    :param(bool) is3d: рисовать трехмерный график или двумерный в осяц x y
    :param(float) scale: масштаб для стрелочек
    """
    names = []
    b_fields = {}
    signs = {'fst': 0, 'neg': 1, 'pos': 2}
    files = []

    for f in os.listdir(out_folder):
        try:
            _, sign, *b = f[:-4].split(',')
        except ValueError:
            continue
        b = list(map(lambda x: int(float(x)), b))
        names.append([signs[sign], *b, f])
        b_fields[f] = b
        files.append(f)

    def cmp(x, y):
        if x[0] != y[0]:
            return x[0] - y[0]
        sgn = x[0]
        for i in range(1, 4):
            if x[i] != y[i]:
                return y[i] - x[i] if sgn == 1 else x[i] - y[i]
        return 0

    names.sort(key=cmp_to_key(cmp))
    d_names = {x[4]: ('{}_{}_{}_{}_{}'.format(x[0], i, x[1], x[2], x[3]))
               for i, x in enumerate(names)}
    if not os.path.exists(pic_folder):
        os.mkdir(pic_folder)
    if clear:
        _clear_folder(pic_folder)
    for f in files:
        if is3d:
            draw_3d_vectors_plot(
                '{}/{}'.format(out_folder, f), borders=borders,
                negative_borders=negative_borders, save=not show,
                pic_dir=pic_folder, name=d_names[f],
                text='B = ({}, {}, {})'.format(*b_fields[f]),
                draw_particles=False, scale=scale)
        else:
            draw_xy_vectors_plot(
                '{}/{}'.format(out_folder, f), borders=borders,
                negative_borders=negative_borders, save=not show,
                pic_dir=pic_folder, name=d_names[f],
                text='B = ({}, {}, {})'.format(*b_fields[f]))


@prepare_dir_wrapper
def draw_3d_vectors_plot(filename: str, settings_fname: str=None, *, borders: list=None,
                         negative_borders: bool=True, label: str=None,
                         save: bool=False, pic_dir: str='Screenshots', name: str=None,
                         text: str=None, draw_particles: bool=True, scale: float=1):
    """
    Рисует трехмерный график веторов
    :param filename: путь к файлу с данными
    :param settings_fname: путь к файлу с настройками, для отображения
        их на графике
    :param borders: массив границ графика.
        формат: либо [x, y, z] либо [x1, x2, y1, y2, z1, z2]
    :param negative_borders: если формат массива borders [x, y, z],
        то этот параметр определяет границы.
        Если True, то границы будут [-x, x, -y, y, -z, z],
        если False, то [0, x, 0, y, 0, z]
    :param label: название графика
    :param(bool) save: сохранять ли график в файл
    :param pic_dir: путь к папке для сохранения скриншотов
    :param name: имя для скриншота
    :param text: текст для отображения на графике
    :param(bool) draw_particles: нужно ли отображать частицы
    :param(float) scale: масштаб стрелочек
    """
    global _counter
    fig = plt.figure(_counter)
    _counter += 1
    if label is not None:
        fig.canvas.set_window_title(str(label))
    vectors, points = _read_vectors(filename)
    ax = fig.add_subplot(111, projection='3d')
    if borders is None:
        mins, maxs = vectors.min(axis=0), vectors.max(axis=0)
        x_min, x_max = min(mins[0], mins[3]), max(maxs[0], maxs[3])
        y_min, y_max = min(mins[1], mins[4]), max(maxs[1], maxs[4])
        z_min, z_max = min(mins[2], mins[5]), max(maxs[2], maxs[5])
        x_k, y_k, z_k = 1.1, 1.1, 1.1
        if z_max - z_min < 1:
            length = x_max - x_min
            z_min, z_max = -length / 2, length / 2
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
    ax.set_xlabel('X axis')
    ax.set_ylabel('Y axis')
    ax.set_zlabel('Z axis')

    x1, y1, z1 = vectors[:, 0], vectors[:, 1], vectors[:, 2]
    x2, y2, z2 = vectors[:, 3], vectors[:, 4], vectors[:, 5]
    u, v, w = x2 - x1, y2 - y1, z2 - z1

    # scaling
    # уебался, пока разбирался, как масштабировать эти чертовы стрелочки
    k = (scale - 1) / 2
    dx, dy, dz = u * k, v * k, w * k
    x1 -= dx
    y1 -= dy
    z1 -= dz
    x2 += dx
    y2 += dy
    z2 += dz
    u, v, w = x2 - x1, y2 - y1, z2 - z1

    ax.quiver(x1, y1, z1, u, v, w, length=scale * 2, pivot='tail', arrow_length_ratio=0.2)  # 0.5

    if draw_particles:
        x, y, z = points[:, 0], points[:, 1], points[:, 2]
        ax.scatter(x, y, z, c='r')

    if settings_fname is not None:
        ax.text2D(0.05, 0.65, str(Settings(settings_fname)),
                  transform=ax.transAxes)
    if text is not None:
        ax.text2D(0.05, 0.65, text, transform=ax.transAxes)

    if save:
        if name is None:
            name = 'fig_{}'.format(_counter)
        plt.savefig('{}/{}.png'.format(pic_dir, name), format='png')
        plt.clf()


def _draw_vectors_plot(filename, label, x, y, borders=None,
                       negative_borders=False,
                       save=False, pic_dir='Screenshots',
                       name=None, text=None):
    """
    Рисует двумерный график в заданных осях
    :param filename: путь к файлу с даннми
    :param label: название графика
    :param x: ось абсцисс (x -- 0, y -- 1, z -- 2)
    :param y: ось ординат (x -- 0, y -- 1, z -- 2)
    :param borders: массив границ графика.
        формат: либо [x, y, z] либо [x1, x2, y1, y2, z1, z2]
    :param negative_borders: если формат массива borders [x, y, z],
        то этот параметр определяет границы.
        Если True, то границы будут [-x, x, -y, y, -z, z],
        если False, то [0, x, 0, y, 0, z]
    :param(bool) save: сохранять ли график в файл
    :param pic_dir: путь к папке для сохранения скриншотов
    :param name: имя для скриншота
    :param text: текст для отображения на графике
    """
    global _counter
    fig = plt.figure(_counter)
    _counter += 1
    if label is not None:
        fig.canvas.set_window_title(label)
    # индексы с нужными осями
    x1, y1, x2, y2 = x, y, x + 3, y + 3
    vectors, points = _read_vectors(filename)
    # минимальные и максимальные значения
    if borders is None:
        mins, maxs = vectors.min(axis=0), vectors.max(axis=0)
        axis = [min(mins[x1], mins[x2]), max(maxs[x1], maxs[x2]),
                min(mins[y1], mins[y2]), max(maxs[y1], maxs[y2])]
    else:
        if len(borders) == 2:
            k = -1 if negative_borders else 0
            axis = [borders[0] * k, borders[0],
                    borders[1] * k, borders[1]]
        else:
            axis = borders

    # масштаб осей
    plt.axis(list(map(lambda k: k * 1.1, axis)))
    # подписи оскй
    axises = {0: 'x', 1: 'y', 2: 'z'}
    plt.xlabel(axises[x])
    plt.ylabel(axises[y])
    # рисование стрелок
    ax = plt.axes()
    for v in vectors:
        ax.arrow(v[x1], v[y1], v[x2] - v[x1], v[y2] - v[y1])

    if text is not None:
        ax.text(0.05, 0.65, text, transform=ax.transAxes)

    if save:
        if name is None:
            name = 'fig_{}'.format(_counter)
        plt.savefig('{}/{}.png'.format(pic_dir, name), format='png')
        plt.clf()


def draw_xy_vectors_plot(filename, label=None, borders=None,
                         negative_borders=False,
                         save=False, pic_dir='Screenshots',
                         name=None, text=None):
    """
    Рисует двумерный график в заданных осях
    :param filename: путь к файлу с даннми
    :param label: название графика
    :param x: ось абсцисс (x -- 0, y -- 1, z -- 2)
    :param y: ось ординат (x -- 0, y -- 1, z -- 2)
    :param borders: массив границ графика.
        формат: либо [x, y, z] либо [x1, x2, y1, y2, z1, z2]
    :param negative_borders: если формат массива borders [x, y, z],
        то этот параметр определяет границы.
        Если True, то границы будут [-x, x, -y, y, -z, z],
        если False, то [0, x, 0, y, 0, z]
    :param(bool) save: сохранять ли график в файл
    :param pic_dir: путь к папке для сохранения скриншотов
    :param name: имя для скриншота
    :param text: текст для отображения на графике
    """
    _draw_vectors_plot(filename, label, 0, 1, borders, negative_borders,
                       save, pic_dir, name, text)


def draw_xz_vectors_plot(filename, label=None, borders=None,
                         negative_borders=False,
                         save=False, pic_dir='Screenshots',
                         name=None, text=None):
    """
    Рисует двумерный график в заданных осях
    :param filename: путь к файлу с даннми
    :param label: название графика
    :param x: ось абсцисс (x -- 0, y -- 1, z -- 2)
    :param y: ось ординат (x -- 0, y -- 1, z -- 2)
    :param borders: массив границ графика.
        формат: либо [x, y, z] либо [x1, x2, y1, y2, z1, z2]
    :param negative_borders: если формат массива borders [x, y, z],
        то этот параметр определяет границы.
        Если True, то границы будут [-x, x, -y, y, -z, z],
        если False, то [0, x, 0, y, 0, z]
    :param(bool) save: сохранять ли график в файл
    :param pic_dir: путь к папке для сохранения скриншотов
    :param name: имя для скриншота
    :param text: текст для отображения на графике
    """
    _draw_vectors_plot(filename, label, 0, 2, borders, negative_borders,
                       save, pic_dir, name, text)


def draw_yz_vectors_plot(filename, label=None, borders=None,
                         negative_borders=False,
                         save=False, pic_dir='Screenshots',
                         name=None, text=None):
    """
    Рисует двумерный график в заданных осях
    :param filename: путь к файлу с даннми
    :param label: название графика
    :param x: ось абсцисс (x -- 0, y -- 1, z -- 2)
    :param y: ось ординат (x -- 0, y -- 1, z -- 2)
    :param borders: массив границ графика.
        формат: либо [x, y, z] либо [x1, x2, y1, y2, z1, z2]
    :param negative_borders: если формат массива borders [x, y, z],
        то этот параметр определяет границы.
        Если True, то границы будут [-x, x, -y, y, -z, z],
        если False, то [0, x, 0, y, 0, z]
    :param(bool) save: сохранять ли график в файл
    :param pic_dir: путь к папке для сохранения скриншотов
    :param name: имя для скриншота
    :param text: текст для отображения на графике
    """
    _draw_vectors_plot(filename, label, 1, 2, borders, negative_borders,
                       save, pic_dir, name, text)


def draw_all_2d_vectors_plots(filename, label=None):
    """
    Рисует графики векторов во всех плоскостях
    :param filename: путь к файлу с данными
    :param label: название графика
    """
    if label is None:
        labels = ['xy', 'yz', 'xz']
    else:
        labels = [label, label, label]
    draw_xy_vectors_plot(filename, labels[0])
    draw_yz_vectors_plot(filename, labels[1])
    draw_xz_vectors_plot(filename, labels[2])


def draw_multiple_plots_for_cycle(out_folder, parameter_name, start, end, step,
                                  addition=True,
                                  settings_fname=None, borders=None,
                                  negative_borders=False,
                                  save=False, pic_dir='Screenshots'):
    """
    Рисует набор графиков для данных с одним изменяющимся параметром
    :param out_folder: путь к папке с данными
    :param parameter_name: параметр, который менялся
    :param start: начальное значение параметра
    :param end: конечное значение параметра
    :param step: шаг изменения параметра
    :param(bool) addition: переменная, определяющая, как изменялся параметр.
        Если True, то new_value = old_value + step
        Если False, то new_value = old_value * step
    :param settings_fname: путь к файлу с настройками, для отображения
        их на графике
    :param borders: массив границ графика.
        формат: либо [x, y, z] либо [x1, x2, y1, y2, z1, z2]
    :param negative_borders: если формат массива borders [x, y, z],
        то этот параметр определяет границы.
        Если True, то границы будут [-x, x, -y, y, -z, z],
        если False, то [0, x, 0, y, 0, z]
    :param(bool) save: сохранять ли график в файл
    :param pic_dir: путь к папке для сохранения скриншотов
    """
    template = parameter_name + ',{}.txt'
    value = start
    while value <= end:
        name = '{} = {}'.format(parameter_name, value)
        draw_3d_vectors_plot(
            '{}/{}'.format(out_folder, template.format(value)), settings_fname,
            label=name, borders=borders, negative_borders=negative_borders,
            save=save, pic_dir=pic_dir, name=name)
        if addition:
            value += step
        else:
            value *= step


# Используется для рисования всех созданных графиков
def end_of_drawing():
    """
    Необходимо вызывать после вызова всех процедур рисования для отображения
        нарисованных графиков (если не был выбран режим сохранения в файл)
    """
    plt.show()
