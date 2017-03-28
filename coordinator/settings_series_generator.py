from mamca import *

"""
    Небольшой скрипт для генерации наборов настроечных файлов
"""


def drange(start, stop, step):
    r = start
    while r < stop:
        yield float(r)
        r += step


if __name__ == '__main__':
    setings_fname = './resources/settings.json'
    settings = Settings(setings_fname)
    template = 'jex={:.1f}'
    folder_template = './resources/settings/{}.json'
    for jex in drange(19, 19.6, 0.2):
        name = template.format(jex)
        settings.jex = jex
        settings.name = name
        settings.save_settings(folder_template.format(name))
