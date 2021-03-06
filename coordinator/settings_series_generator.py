from mamca import *

"""
    Небольшой скрипт для генерации наборов настроечных файлов
"""


def drange(start, stop, step):
    r = start
    while r <= stop:
        yield float(r)
        r += step


def mrange(start, step, n):
    r = start
    for _ in range(n):
        yield float(r)
        r *= step


if __name__ == '__main__':
    setings_fname = './resources/settings.json'
    settings = Settings(setings_fname)
    template = 'jex_{:.2f}'
    folder_template = './resources/settings/{}.json'
    for jex in drange(0.0, 5.0, 0.5):
        name = template.format(jex)
        settings.jex = jex
        settings.name = name
        settings.save_settings(folder_template.format(name))
