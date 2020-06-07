import os


def system_log(cmd, raise_exception=True):
    print('Executing', cmd)
    code = os.system(cmd)
    if raise_exception and code != 0:
        raise Exception('Bad exit code', code)
    return code


def main():
    print('ok, start!')
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

    print('pgsql out exists:', os.path.exists('/home/postgres/pgsql-out.txt'))
    if not os.path.exists('/home/postgres/pgsql-out.txt'):
        print('checking sources for user postgres and db files')
        system_log(
            'runuser -l postgres -c "cd; cd code2vec-satd-classifier && git pull || git clone https://github.com/simonegiacomelli/code2vec-satd-classifier"')
        system_log(
            "runuser -l postgres -c 'cd; cd ./code2vec-satd-classifier/satd-classifier && cd data/pgsql || unzip -q ./pgsql_binaries/pgsql_linux.zip -d ./data && echo unzip done' ")
        system_log("runuser -l postgres -c 'cd ./code2vec-satd-classifier &&  python3 code2vec-satd/colab/utils/download_http_server.py --url  http://foo.inf.usi.ch:8000/ --folder ./satd-classifier/data/backup/bk1'")
        # sadly, for how the pgsql restore program works, it is expected to receive an error exit code
        system_log("runuser -l postgres -c 'cd ./code2vec-satd-classifier/satd-classifier && ./gradlew pgsqlRestore --console=plain' > ~/pgsql-restore-out.txt 2>&1", raise_exception=False)

        get_ipython().system_raw(
            "runuser -l postgres -c 'cd ./code2vec-satd-classifier/satd-classifier && ./gradlew showdatabase  </dev/null > ~/pgsql-out.txt 2>&1 & disown '")

    system_log('pip install optuna')
    system_log('pip install tensorflow==2.1.0')

    print('done')


if __name__ == '__main__':
    main()
