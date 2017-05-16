import os
import shutil
import sys

from json.decoder import JSONDecodeError
from mamca import *


def single_simulation(settings_fname: str = None):
    if settings_fname is None:
        settings_fname = sys.argv[2]
    if not check_settings(settings_fname):
        exit_on_fail('settings file is incorrect')
    single_run(settings_fname=settings_fname)
    settings = Settings(settings_fname)
    check_borders(settings)
    if settings.hysteresis:
        draw_hyst_plot(
            settings=settings,
            b_axis='x',
            m_axis='x'
        )
    draw_all_vectors_plots(
        settings=settings,
        scale=4,
        draw_points=False
    )
    # create_momenta_gif(settings=settings)
    play_success_notification()


def multiple_simulations():
    resource_folder = sys.argv[2]
    if not os.path.exists(resource_folder):
        exit_on_fail('"{}" directory does not exist'.format(resource_folder))
    if not os.path.isdir(resource_folder):
        exit_on_fail('"{}" is not directory'.format(resource_folder))
    settings_files = list(sorted(os.listdir(resource_folder)))
    settings_path_template = resource_folder + '/{}'
    create_out_hysteresis_folder(Settings(settings_path_template.format(settings_files[0])))
    for file in settings_files:
        settings_fname = settings_path_template.format(file)
        if not settings_fname.endswith('.json'):
            continue
        if not check_settings(settings_fname):
            print('{} is not valid settings file'.format(settings_fname))
            continue
        try:
            single_simulation(settings_fname)
            copy_hyst_plot(Settings(settings_fname))
        except Exception as e:
            print(e)


def copy_hyst_plot(settings: Settings):
    if not settings.hysteresis:
        return
    plot_name = HYST_PLOT_TEMPLATE.format(settings.name) + '.png'
    src_plot_path = '{}/{}/{}'.format(settings.dataFolder, settings.name, plot_name)
    dst_plot_path = '{}/plots/{}'.format(settings.dataFolder, plot_name)
    shutil.copyfile(src_plot_path, dst_plot_path)


def create_out_hysteresis_folder(settings: Settings):
    data_folder = '{}/plots'.format(settings.dataFolder)
    if not os.path.exists(data_folder):
        os.mkdir(data_folder)


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


def main():
    if len(sys.argv) < 3:
        exit_on_fail('Not enough input arguments')
    if sys.argv[1] == 'single':
        single_simulation()
    elif sys.argv[1] == 'multiple':
        multiple_simulations()
    else:
        exit_on_fail('wrong arguments')


if __name__ == '__main__':
    main()
