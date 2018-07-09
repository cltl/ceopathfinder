import os
import os.path
import sys
from collections import namedtuple

class NotValidEnding(Exception):
    pass


def check_score(input_ending):

    file_ending = ['.eval', '.eval.bl1S', '.eval.bl3S', '.eval.bl5S', '.eval.blANY', '.eval.ceo1S', '.eval.ceo3S', '.eval.ceo5S', '.eval.ceoANY', '.eval.nc1S', '.eval.nc3S', '.eval.nc5S', '.eval.ncANY', '.eval.clink1S', '.eval.clink3S', '.eval.clink5S', '.eval.clinkANY', '.eval.fn1S', '.eval.fn3S', '.eval.fn5S', '.eval.fnANY']

    if input_ending not in file_ending:
        raise NotValidEnding
    else:
        return input_ending


def system_end():

    while True:
        ends = input("System output ending: " + "\n")
        try:
            check_score(ends)
            return ends
        except NotValidEnding:
            print("Illegal value - system output must ends with one of the following values:  '.eval', '.eval.bl1S', '.eval.bl3S', '.eval.bl5S', 'eval.blANY', .eval.ceo1S','.eval.ceo3S', '.eval.ceo5S', '.eval.ceoANY', .eval.nc1S','.eval.nc3S', '.eval.nc5S', '.eval.ncANY', '.eval.clink1S', '.eval.clink3S', '.eval.clink5S', '.eval.clinkANY', '.eval.fn1S', '.eval.fn3S', '.eval.fn5S', '.eval.fnANY'")
            return system_end()


def check_path(filepath):
    if os.path.isdir(filepath):
        if filepath[-1] != '/':
            filepath += '/'

    return filepath

Result_strict = namedtuple('Result', ['true_positive', 'false_positive', 'false_negative'])
Result_soft = namedtuple('Result_value', ['true_positive', 'false_positive', 'false_negative'])

def true_positive(test, gold):
    counter_tp = 0
    for i in test:
        if i in gold:
            counter_tp += 1
    return counter_tp

def false_positives(test, gold):
    counter_fp = 0
    for i in test:
        if i not in gold:
            counter_fp +=1
    return counter_fp


def false_negatives(gold, test):
    counter_fn = 0
    for i in gold:
        if i not in test:
            counter_fn += 1
    return counter_fn


def process_file_event_only(file_name):

    event_only = []

    if os.path.isfile(file_name):

        with open(file_name) as fileObject:
            for line in fileObject:
                line_stripped = line.strip()

                """
                eval per event mention only
                """
                event_only.append(line_stripped)

    return event_only


def process_file(file_name):

    event_pairs = []
    event_pairs_values = []
    event_only = []

    if os.path.isfile(file_name):

        with open(file_name) as fileObject:
            for line in fileObject:
                line_stripped = line.strip()
                line_splitted = line_stripped.split("\t")

                """
                event mention only
                """
                if line_splitted[1] not in event_only:
                    event_only.append(line_splitted[1])

                if line_splitted[2] not in event_only:
                    event_only.append(line_splitted[2])

                """
                strict eval - pairs as they are in system and gold
                """
                pairs_value_strict = line_splitted[1] + "#" + line_splitted[2] + "#" + line_splitted[3]
                event_pairs.append(pairs_value_strict)

                """
                eval - sort by token id event
                """
#                print(tuple(line_splitted[1].split(":")[0].split("_")), line_splitted[2].split(":")[0].split("_"))

                if "_" in line_splitted[1].split(":")[0] or "_" in line_splitted[2].split(":")[0]:
                    first_elem = int(line_splitted[1].split(":")[0].split("_")[0])
                    second_elem = int(line_splitted[2].split(":")[0].split("_")[0])
                    if first_elem < second_elem:
                        pairs_only_sorted = (line_splitted[1].split(":")[0], line_splitted[2].split(":")[0],)
                        event_pairs_values.append(pairs_only_sorted)
                    else:
                        pairs_only_sorted = (line_splitted[2].split(":")[0], line_splitted[1].split(":")[0],)
                        event_pairs_values.append(pairs_only_sorted)
                else:
                    pairs_only_sorted = tuple(sorted((int(line_splitted[1].split(":")[0]), int(line_splitted[2].split(":")[0]),)))
                    event_pairs_values.append(pairs_only_sorted)

    return event_only, event_pairs, event_pairs_values


