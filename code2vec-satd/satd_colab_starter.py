import os
import urllib.request


def system_log(cmd, raise_exception=True):
    print('Executing', cmd)
    code = os.system(cmd)
    if raise_exception and code != 0:
        raise Exception('Bad exit code', code)
    return code


def main():
    print(f'ok, start! I\'m [{os.uname()}]')
    urllib.request.urlretrieve('http://e.jako.pro:8000/optuna-properties.txt', 'optuna-properties.txt')
    from optuna_properties import prefetch_file_properties
    prefetch_file_properties()
    system_log(
        'cd /content/code2vec-satd-classifier/satd-classifier && cd data/pgsql || unzip -q ./pgsql_binaries/pgsql_linux.zip -d ./data && echo unzip done')

    # check user postgres
    if system_log('runuser -l postgres -c "cd"', raise_exception=False) == 0:
        print('user postgres do exists.')
    else:
        print('user postgres do not exists. Going to create it')
        create_postgres_user = """useradd -s /bin/bash -p $(openssl passwd -1 postgres) postgres
        mkdir /home/postgres 
        chown postgres:postgres -R /home/postgres"""

        for cmd in create_postgres_user.split('\n'):
            code = system_log(cmd.strip())
    print('pgsql out exists:', os.path.exists('/content/pgsql-out.txt'))
    if not os.path.exists('/content/pgsql-out.txt'):
        system_log(
            "cd /content/code2vec-satd-classifier && python3 code2vec-satd/colab/utils/download_http_server.py --url http://e.jako.pro:8001/ --folder ./satd-classifier/data/backup/bk1  > /content/download-backup-out.txt 2>&1")
        # sadly, for how the pgsql restore program works, it is expected to receive an error exit code
        system_log('chown -R postgres:postgres /content')
        system_log(
            "runuser -l postgres -c 'cd /content/code2vec-satd-classifier/satd-classifier && ./gradlew pgsqlRestore --console=plain -Dorg.gradle.daemon=false > /content/pgsql-restore-out.txt 2>&1'",
            raise_exception=False)
        get_ipython().system_raw(
            "runuser -l postgres -c 'cd /content/code2vec-satd-classifier/satd-classifier && ./gradlew pgsqlStartInstance --console=plain -Dorg.gradle.daemon=false  </dev/null > /content/pgsql-out.txt 2>&1 & disown '")

    system_log('pip install optuna  > /content/pip-install-optuna-out.txt 2>&1')
    system_log('pip install tensorflow==2.1.0 > /content/pip-install-tensorflow.txt 2>&1')

    print('done')


if __name__ == '__main__':
    main()
