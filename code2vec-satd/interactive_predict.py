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

    def predict(self):
        in_dir = Path('build-dataset/java-small/test')
        out_dir = Path('build-dataset/java-small-evaluated/test')
        if out_dir.exists():
            shutil.rmtree(out_dir)
        out_dir.mkdir(parents=True)
        entries = sorted(os.listdir(in_dir))
        done = 0
        correct = 0
        for name in entries:
            if name.endswith('.java'):
                input_filename = os.path.join(in_dir, name)
                output_filename = os.path.join(out_dir, name)
                if self.predict_file(input_filename, output_filename):
                    correct += 1
                done += 1
                print('correct/done: %d/%d accuracy: %s %%  -- overall done/tot %%: %s' % (
                    correct, done, round(correct / done * 1000)/10, round(done / len(entries) * 1000)/10))

    def predict_file(self, input_filename, output_filename):
        print(input_filename, '--->', output_filename)
        output = []
        try:
            predict_lines, hash_to_string_dict = self.path_extractor.extract_paths(input_filename)
        except ValueError as e:
            print(e)
            print('press a key to continue...')
            input()
            return
        raw_prediction_results = self.model.predict(predict_lines)
        method_prediction_results = common.parse_prediction_results(
            raw_prediction_results, hash_to_string_dict,
            self.model.vocabs.target_vocab.special_words, topk=SHOW_TOP_CONTEXTS)

        for raw_prediction, method_prediction in zip(raw_prediction_results, method_prediction_results):
            prediction = method_prediction.predictions[0]['name'][0]
            actual = method_prediction.original_name
            output.append('Prediction:\t' + prediction)
            output.append('Actual:\t' + actual)
            for name_prob_pair in method_prediction.predictions:
                output.append('\t(%f) predicted: %s' % (name_prob_pair['probability'], name_prob_pair['name']))
            # output.append('Attention:')
            # for attention_obj in method_prediction.attention_paths:
            #     output.append('%f\tcontext: %s,%s,%s' % (
            #         attention_obj['score'], attention_obj['token1'], attention_obj['path'],
            #         attention_obj['token2']))
            # if self.config.EXPORT_CODE_VECTORS:
            #     output.append('Code vector:')
            #     output.append(' '.join(map(str, raw_prediction.code_vector)))
        output_body = '\n'.join(output)
        Path(output_filename).write_text(
            '/*\n' + output_body + '\n*/' +
            '\n\n' + Path(input_filename).read_text()
        )
        return prediction == actual
