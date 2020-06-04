import traceback

import os
from pathlib import Path
import shutil

from common import common
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
        in_dir = Path('build-dataset/java-small/test')
        out_dir = Path('build-dataset/java-small-evaluated/test')
        print('Starting evaluation of', in_dir)

        if out_dir.exists():
            shutil.rmtree(out_dir)
        out_dir.mkdir(parents=True)
        entries = sorted([e for e in os.listdir(in_dir) if e.endswith('.java')])
        assert len(predictions) == len(entries)
        done = 0
        correct = 0
        for name in entries:
            input_filename = os.path.join(in_dir, name)
            output_filename = os.path.join(out_dir, name)
            if self.predict_file(input_filename, output_filename, [predictions[done]]):
                correct += 1
            done += 1
        print('correct/done: %d/%d accuracy: %s %%  -- overall done/tot %%: %s' % (
            correct, done, round(correct / done * 1000) / 10, round(done / len(entries) * 1000) / 10))

    def predict_file(self, input_filename, output_filename, raw_prediction_results=None):
        # print(input_filename, '--->', output_filename)
        if raw_prediction_results is None:
            try:
                predict_lines, _ = self.path_extractor.extract_paths(input_filename)
            except ValueError as e:
                print(e)
                print('press a key to continue...')
                input()
                return
            raw_prediction_results = self.model.predict(predict_lines)

        output = []
        for raw_prediction in raw_prediction_results:

            prediction = raw_prediction.topk_predicted_words[0]
            actual = raw_prediction.original_name

            output.append('Prediction:\t' + prediction)
            output.append('Actual:\t' + actual)
            for (name, score) in zip(raw_prediction.topk_predicted_words, raw_prediction.topk_predicted_words_scores):
                if name != '<PAD_OR_OOV>':
                    output.append('\t(%f) predicted: [\'%s\']' % (score, name))

        output_body = '\n'.join(output)
        Path(output_filename).write_text(
            '/*\n' + output_body + '\n*/' +
            '\n\n' + Path(input_filename).read_text()
        )
        return prediction == actual
