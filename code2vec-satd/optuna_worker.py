import os

import optuna

import full_pipeline
from satd_utils import prop2dict


def objective(trial):

    clean_token_count_limit = int(trial.suggest_discrete_uniform('clean_token_count_limit', 20, 60, 1))

    clean_token_count_limit = int(clean_token_count_limit)
    evaluation, info, output = full_pipeline.run(clean_token_count_limit)
    accuracy_str = prop2dict(evaluation)['accuracy']
    accuracy = float(accuracy_str)
    # ALTER TABLE trial_user_attributes ALTER COLUMN value_json TYPE text;
    loss = 1.0 - accuracy
    user_data = {
        'loss': loss,
        # -- store other results like this
        'os_uname': os.uname(),
        'evaluation': evaluation,
        'attachments': {'info': info, 'output': output}
    }
    trial.set_user_attr('user_data', user_data)
    return loss

if __name__ == '__main__':
    from optuna_properties import get_file_properties

    prop = get_file_properties()
    db_url = prop.db_url
    study_name = prop.study_name
    # db_url=sqlite:///example.db\nstudy_name=exp1
    # study_name = 'example-study'  # Unique identifier of the study.
    study = optuna.create_study(study_name=study_name, storage=db_url, load_if_exists=True)
    study.optimize(objective, n_trials=10000)
