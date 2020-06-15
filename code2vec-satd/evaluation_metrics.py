from pathlib import Path


class RelevanceMeasures:
    def __init__(self, tp, tn, fp, fn):
        self.tp = tp
        self.tn = tn
        self.fp = fp
        self.fn = fn
        tp, tn, fp, fn = map(float, [tp, tn, fp, fn])
        self.precision = tp / (tp + fp)
        self.recall = tp / (tp + fn)
        self.accuracy = (tp + tn) / (tp + tn + fp + fn)
        self.f1 = 2 * (self.precision * self.recall) / (self.precision + self.recall)

    def round(self):
        self.precision = round(self.precision * 10000.) / 100
        self.recall = round(self.recall * 10000.) / 100
        self.accuracy = round(self.accuracy * 10000.) / 100
        self.f1 = round(self.f1 * 10000.) / 100
        return self

    def csv(self):
        return ','.join(
            map(str, [self.tp, self.tn, self.fp, self.fn, self.precision, self.recall, self.accuracy, self.f1]))


def _pred_conf(class_name, content):
    d = {k: v for k, v in [kv.split(':') for kv in content.split(';')]}
    if class_name in d:
        return float(d[class_name])
    else:
        return 0.


def main(content):
    confidences = [0.5, 0.6, 0.7, 0.8, 0.9, 0.0]
    lines_str = content.split('\n')[1:]  # skip header
    lines = [line.split('\t') for line in lines_str if len(line) > 0]

    # [tp, tn, fp, fn]
    for confidence in confidences:
        for posi_class in ['satd', 'fixed']:
            metrics = [0, 0, 0, 0]
            for line in lines:
                index = 0
                # [index	satd_id	predicted	actual	confidence ]
                # ['1', '3', 'fixed', 'satd', 'fixed:0.639131;satd:0.359158']
                actual = line[3]
                predicted = line[2]
                predicted_confidence = _pred_conf(predicted, line[4])
                if predicted_confidence >= confidence:
                    index = (0 if actual == predicted else 2) + \
                            (0 if actual == posi_class else 1)
                    metrics[index] += 1

            print(f'{posi_class},{confidence},{RelevanceMeasures(*metrics).round().csv()}'.replace(',', '\t'))


if __name__ == '__main__':
    main(Path('./build-dataset/java-small/evaluation_detail.txt').read_text())

"""
run_id	positive_class	confidence	tp	tn	fp	fn	precision	recall	accuracy	f1
18	    satd	        0.5	        927	862	477	539	66.03	    63.23	63.78	    64.6
18	    fixed	        0.5	        862	927	539	477	61.53	    64.38	63.78	    62.92
18	    satd	        0.6	        869	812	436	489	66.59	    63.99	64.5	    65.26
18	    fixed	        0.6	        812	869	489	436	62.41	    65.06	64.5	    63.71
18	    satd	        0.7	        823	755	378	425	68.53	    65.95	66.27	    67.21
18	    fixed	        0.7	        755	823	425	378	63.98	    66.64	66.27	    65.28
18	    satd	        0.8	        776	685	330	370	70.16	    67.71	67.61	    68.92
18	    fixed	        0.8	        685	776	370	330	64.93	    67.49	67.61	    66.18
18	    satd	        0.9	        674	589	256	295	72.47	    69.56	69.63	    70.98
18	    fixed	        0.9	        589	674	295	256	66.63	    69.7	69.63	    68.13
18	    satd	        0.0	        933	865	480	548	66.03	    63.0	63.62	    64.48
18	    fixed	        0.0	        865	933	548	480	61.22	    64.31	63.62	    62.73
"""
