import os

import optuna

import full_pipeline
from satd_utils import prop2dict


def objective(trial):
    clean_token_count_limit = int(trial.suggest_discrete_uniform('clean_token_count_limit', 100, 400, 1))
    default_embeddings_size = int(trial.suggest_discrete_uniform('default_embeddings_size', 64, 768, 1))

    accuracy = None
    evaluation, info, output = ('', '', [])
    error = ''
    try:
        evaluation, info, _ = full_pipeline.run(clean_token_count_limit, default_embeddings_size, output=output)
        accuracy_str = prop2dict(evaluation)['accuracy']
        accuracy = float(accuracy_str)
    except Exception as ex:
        import traceback
        error = traceback.format_exc()
        print('error-01', error)
        print('output-01-start', '\n'.join(output))
        print('output-01-end  --------------------------------------------')
        print('')
    # ALTER TABLE trial_user_attributes ALTER COLUMN value_json TYPE text;

    user_data = {
        'accuracy': accuracy,
        # -- store other results like this
        'os_uname': os.uname(),
        'clean_token_count_limit': clean_token_count_limit,
        'attachments': {'info': info, 'output': output}
    }
    if error != '':
        user_data['error'] = error
    trial.set_user_attr('user_data', user_data)

    return accuracy


if __name__ == '__main__':
    from optuna_properties import get_file_properties

    prop = get_file_properties()
    db_url = prop.db_url
    study_name = prop.study_name
    # db_url=sqlite:///example.db\nstudy_name=exp1
    # study_name = 'example-study'  # Unique identifier of the study.
    study = optuna.create_study(study_name=study_name, storage=db_url, load_if_exists=True, direction='maximize')
    study.optimize(objective, n_trials=10000)
    print('done')
