from pathlib import Path

from satd_utils import prop2dict


class FileProperties(dict):
    def __init__(self, filename):
        super().__init__()
        path = Path(filename).absolute()
        if not path.exists():
            line = input('Insert [' + str(path) + '] content').split('\\n')
            content = '\n'.join([l.strip() for l in line])
            path.write_text(content)
        prop_dict = prop2dict(path.read_text())
        self.__dict__.update(prop_dict)
        self.update(prop_dict)


if __name__ == '__main__':
    fp = FileProperties('test-properties.txt')
    print(fp)
