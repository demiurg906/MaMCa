import sys
import subprocess

from .settings import Settings
from .util import which
from . import MAMCA_PATH


def single_run(settings_fname, mamca_path=MAMCA_PATH):
    """
    Запускает однократное моделирование
    :param settings_fname: путь к файлу с настройками
    :param mamca_path: путь к исполняемому файлу моделирующей программы
    """
    java_path = which('java')
    settings = Settings(settings_fname)
    if java_path is None:
        print('Can\'t find java. Check that java is installed and put in PATH')
        sys.exit(0)
    completed = subprocess.run(
        '{0} -Xms{1}m -Xmx{1}m -jar {2} -s {3}'.format(java_path, settings.memory, mamca_path, settings_fname),
        stdout=sys.stdout, stderr=sys.stderr, )
    if completed.returncode != 0:
        sys.exit(0)
