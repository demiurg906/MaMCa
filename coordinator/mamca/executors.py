import sys
import subprocess

from .util import which
from . import MAMCA_PATH


def single_run(settings_fname, mamca_path=MAMCA_PATH):
    """
    Запускает однократное моделирование
    :param settings_fname: путь к файлу с настройками
    :param mamca_path: путь к исполняемому файлу моделирующей программы
    """
    print('Single run just started')
    java_path = which('java')
    if java_path is None:
        print('Can\'t find java. Check that java is installed and put in PATH')
        sys.exit(0)
    subprocess.run(
        '{} -jar {} -s {}'.format(java_path, mamca_path, settings_fname),
        stdout=sys.stdout, stderr=sys.stderr, )
    print('Single run has finished')
