from mamca import *

if __name__ == '__main__':
    single_run()
    out_folder = get_default_out_folder()
    # draw_3d_vectors_plot('{}/momenta.txt'.format(out_folder), settings_fname='./resources/settings.json',
    #                      save=True, pic_dir='./resources/pictures', name='exchange_3')
    draw_3d_vectors_plot(
        '{}/momenta.txt'.format(get_default_out_folder()),
        borders=[5, 5, 5], scale=3
    )
    end_of_drawing()
