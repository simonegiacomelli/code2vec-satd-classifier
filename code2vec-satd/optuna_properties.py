from file_properties import FileProperties


def get_file_properties():
    prop = FileProperties('optuna-properties.txt')
    return prop


if __name__ == '__main__':
    get_file_properties()