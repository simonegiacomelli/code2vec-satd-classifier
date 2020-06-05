from pathlib import Path

from satd_utils import prop2dict


class MongoProp:
    def __init__(self):
        path = Path('mongo.txt')
        if not path.exists():
            line = input('Insert mongo.properties content').split('\\n')
            content = '\n'.join([l.strip() for l in line])
            path.write_text(content)
        prop_prop = prop2dict(path.read_text())
        self.username, self.password, self.hostname = \
            prop_prop['username'], prop_prop['password'], prop_prop['hostname']

    def mongo_url(self):
        return f'mongo://{self.username}:{self.password}@{self.hostname}/foo_db/jobs?authSource=admin'

    def mongo_url_worker(self):
        return f'mongo://{self.username}:{self.password}@{self.hostname}/foo_db/jobs?authSource=admin&skip'
