import os

import optuna

import full_pipeline
from satd_utils import prop2dict


def objective(trial):
    # x = trial.suggest_uniform('x', -10, 10)
    # return (x - 2) ** 2

    clean_token_count_limit = int(trial.suggest_discrete_uniform('clean_token_count_limit', 20, 60000, 1))
    return float(clean_token_count_limit) ** 2


def main():
    from file_properties import FileProperties
    from optuna_properties import get_file_properties
    db_url = get_file_properties().db_url
    # db_url=sqlite:///example.db\nstudy_name=exp1
    # study_name = 'example-study'  # Unique identifier of the study.
    study = optuna.create_study(study_name='toy-db-experiment', storage=db_url, load_if_exists=True)
    study.optimize(objective, n_trials=1000)


if __name__ == '__main__':
    main()

