# https://github.com/hyperopt/hyperopt/wiki/FMin#12-attaching-extra-information-via-the-trials-object
# https://github.com/hyperopt/hyperopt/wiki/FMin#13-the-trials-object
import pickle
import time
from pathlib import Path
from hyperopt import fmin, tpe, hp, STATUS_OK, Trials

import full_pipeline
from satd_utils import prop2dict
import os


def objective(clean_token_count_limit):
    return {
        'loss': float(clean_token_count_limit) ** 2,
        'status': STATUS_OK,
        # -- store other results like this
        'os_uname': os.uname(),
    }
    clean_token_count_limit = int(clean_token_count_limit)
    evaluation, info, output = full_pipeline.run(clean_token_count_limit)
    accuracy_str = prop2dict(evaluation)['accuracy']
    accuracy = float(accuracy_str)
    return {
        'loss': 1.0 - accuracy,
        'status': STATUS_OK,
        # -- store other results like this
        'os_uname': os.uname(),
        'evaluation': evaluation,
        'attachments': {'info': info, 'output': output}
    }

from hyperopt.mongoexp import MongoTrials
import hyperopt
from hyperopt_mongo_prop import MongoProp

mongo_url = MongoProp().mongo_url()
print('mongo_url', mongo_url)

trials = MongoTrials(mongo_url, exp_key='exp1')

while True:
    best_trial = ''
    try:
        best_trial = 'best trial: ' + str(trials.best_trial)
    except hyperopt.exceptions.AllTrialsFailed:
        pass
    prev_len = len(trials.results)
    max_eval = prev_len + 2
    print('trials of previous runs:', prev_len, best_trial)
    print('next max_eval',max_eval)
    best = fmin(objective,
                space=hp.quniform('clean_token_count_limit', 20, 60, 1),
                algo=tpe.suggest,
                max_evals=max_eval,
                show_progressbar=False,
                trials=trials,
                max_queue_len=10)
    print(best)
