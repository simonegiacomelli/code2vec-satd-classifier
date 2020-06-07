import os

import optuna
from optuna import Trial

import full_pipeline
from satd_utils import prop2dict


def objective(trial: Trial):
    # x = trial.suggest_uniform('x', -10, 10)
    # return (x - 2) ** 2
    clean_token_count_limit = int(trial.suggest_discrete_uniform('clean_token_count_limit', 20, 60000, 1))
    trial.set_user_attr('run', {
        'loss': 1.2,
        # -- store other results like this
        'os_uname': os.uname(),
        'clean_token_count_limit': clean_token_count_limit,
        'attachments': {'info': 'info', 'output': 'output'}
    })
    if clean_token_count_limit < 10000:
        return None
    return float(clean_token_count_limit) ** 2


if __name__ == '__main__':
    study = optuna.create_study(study_name='toy-experiment', storage='sqlite:///example.db', load_if_exists=True)
    study.optimize(objective, n_trials=10000)
