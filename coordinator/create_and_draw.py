import os
import sys

from json.decoder import JSONDecodeError
from mamca import *


def single_simulation(settings_fname: str = None):
    if settings_fname is None:
        settings_fname = sys.argv[2]
    if not check_settings(settings_fname):
        exit_on_fail('settings file is incorrect')
    # single_run(settings_fname=settings_fname)
    settings = Settings(settings_fname)
    check_borders(settings)
    if settings.hysteresis:
        draw_hyst_plot(
            settings=settings,
            b_axis='x',
            m_axis='x'
        )
    # draw_all_vectors_plots(
    #     settings=settings,
    #     scale=4,
    #     draw_points=False
    # )
    # create_momenta_gif(settings=settings)
    play_success_notification()


def multiple_simulations():
    resource_folder = sys.argv[2]
    if not os.path.exists(resource_folder):
        exit_on_fail('"{}" directory does not exist'.format(resource_folder))
    if not os.path.isdir(resource_folder):
        exit_on_fail('"{}" is not directory'.format(resource_folder))
    for file in sorted(os.listdir(resource_folder)):
        settings_fname = '{}/{}'.format(resource_folder, file)
        if not settings_fname.endswith('.json'):
            continue
        if not check_settings(settings_fname):
            print('{} is not valid settings file'.format(settings_fname))
            continue
        try:
            single_simulation(settings_fname)
        except Exception as e:
            print(e)


def check_settings(settings_fname: str):
    try:
        Settings(settings_fname)
    except JSONDecodeError:
        return False
    except IOError:
        return False
    return True


def exit_on_fail(message: str = None):
    if message is not None:
        print(message)
    play_failure_notification()
    sys.exit(1)


if __name__ == '__main__':
    if len(sys.argv) < 3:
        exit_on_fail('Not enough input arguments')
    if sys.argv[1] == 'single':
        single_simulation()
    elif sys.argv[1] == 'multiple':
        multiple_simulations()
    else:
        exit_on_fail('wrong arguments')
