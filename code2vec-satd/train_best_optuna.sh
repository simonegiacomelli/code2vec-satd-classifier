#!/usr/bin/env bash
###########################################################
# Change the following values to train a new model.
# type: the name of the new model, only affects the saved file name.
# dataset: the name of the dataset, as was preprocessed using preprocess.sh
# test_data: by default, points to the validation set, since this is the set that
#   will be evaluated after each training iteration. If you wish to test
#   on the final (held-out) test set, change 'val' to 'test'.
type=java-small
dataset_name=java-small
data_dir=data/${dataset_name}
data=${data_dir}/${dataset_name}
test_data=${data_dir}/${dataset_name}.val.c2v
model_dir=models/${type}

mkdir -p models/${model_dir}
set -e

GO="python3 -u code2vec.py --data ${data} --test ${test_data} --save ${model_dir}/saved_model --num-train-epochs 30 --default_embeddings_size 112 --max-context 200 --dropout-keep-rate 0.2 --framework keras --tensorboard"
#open--max-context 300 --dropout-keep-rate 0.2

#GO="python3 -u code2vec.py --data ${data} --test ${test_data} --save ${model_dir}/saved_model "
echo $GO
$GO