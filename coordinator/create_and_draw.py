from mamca import *

if __name__ == '__main__':
    settings_fname = './resources/settings.json'
    single_run(settings_fname=settings_fname)
    settings = Settings(settings_fname)
    if settings.hysteresis:
        draw_all_hyst_plots(
            settings_fname=settings_fname,
            b_axis='x',
            m_axis='x',
            save=True
        )
    else:
        draw_all_3d_vectors_plots(
            settings_fname=settings_fname,
            borders=[30, 30, 30],
            scale=8,
            save=True,
        )
    # end_of_drawing()