def compute_eval(goldf, systemf, file_ending):

    event_only_gold, event_pairs_gold, pairs_relation_gold = process_file(goldf)

    if file_ending == ".eval":
        event_sys = process_file_event_only(systemf)

        tp_pairs = true_positive(event_sys, event_only_gold)
        fp_pairs = false_positives(event_sys, event_only_gold)
        fn_pairs = false_negatives(event_only_gold, event_sys)

        scores = Result_strict(true_positive=tp_pairs,
                               false_positive=fp_pairs,
                               false_negative=fn_pairs)

        return scores

    else:

        event_only_sys, event_pairs_sys, pairs_relations_sys = process_file(systemf)

        tp_pairs = true_positive(event_pairs_sys,event_pairs_gold)
        fp_pairs = false_positives(event_pairs_sys,event_pairs_gold)
        fn_pairs = false_negatives(event_pairs_gold,event_pairs_sys)

        tp_pairs_value = true_positive(pairs_relations_sys,pairs_relation_gold)
        fp_pairs_value = false_positives(pairs_relations_sys,pairs_relation_gold)
        fn_pairs_value = false_negatives(pairs_relation_gold,pairs_relations_sys)


        scores = Result_strict(true_positive=tp_pairs,
                       false_positive=fp_pairs,
                       false_negative=fn_pairs)

        scores_value = Result_soft(true_positive=tp_pairs_value,
                       false_positive=fp_pairs_value,
                       false_negative=fn_pairs_value)


        return scores, scores_value


def eval(golddir, systemdir):

    gold_check = check_path(golddir)
    system_check = check_path(systemdir)

    file_names_ecbplus = [(gold_check, f) for f in os.listdir(gold_check)]

    file_eval_ending = system_end()

    all_results = []
    for f in file_names_ecbplus:
        if f[1].endswith(".eval"):

            systemf = system_check + f[1].split(".eval")[0] + "_connected-events" +  file_eval_ending

            result = compute_eval(gold_check + f[1], systemf, file_eval_ending)
            all_results.append(result)

    if file_eval_ending == ".eval":
        tp_events = (sum([i.true_positive for i in all_results]))
        fp_events = (sum([i.false_positive for i in all_results]))
        fn_events = (sum([i.false_negative for i in all_results]))

        precision_events = float(tp_events) / (float(tp_events) + float(fp_events))
        recall_events = float(tp_events) / (float(tp_events) + float(fn_events))
        if precision_events == 0.0 and recall_events == 0.0:
            f1_events = 0.0
        else:
            f1_events = (2*((precision_events * recall_events)/(precision_events + recall_events)))

            print("Precision events: " + str(precision_events) + "\n"
                  + "Recall pairs-value strict: " + str(recall_events) + "\n"
                  + "F1 pairs-value strict: " +  str(f1_events) + "\n")

    else:
        tp_pairs = (sum([i[0].true_positive for i in all_results]))
        fp_pairs = (sum([i[0].false_positive for i in all_results]))
        fn_pairs = (sum([i[0].false_negative for i in all_results]))


        tp_pairs_value = (sum([i[1].true_positive for i in all_results]))
        fp_pairs_value = (sum([i[1].false_positive for i in all_results]))
        fn_pairs_value = (sum([i[1].false_negative for i in all_results]))


        precision_pairs = float(tp_pairs) / (float(tp_pairs) + float(fp_pairs))
        recall_pairs = float(tp_pairs) / (float(tp_pairs) + float(fn_pairs))
        if precision_pairs == 0.0 and recall_pairs == 0.0:
            f1_pairs = 0.0
        else:
            f1_pairs = (2*((precision_pairs * recall_pairs)/(precision_pairs + recall_pairs)))

        print("Precision pairs-value strict: " + str(precision_pairs) + "\n"
              + "Recall pairs-value strict: " + str(recall_pairs) + "\n"
              + "F1 pairs-value strict: " +  str(f1_pairs) + "\n")


        precision_pairs_value = float(tp_pairs_value) / (float(tp_pairs_value) + float(fp_pairs_value))
        recall_pairs_value = float(tp_pairs_value) / (float(tp_pairs_value) + float(fn_pairs_value))

        if precision_pairs_value == 0.0 and recall_pairs_value == 0.0:
            f1_pairs_value = 0.0
        else:
            f1_pairs_value = (2 * ((precision_pairs_value * recall_pairs_value) / (precision_pairs_value + recall_pairs_value)))

            print("Precision pairs-only: " + str(precision_pairs_value) + "\n"
              + "Recall pairs-only: " + str(recall_pairs_value) + "\n"
              + "F1 pairs-only: " + str(f1_pairs_value) + "\n")

