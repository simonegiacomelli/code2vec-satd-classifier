import os


def run_command(command):
    exit_status = os.system(command)
    if exit_status != 0:
        raise Exception(f'Exit status {exit_status} for {command}')


def run_gradle(task, arguments):
    run_command(f'cd ../satd-classifier && ./gradlew {task} -Parguments="{arguments}"')


# run_gradle('MainGenDatasetArgs', '--exit_status 7') # test exit code

run_gradle('MainGenDatasetArgs', '--clean_token_count_limit 20')
run_command('./preprocess-only-histograms.sh')
