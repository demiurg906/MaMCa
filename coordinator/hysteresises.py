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


def single_hyst_run(ms, kan, jex):
    cur_folder = out_folder.format(ms, kan, jex)
    if not os.path.exists(cur_folder):
        os.mkdir(cur_folder)
    cur_settings = '{0}/{1}.txt'.format(parent_out_folder,
                                        template.format(ms, kan, jex))
    s = Settings(settings)
    s['ms'] = ms
    s['kan'] = kan
    s['jex'] = jex
    s.save_settings(cur_settings)
    hysteresis_log_run(out_folder=cur_folder, settings_fname=cur_settings,
                       k=5, prec=1)


def single_draw_hyst(ms, kan, jex):
    cur_folder = out_folder.format(ms, kan, jex)
    cur_settings = '{0}/{1}.txt'.format(parent_out_folder,
                                        template.format(ms, kan, jex))
    draw_hyst_plot(cur_folder, 'x', 'x', filter=[20, 20, 60, 60],
                   settings_fname=cur_settings, pic_dir=pic_folder,
                   save=True, name=template.format(ms, kan, jex))


def prepare_folder(path):
    if os.path.exists(path):
        shutil.rmtree(path)
    os.mkdir(path)

if __name__ == '__main__':
    template = 'ms={:.0e},_kan={:.0e},_jex={:.0e}'
    parent_out_folder = 'out_hysts_'
    out_folder = parent_out_folder + '/' + template
    pic_folder = 'Screenshots/hysteresises_'
    settings = 'settings.txt'

    prepare_folder(parent_out_folder)
    prepare_folder(pic_folder)

    tens = [10 ** (i - 3) for i in range(7)]
    def_s = Settings(settings)

    to_grad = lambda x: [x * ten for ten in tens]
    ms_s = to_grad(def_s['ms'])
    kan_s = to_grad(def_s['kan'])
    jex_s = to_grad(def_s['jex'])
    args = [(ms, def_s['kan'], def_s['jex']) for ms in ms_s] + \
           [(def_s['ms'], kan, def_s['jex']) for kan in kan_s] + \
           [(def_s['ms'], def_s['kan'], jex) for jex in jex_s]
    # args = list(product(ms_s, kan_s, jex_s))

    pool = ThreadPool(4)
    for arg in args:
        pool.add_task(single_hyst_run, *arg)
    pool.wait_completion()

    for arg in args:
        single_draw_hyst(*arg)

