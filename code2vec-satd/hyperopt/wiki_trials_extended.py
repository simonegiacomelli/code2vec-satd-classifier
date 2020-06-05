# https://github.com/hyperopt/hyperopt/wiki/FMin#12-attaching-extra-information-via-the-trials-object
# https://github.com/hyperopt/hyperopt/wiki/FMin#13-the-trials-object
import pickle
import time
from pathlib import Path
from hyperopt import fmin, tpe, hp, STATUS_OK, Trials


def objective(x):
    return {
        'loss': (x - 1) ** 2,
        'status': STATUS_OK,
        # -- store other results like this
        'eval_time': time.time(),
        'other_stuff': {'type': None, 'value': [0, 1, 2]},
        # -- attachments are handled differently
        'attachments':
            {'time_module': pickle.dumps(time.time)}
    }


state = Path(__file__ + '.pickle.state')
if state.exists():
    trials = pickle.loads(state.read_bytes())
else:
    trials = Trials()
print('trials of previous runs:', len(trials.results))
best = fmin(objective,
            space=hp.quniform('x', -100000, 100000, 1),
            algo=tpe.suggest,
            max_evals=len(trials.results) + 100,
            trials=trials)
state.write_bytes(pickle.dumps(trials))
print(best)
