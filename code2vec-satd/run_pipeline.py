import os


def run_gradle(task, arguments):
    exit_status = os.system(f'cd ../satd-classifier && ./gradlew {task} -Parguments="{arguments}"')
    print('exit_status =', exit_status)


run_gradle('MainGenDatasetArgs', '--exit_status 1')
