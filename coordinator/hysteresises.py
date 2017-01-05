import time

from mamca import *

import os
import shutil
from queue import Queue
from threading import Thread


class Worker(Thread):
    """ Thread executing tasks from a given tasks queue """
    def __init__(self, tasks):
        Thread.__init__(self)
        self.tasks = tasks
        self.daemon = True
        self.start()

    def run(self):
        while True:
            func, args, kargs = self.tasks.get()
            try:
                func(*args, **kargs)
            except Exception as e:
                # An exception happened in this thread
                print(e)
            finally:
                # Mark this task as done, whether an exception happened or not
                self.tasks.task_done()


class ThreadPool:
    """ Pool of threads consuming tasks from a queue """
    def __init__(self, num_threads):
        self.tasks = Queue(num_threads)
        for _ in range(num_threads):
            Worker(self.tasks)

    def add_task(self, func, *args, **kargs):
        """ Add a task to the queue """
        self.tasks.put((func, args, kargs))

    def map(self, func, args_list):
        """ Add a list of tasks to the queue """
        for args in args_list:
            self.add_task(func, args)

    def wait_completion(self):
        """ Wait for completion of all the tasks in the queue """
        self.tasks.join()


def single_hyst_run():
    single_run(settings_fname=settings, out_folder=out_folder)


def single_draw_hyst():
    cur_folder = out_folder
    draw_hyst_plot(cur_folder, 'x', 'x',  # filter=[20, 20, 60, 60],
                   pic_dir=pic_folder,
                   save=True, name='0_hyst_plot')
    draw_hyst_plot(cur_folder, 'x', 'x', direction='fst',  # filter=[20, 20, 60, 60],
                   pic_dir=pic_folder,
                   save=True, name='1_hyst_plot_fst')
    draw_hyst_plot(cur_folder, 'x', 'x', direction='neg',  # filter=[20, 20, 60, 60],
                   pic_dir=pic_folder,
                   save=True, name='2_hyst_plot_neg')
    draw_hyst_plot(cur_folder, 'x', 'x', direction='pos',  # filter=[20, 20, 60, 60],
                   pic_dir=pic_folder,
                   save=True, name='3_hyst_plot_pos')


def create_gif():
    create_hysteresis_gif(out_folder, '{}/gif'.format(pic_folder),
                          borders=[30, 30, 30], negative_borders=True,
                          scale=2)


def prepare_folder(path):
    if os.path.exists(path):
        shutil.rmtree(path, ignore_errors=True)
    if not os.path.exists(path):
        os.mkdir(path)


if __name__ == '__main__':
    start_time = time.time()

    parent_out_folder = 'resources/out/hyst'
    out_folder = parent_out_folder
    pic_folder = 'resources/pictures/hysteresis'
    settings = 'resources/settings.json'

    cur_settings = 'resources/current_settings.json'

    # prepare_folder(parent_out_folder)
    # prepare_folder(pic_folder)

    def_s = Settings(settings)

    single_hyst_run()
    single_draw_hyst()
    create_gif()

    end_time = time.time()
    print(('-' * 50 + '\n') * 3)
    print('time of work is {:.2f} seconds'.format(end_time - start_time))

    # args = list(product(ms_s,
    #  kan_s, jex_s))
    # pool = ThreadPool(4)
    # for arg in args:
    #     pool.add_task(single_hyst_run, *arg)
    # pool.wait_completion()
    #
    # for arg in args:
    #     single_draw_hyst(*arg)

