import traceback
from pathlib import Path


def open_file():
    res = Path('non-exist').read_text()
    return res


def main():
    print(open_file())


if __name__ == '__main__':
    try:
        main()
    except Exception :
        # print(str(ex))
        print('aa',traceback.format_exc())