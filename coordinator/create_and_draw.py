from mamca import *

if __name__ == '__main__':
    main_out_folder = '../MaMCa_out/'
    out_folder = main_out_folder + 'out_rings_offset_test'
    pic_folder = main_out_folder + 'Screenshots/rings_offset_test'
    settings = 'settings.txt'

    single = True
    hysteresis = False
    hysteresis_gif = False
    cycle = False

    if single:
        momenta_path = out_folder + '/momenta.txt'
        single_run(out_folder=out_folder, settings_fname=None)
        draw_3d_vectors_plot(momenta_path, save=False, settings_fname=settings)
        # draw_3d_vectors_plot(momenta_path, borders=[0.5, 0.5, 2], save=True)
        # draw_3d_vectors_plot(momenta_path, borders=[1, 1, 2], save=True)

    if hysteresis:
        cur_pic_folder = '{}/{}'.format(pic_folder, 'hyst_plane_60x60_new')
        hysteresis_log_run(out_folder=out_folder,
                           settings_fname='settings.txt', k=3, prec=1,
                           min_log_scale=0.1)
        draw_hyst_plot(out_folder, 'x', 'x', settings_fname=settings,
                       save=True, filter=[20, 20, 60, 60],
                       pic_dir=cur_pic_folder, name='fig_2')
        # draw_hyst_plot(out_folder, 'x', 'y', settings_fname=settings)
        # draw_hyst_plot(out_folder, 'x', 'z', settings_fname=settings)

        # draw_hyst_plot(out_folder, 'x', 'x', borders=[-0.5e7, 0.5e7], filter={'fst'})
        # draw_hyst_plot(out_folder, 'x', 'x', borders=[-0.5e7, 0.5e7], filter={'neg'})
        # draw_hyst_plot(out_folder, 'x', 'x', borders=[-0.5e7, 0.5e7], filter={'pos'})

    if hysteresis_gif:
        hyst_name = 'hyst_plane_60x60'

        # sample = 'ms=5e+02,_kan=8e+04,_jex=1e-01'
        # sample = 'ms=5e+05,_kan=8e+07,_jex=1e-01'
        sample = 'ms=5e+08,_kan=8e+04,_jex=1e-01'
        sample_out_folder = '{}/out_{}/{}'.format(main_out_folder, hyst_name, sample)
        sample_pic_folder = '{}/{}/{}'.format(pic_folder, hyst_name, sample)
        create_hysteresis_gif(sample_out_folder, sample_pic_folder,
                              borders=[20, 40, 20, 40], is3d=False)

    if cycle:
        par_name = 'off'
        addition = True
        start, end, step = 0, 5, 1
        cycle_one_parameter(par_name, start, end, step, addition,
                            settings_fname=settings, out_folder=out_folder)
        draw_multiple_plots_for_cycle(out_folder, par_name, start, end, step,
                                      addition, settings, save=False,
                                      pic_dir=pic_folder)

    end_of_drawing()
