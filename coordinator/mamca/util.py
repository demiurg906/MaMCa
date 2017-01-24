def which(program):
    """
    Функция ищет путь к программе в системном PATH
    :param program:
    :return:
    """
    import os
    import sys
    if 'win' in sys.platform:
        extension = ['', 'exe', 'msi', 'bat']
    else:
        extension = ['', 'sh']

    def is_exe(fpath):
        return os.path.isfile(fpath) and os.access(fpath, os.X_OK)

    fpath, fname = os.path.split(program)
    if fpath:
        if is_exe(program):
            return program
    else:
        for path in os.environ["PATH"].split(os.pathsep):
            path = path.strip('"')
            exe_file = os.path.join(path, program)
            for exe in extension:
                exe_fpath = '{}.{}'.format(exe_file, exe)
                if is_exe(exe_fpath):
                    return exe_fpath

    return None