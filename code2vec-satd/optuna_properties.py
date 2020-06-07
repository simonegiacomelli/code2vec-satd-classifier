from file_properties import FileProperties


def get_file_properties():
    prop = FileProperties('optuna-properties.txt')
    return prop

def prefetch_file_properties():
    get_file_properties()

if __name__ == '__main__':
    get_file_properties()