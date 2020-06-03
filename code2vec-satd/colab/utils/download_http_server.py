import urllib.request
from html.parser import HTMLParser
import os


class CatchStartTag(HTMLParser):
    def __init__(self, href_callback):
        super().__init__()
        self.href_callback = href_callback

    def handle_starttag(self, tag, attrs):
        if tag == 'a':
            self.href_callback(dict(attrs)['href'])


def download_files_in_url(start_url, start_folder):
    todo = []

    def add(u, f):
        print(u, f)
        todo.append((u, f))

    add(start_url, start_folder)
    while len(todo) > 0:
        url, folder = todo.pop()
        if url.endswith('/'):
            with urllib.request.urlopen(url) as fp:
                html = fp.read().decode('utf8')
            CatchStartTag(lambda h: add(url + h,
                                        os.path.join(folder, h) if h.endswith('/') else folder)).feed(html)
        else:
            os.makedirs(folder, exist_ok=True)
            filename = os.path.join(folder, url.split('/')[-1])
            if not os.path.exists(filename):
                print('downloading', url, 'to', filename)
                urllib.request.urlretrieve(url, filename)


if __name__ == '__main__':
    # download_files_in_url('http://foo.inf.usi.ch:8000/', './temp/download')
    import argparse

    parser = argparse.ArgumentParser(description="Synthetic Dataset generation")
    parser.add_argument("--url", type=str, dest="url", required=True, help="url to download from")
    parser.add_argument("--folder", type=str, dest="folder", required=True, help="folder to download to")
    args = parser.parse_args()
    download_files_in_url(args.url, args.folder)
