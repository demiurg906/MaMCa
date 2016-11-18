from mamca import *

if __name__ == '__main__':
    single_run()
    draw_3d_vectors_plot('{}/momenta.txt'.format(get_default_out_folder()))
    end_of_drawing()
