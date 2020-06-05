from pathlib import Path

from satd_utils import prop2dict


class MongoProp:
    def __init__(self):
        prop_prop = prop2dict(Path('mongo.properties').read_text())
        self.username, self.password, self.hostname = \
            prop_prop['username'], prop_prop['password'], prop_prop['hostname']

    def mongo_url(self):
        return f'mongo://{self.username}:{self.password}@{self.hostname}/foo_db/jobs?authSource=admin'

    def mongo_url_worker(self):
        return f'mongo://{self.username}:{self.password}@{self.hostname}/foo_db/jobs?authSource=admin&skip'
