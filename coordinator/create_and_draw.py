from mamca import *

if __name__ == '__main__':
    settings_fname = './resources/settings.json'
    single_run(settings_fname=settings_fname)
    draw_both_3d_vectors_plots(
        settings_fname=settings_fname,
        borders=[30, 30, 30],
        scale=8,
        save=True,
    )
    # end_of_drawing()


# if __name__ == '__main__':
#     if cycle:
#         par_name = 'off'
#         addition = True
#         start, end, step = 0, 5, 1
#         cycle_one_parameter(par_name, start, end, step, addition,
#                             settings_fname=settings, out_folder=out_folder)
#         draw_multiple_plots_for_cycle(out_folder, par_name, start, end, step,
#                                       addition, settings, save=False,
#                                       pic_dir=pic_folder)
#
#     end_of_drawing()
