import optuna
from optuna import Trial
from optuna.trial import FrozenTrial, TrialState

if __name__ == '__main__':
    from optuna_properties import get_file_properties

    prop = get_file_properties()
    db_url = prop.db_url
    study_name = prop.study_name
    study = optuna.load_study(study_name=study_name, storage=db_url)
    import os
    import json

    base_folder = f'optuna_studies/{study_name}'
    os.makedirs(base_folder, exist_ok=True)
    for trial in study.trials:
        trial: FrozenTrial
        if trial.state == TrialState.COMPLETE:
            ud = trial.user_attrs['user_data']
            with open(base_folder + f'/{trial.number}.txt', 'w') as f:
                f.write(json.dumps({k: v for k, v in ud.items() if k != 'attachments'}, indent=4))
                att = ud['attachments']
                f.write(att['info'])
                f.write(''.join(att['output']))

    print('done')
