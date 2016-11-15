default_out_folder = 'out'
default_settings_fname = 'settings.txt'
default_mamca_path = './MaMCa.exe'


def get_default_out_folder():
    return default_out_folder


def get_default_settings_file():
    return default_settings_fname


def get_default_mamca_path():
    return default_mamca_path


def set_default_out_folder(path):
    global default_out_folder
    default_out_folder = path


def set_default_settings_file(path):
    global default_settings_fname
    default_settings_fname = path


def set_default_mamca_path(path):
    global default_mamca_path
    default_mamca_path = path
