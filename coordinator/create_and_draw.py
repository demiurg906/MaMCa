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
    create_out_folders(Settings(settings_path_template.format(settings_files[0])))
    for file in settings_files:
        settings_fname = settings_path_template.format(file)
        if not settings_fname.endswith('.json'):
            continue
        if not check_settings(settings_fname):
            print('{} is not valid settings file'.format(settings_fname))
            continue
        try:
            single_simulation(settings_fname)
            copy_settings_and_hyst_plot(Settings(settings_fname))
        except Exception as e:
            print(e)


def copy_settings_and_hyst_plot(settings: Settings):
    data_folder = '{}/{}'.format(settings.dataFolder, settings.name)

    settings_fname = 'settings_{}.json'.format(settings.name)
    settings_src_path = '{}/{}'.format(data_folder, settings_fname)
    settings_dst_path = '{}/settings/{}'.format(data_folder, settings_fname)
    shutil.copyfile(settings_src_path, settings_dst_path)

    if settings.hysteresis:
        plot_name = HYST_PLOT_TEMPLATE.format(settings.name) + '.png'
        plot_src_path = '{}/{}'.format(data_folder, plot_name)
        plot_dst_path = '{}/plots/{}'.format(settings.dataFolder, plot_name)
        shutil.copyfile(plot_src_path, plot_dst_path)


def create_out_folders(settings: Settings):
    folders = ['{}/plots'.format(settings.dataFolder),
               '{}/settings'.format(settings.dataFolder)]
    for folder in folders:
        if not os.path.exists(folder):
            os.mkdir(folder)


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
