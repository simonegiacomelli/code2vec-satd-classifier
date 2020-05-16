import shutil
from distutils.dir_util import copy_tree

shutil.rmtree('./bin', ignore_errors=True)
copy_tree('./target/classes','./bin/classes')

