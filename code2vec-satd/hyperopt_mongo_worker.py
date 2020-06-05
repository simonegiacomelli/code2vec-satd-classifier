#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import re
import sys
import os
from hyperopt.mongoexp import main
from hyperopt_mongo_prop import MongoProp

if __name__ == '__main__':
    print('sys.argv', sys.argv)
    sys.argv[0] = os.path.abspath(re.sub(r'(-script\.pyw|\.exe)?$', '', sys.argv[0]))
    print('new  sys.argv[0]', sys.argv[0])
    sys.argv.append('--mongo=' + MongoProp().mongo_url_worker())
    sys.argv.append('--workdir=' + os.getcwd())
    sys.exit(main())
