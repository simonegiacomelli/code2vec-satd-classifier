import os
import subprocess
import shutil
from pathlib import Path


def run(clean_token_count_limit, default_embeddings_size=256, verbose=False, output=[], num_train_epochs=20):
    dataset_path = Path('./build-dataset/java-small')
    dataset_path.mkdir(parents=True, exist_ok=True)
    output_file = os.path.abspath('%s.output.txt' % dataset_path)
    output_file_final = os.path.abspath('%s/output.txt' % dataset_path)
    with open(output_file, 'w') as f:
        f.write(f'Start {__file__}\n')

    # shutil.rmtree('./models')

    # threading.Thread(target=lambda: os.system(f'tail -F {output_file}'), daemon=True).start()

    def run_command(command, cwd=None):
        command_str = command
        if isinstance(command, list):
            command_str = ' '.join(command)
        print(f'executing [{command_str}]')
        p = subprocess.Popen(command, cwd=cwd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        with open(output_file, 'a') as f:
            f.write(f'Executing [{command_str}]\n')
            for line in iter(p.stdout.readline, b''):
                line = line.decode('utf-8')
                if verbose:
                    print(line, end='')
                output.append(line)
                f.write(line)
                f.flush()
        p.wait()
        exit_status = p.returncode

        if exit_status != 0:
            raise Exception(f'Exit status {exit_status} for {command}')

    dataset_name = 'java-small'
    model_dir = 'models/' + dataset_name

    def run_train():
        data_dir = 'data/' + dataset_name
        data = data_dir + '/' + dataset_name
        test_data = data_dir + '/' + dataset_name + '.val.c2v'
        os.makedirs(model_dir, exist_ok=True)

        GO = "python3 -u code2vec.py --data %s --test %s --save %s/saved_model --default_embeddings_size %s --framework keras --tensorboard" \
             " --num-train-epochs %d" % \
             (data, test_data, model_dir, str(default_embeddings_size), num_train_epochs)
        run_command(GO.split(' '))

    # test exit code
    # run_command(['./gradlew', 'MainGenDatasetArgs', f'-Parguments=--exit_status 7'], cwd='../satd-classifier')

    run_command(['./gradlew'
                    , 'MainGenDatasetArgs'
                    , f'-Parguments=--clean_token_count_limit {clean_token_count_limit}'
                    , '--console=plain'],
                cwd='../satd-classifier')
    run_command('./preprocess-only-histograms.sh')
    # run_command('./train.sh')
    run_train()
    # run_command('./evaluate_trained_model.sh')
    run_command(('python3 code2vec.py --framework keras --load %s/saved_model --predict --default_embeddings_size %s'
                 % (model_dir, str(default_embeddings_size))).split(' '))
    shutil.move(output_file, output_file_final)

    evaluation = (dataset_path / 'evaluation.txt').read_text()
    evaluation_detail = (dataset_path / 'evaluation_detail.txt').read_text()
    info = (dataset_path / 'info.txt').read_text()
    # output = (dataset_path / 'output.txt').read_text()
    return evaluation, evaluation_detail, info, output


if __name__ == '__main__':
    evaluation = run(clean_token_count_limit=40, default_embeddings_size=64, verbose=True, num_train_epochs=5)[0]
    print('evaluation', evaluation)
