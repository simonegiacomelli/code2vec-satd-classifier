import os
import subprocess
import shutil

output_file = os.path.abspath('./build-dataset/java-small.output.txt')
output_file_final = os.path.abspath('./build-dataset/java-small/output.txt')
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
            print(line, end='')
            f.write(line)
    p.wait()
    exit_status = p.returncode

    if exit_status != 0:
        raise Exception(f'Exit status {exit_status} for {command}')


# test exit code
# run_command(['./gradlew', 'MainGenDatasetArgs', f'-Parguments=--exit_status 7'], cwd='../satd-classifier')


run_command(['./gradlew', 'MainGenDatasetArgs', f"-Parguments=--clean_token_count_limit 20"], cwd='../satd-classifier')
run_command('./preprocess-only-histograms.sh')
run_command('./train.sh')
run_command('./evaluate_trained_model.sh')
shutil.move(output_file, output_file_final)
