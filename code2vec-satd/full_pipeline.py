import os
import subprocess
import shutil
from pathlib import Path


def run(clean_token_count_limit, verbose=False):
    dataset_path = Path('./build-dataset/java-small')
    dataset_path.mkdir(parents=True, exist_ok=True)
    output_file = os.path.abspath('%s.output.txt' % dataset_path)
    output_file_final = os.path.abspath('%s/output.txt' % dataset_path)
    with open(output_file, 'w') as f:
        f.write('Start')

    # threading.Thread(target=lambda: os.system(f'tail -F {output_file}'), daemon=True).start()

    def run_command(command, cwd=None):
        print(f'executing {command}')
        p = subprocess.Popen(command, cwd=cwd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        with open(output_file, 'a') as f:
            f.write(f'Executing {command}\n')
            for line in iter(p.stdout.readline, b''):
                line = line.decode('utf-8')
                if verbose:
                    print(line, end='')
                f.write(line)
        p.wait()
        exit_status = p.returncode

        if exit_status != 0:
            raise Exception(f'Exit status {exit_status} for {command}')

    # test exit code
    # run_command(['./gradlew', 'MainGenDatasetArgs', f'-Parguments=--exit_status 7'], cwd='../satd-classifier')

    run_command(['./gradlew'
                    , 'MainGenDatasetArgs'
                    , f'-Parguments=--clean_token_count_limit {clean_token_count_limit}'
                    , '--console=plain'],
                cwd='../satd-classifier')
    run_command('./preprocess-only-histograms.sh')
    run_command('./train.sh')
    run_command('./evaluate_trained_model.sh')
    shutil.move(output_file, output_file_final)

    evaluation = (dataset_path / 'evaluation.txt').read_text()
    info = (dataset_path / 'info.txt').read_text()
    output = (dataset_path / 'output.txt').read_text()
    return evaluation, info, output


if __name__ == '__main__':
    print(run(20))