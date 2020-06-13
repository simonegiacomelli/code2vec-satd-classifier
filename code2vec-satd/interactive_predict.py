import os
import shutil
from pathlib import Path

from extractor import Extractor

SHOW_TOP_CONTEXTS = 10
MAX_PATH_LENGTH = 8
MAX_PATH_WIDTH = 2
JAR_PATH = 'JavaExtractor/JPredict/target/JavaExtractor-0.0.1-SNAPSHOT.jar'


class InteractivePredictor:
    exit_keywords = ['exit', 'quit', 'q']

    def __init__(self, config, model):
        model.predict([])
        self.model = model
        self.config = config
        self.path_extractor = Extractor(config,
                                        jar_path=JAR_PATH,
                                        max_path_length=MAX_PATH_LENGTH,
                                        max_path_width=MAX_PATH_WIDTH)

    def read_file(self, input_filename):
        with open(input_filename, 'r') as file:
            return file.readlines()

    def predict_fast(self):
        test_data_path = 'data/java-small/java-small.test.c2v'
        with open(test_data_path, 'r') as f:
            lines = f.read().splitlines()
        predictions = self.model.predict(lines)
        return predictions

    def predict(self):
        predictions = self.predict_fast()
        dataset_path = 'build-dataset/java-small'
        in_dir = Path('%s/test' % dataset_path)
        out_dir = Path('%s-evaluated/test' % dataset_path)
        print('Starting evaluation of', in_dir)

        if out_dir.exists():
            shutil.rmtree(out_dir)
        out_dir.mkdir(parents=True)
        entries = sorted([e for e in os.listdir(in_dir) if e.endswith('.java')])
        assert len(predictions) == len(entries)
        done = 0
        correct = 0
        eval_detail = (Path(dataset_path) / 'evaluation_detail.txt').open('w')
        eval_detail.write(f'index\tsatd_id\tpredicted\tactual\tconfidence\n')
        for name in entries:
            input_filename = os.path.join(in_dir, name)
            output_filename = os.path.join(out_dir, name)
            if self.predict_file(input_filename, output_filename, [predictions[done]], eval_detail):
                correct += 1
            done += 1
        eval_detail.close()
        accuracy = round(correct / done * 1000) / 1000
        print('correct/done: %d/%d accuracy: %s %%  -- overall done/tot %%: %s' % (
            correct, done, accuracy * 100, round(done / len(entries) * 1000) / 10))
        (Path(dataset_path) / 'evaluation.txt').write_text(f'accuracy={accuracy}')

    def predict_file(self, input_filename, output_filename, raw_prediction_results, eval_detail):
        # print(input_filename, '--->', output_filename)
        output = []
        confidence = []
        for raw_prediction in raw_prediction_results:

            prediction = raw_prediction.topk_predicted_words[0]
            actual = raw_prediction.original_name

            output.append('Prediction:\t' + prediction)
            output.append('Actual:\t' + actual)
            for (name, score) in zip(raw_prediction.topk_predicted_words, raw_prediction.topk_predicted_words_scores):
                if name != '<PAD_OR_OOV>':
                    output.append('\t(%f) predicted: [\'%s\']' % (score, name))
                    confidence.append(f'{score}:{name}')

        output_body = '\n'.join(output)
        Path(output_filename).write_text(
            '/*\n' + output_body + '\n*/' +
            '\n\n' + Path(input_filename).read_text('utf-8')
        )
        index, satd_id = map(int, input_filename.split('_')[:2])
        conf = ';'.join(confidence)
        eval_detail.write(f'{index}\t{satd_id}\t{prediction}\t{actual}\t{conf}\n')
        return prediction == actual
