import sys

from json.decoder import JSONDecodeError
from mamca import *

if __name__ == '__main__':
    settings_fname = './resources/settings.json'
    single_run(settings_fname=settings_fname)
    settings = Settings(settings_fname)
    check_borders(settings)
    if settings.hysteresis:
        draw_all_hyst_plots(
            settings=settings,
            b_axis='x',
            m_axis='x'
        )
    draw_all_vectors_plots(
        settings=settings,
        scale=4,
        draw_points=False
    )
    create_momenta_gif(settings=settings)
    play_success_notification()
