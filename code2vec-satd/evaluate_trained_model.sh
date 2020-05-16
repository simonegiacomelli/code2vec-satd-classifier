# tensorflow
#python3 code2vec.py --load models/java-small/saved_model_iter8 --test data/java-small/java-small.train.c2v

# keras
#python3 code2vec.py --framework keras --load models/java-small/saved_model --test data/java-small/java-small.test.c2v

#modified prediction
python3 code2vec.py --framework keras --load models/java-small/saved_model --predict
exit $?