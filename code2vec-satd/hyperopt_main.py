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


state = Path(__file__ + '.pickle.state')
if state.exists():
    trials = pickle.loads(state.read_bytes())
else:
    trials = Trials()

while True:
    print('trials of previous runs:', len(trials.results), trials.best_trial if len(trials.results)>0 else '')
    best = fmin(objective,
                space=hp.quniform('clean_token_count_limit', 20, 60, 1),
                algo=tpe.suggest,
                max_evals=len(trials.results) + 1,
                show_progressbar=False,
                trials=trials)
    state.write_bytes(pickle.dumps(trials))
    print(best)
