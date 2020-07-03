import os

import optuna

import full_pipeline
from satd_utils import prop2dict

if __name__ == '__main__':
    from optuna_properties import get_file_properties
    from optuna.visualization import *

    prop = get_file_properties()
    db_url = prop.db_url
    study_name = prop.study_name
    # db_url=sqlite:///example.db\nstudy_name=exp1
    # study_name = 'example-study'  # Unique identifier of the study.
    study = optuna.create_study(study_name=study_name, storage=db_url, load_if_exists=True)
    plot_optimization_history(study)
    print('done')