def eval_ending(golddir, systemdir, file_eval_ending):
    print(golddir)
    print(systemdir)
    print(file_eval_ending)
    gold_check = check_path(golddir)
    system_check = check_path(systemdir)

    file_names_ecbplus = [(gold_check, f) for f in os.listdir(gold_check)]

    all_results = []
    for f in file_names_ecbplus:
        if f[1].endswith(".eval"):

            systemf = system_check + f[1].split(".eval")[0] +  file_eval_ending

            result = compute_eval(gold_check + f[1], systemf, file_eval_ending)
            all_results.append(result)

    if file_eval_ending == ".eval":
        tp_events = (sum([i.true_positive for i in all_results]))
        fp_events = (sum([i.false_positive for i in all_results]))
        fn_events = (sum([i.false_negative for i in all_results]))

        precision_events = float(tp_events) / (float(tp_events) + float(fp_events))
        recall_events = float(tp_events) / (float(tp_events) + float(fn_events))
        if precision_events == 0.0 and recall_events == 0.0:
            f1_events = 0.0
        else:
            f1_events = (2*((precision_events * recall_events)/(precision_events + recall_events)))

            print("Precision events: " + str(precision_events) + "\n"
                  + "Recall pairs-value strict: " + str(recall_events) + "\n"
                  + "F1 pairs-value strict: " +  str(f1_events) + "\n")

    else:
        tp_pairs = (sum([i[0].true_positive for i in all_results]))
        fp_pairs = (sum([i[0].false_positive for i in all_results]))
        fn_pairs = (sum([i[0].false_negative for i in all_results]))


        tp_pairs_value = (sum([i[1].true_positive for i in all_results]))
        fp_pairs_value = (sum([i[1].false_positive for i in all_results]))
        fn_pairs_value = (sum([i[1].false_negative for i in all_results]))

        if tp_pairs == 0 and fp_pairs == 0:
            precision_pairs = 0.0
            recall_pairs =0.0
        else:
            precision_pairs = float(tp_pairs) / (float(tp_pairs) + float(fp_pairs))
            recall_pairs = float(tp_pairs) / (float(tp_pairs) + float(fn_pairs))
        if precision_pairs == 0.0 and recall_pairs == 0.0:
            f1_pairs = 0.0
        else:
            f1_pairs = (2*((precision_pairs * recall_pairs)/(precision_pairs + recall_pairs)))

        print("Precision pairs-value strict: " + str(precision_pairs) + "\n"
              + "Recall pairs-value strict: " + str(recall_pairs) + "\n"
              + "F1 pairs-value strict: " +  str(f1_pairs) + "\n")

        if tp_pairs == 0 and fp_pairs == 0:
            precision_pairs_value = 0.0
            recall_pairs_value = 0.0
        else:
            precision_pairs_value = float(tp_pairs_value) / (float(tp_pairs_value) + float(fp_pairs_value))
            recall_pairs_value = float(tp_pairs_value) / (float(tp_pairs_value) + float(fn_pairs_value))

        if precision_pairs_value == 0.0 and recall_pairs_value == 0.0:
            f1_pairs_value = 0.0
        else:
            f1_pairs_value = (2 * ((precision_pairs_value * recall_pairs_value) / (precision_pairs_value + recall_pairs_value)))

            print("Precision pairs-only: " + str(precision_pairs_value) + "\n"
              + "Recall pairs-only: " + str(recall_pairs_value) + "\n"
              + "F1 pairs-only: " + str(f1_pairs_value) + "\n")

def main(argv=None):
    if argv is None:
        argv = sys.argv

    if len(argv) < 4:
        print("Usage python3 eval_mentions.py gold system [ending]")
    else:
        eval_ending(argv[1], argv[2], argv[3])


if __name__ == '__main__':
    main()
