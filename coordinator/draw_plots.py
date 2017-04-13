from mamca import *

if __name__ == '__main__':
    settings_fname = './resources/settings.json'
    # settings_fname = '../data/define_jex/jex=0.10/settings.json'
    settings = Settings(settings_fname)
    plane_borders = [80, 160]
    z_borders = [x * (plane_borders[1] - plane_borders[0]) / 2 for x in range(-1, 2, 2)]
    # draw_all_vectors_plots(
    #     settings=settings,
    #     scale=4,
    #     draw_points=False
    # )
    draw_plot_from_hyst_series(settings=settings,
                               numbers=[18, 19, 21],
                               draw_points=False,
                               show=True,
                               # borders=[*plane_borders, *plane_borders, *z_borders]
                               )
    end_of_drawing()