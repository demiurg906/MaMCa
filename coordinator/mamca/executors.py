import sys
import subprocess
from json import JSONDecodeError

from .settings import Settings
from .util import play_failure_notification, which
from . import MAMCA_PATH


def single_run(settings_fname, mamca_path=MAMCA_PATH):
    """
    Запускает однократное моделирование
    :param settings_fname: путь к файлу с настройками
    :param mamca_path: путь к исполняемому файлу моделирующей программы
    """
    try:
        settings = Settings(settings_fname)
    except JSONDecodeError:
        print('settings file is incorrect')
        play_failure_notification()
        sys.exit(1)

    java_path = which('java')
    if java_path is None:
        print('Can\'t find java. Check that java is installed and put in PATH')
        exit_program()

    mathematica_path = which('MathKernel')
    if mathematica_path is None:
        print('Can\'t find Mathematica Kernel. Check that Mathematica is installed and put in PATH')
        exit_program()

    if 'win' in sys.platform:
        extension = '.dll'
    else:
        extension = '.so'
    mathematica_native_library = which('JLinkNativeLibrary', extension)
    if mathematica_native_library is None:
        print('Can\'t find JLinkNativeLibrary.{}. Check that Mathematica is installed and put in PATH'.format(
            extension)
        )
        exit_program()

    completed = subprocess.run(
        '{0} -Xms{1}m -Xmx{1}m -jar {2} -s {3} -m{4}'.format(
            java_path, settings.memory, mamca_path, settings_fname, mathematica_path).split(),
        stdout=sys.stdout, stderr=sys.stderr, )
    if completed.returncode != 0:
        exit_program()


def exit_program():
    play_failure_notification()
    sys.exit(0)
