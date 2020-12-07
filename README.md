
cat ~/Documents/tesi/code2vec-satd-classifier/code2vec-satd/www-public/optuna-properties.txt

# server properties file for optuna distributed computation
cd ~/Documents/tesi/code2vec-satd-classifier/code2vec-satd/www-public
python -m http.server


# serve dataset 
cd ~/Documents/tesi/code2vec-satd-classifier/satd-classifier/data/backup/bk1/
python -m http.server 8001
