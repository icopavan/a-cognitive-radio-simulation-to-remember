#! /usr/bin/env python2
import matplotlib.pyplot as plt
import os
import datetime
import json

FILE_NAMES_LIST = "felice-output-file-names.ini"
OLD_FILES_DIRECTORY = "what-once-was"
NUMBER_OF_EPOCHS = 10000
LEGEND_POSITION = 3

START_INDEX = 1

processed_files = []

def is_positive_answer():
    console = raw_input()
    answer = console.lower()
    while not (answer.startswith('y') or answer.startswith('n')):
        console = raw_input()
        answer = console.lower()
    if answer.startswith('y'):
        return True
    else:
        return False

def get_iso_timestamp():
    now = datetime.datetime.now()
    current_timestamp = now.strftime('%Y-%m-%d-%H-%M-%S')
    return current_timestamp

def create_directory(dir_name):
    if not os.path.exists(dir_name):
        os.makedirs(dir_name)

def backup_file(file_name, backup_dir):
    create_directory(backup_dir)
    extension_index = file_name.rfind('.')
    extension = file_name[extension_index:]
    file_name_wo_ext = file_name[:-(extension_index + 1)]
    now = get_iso_timestamp()
    os.rename(file_name, backup_dir + '/' + file_name_wo_ext + '-' + now + extension)

def read_files(file_list):
    input_file_list = open(file_list, 'r')    
    file_pairs = []
    for line in input_file_list:
        if line[0] is not '#':
            pairs_and_info = line.split('|')
            file_pairs = pairs_and_info[0].split(';')
            a_pair = []
            for single_file in file_pairs:
                processed_files.append(single_file)
                a_pair.append(get_float_values_from_file(single_file))
            info = json.loads(pairs_and_info[1])
            plot_data(a_pair, info)
    input_file_list.close()

def get_float_values_from_file(file_name):
    values = []
    file_obj = open(file_name, 'r')
    for line in file_obj:
        values.append(float(line))
    file_obj.close()
    return values

def plot_data(value_pairs, info):
    global START_INDEX
    for index, values in enumerate(value_pairs):
        plt.plot(range(1,1+NUMBER_OF_EPOCHS), values, c=info['colors'][index],
                 label=info['labels'][index])
    plt.legend(loc=LEGEND_POSITION)
    plt.xlabel(info['xlabel'])
    plt.ylabel(info['ylabel'])
    if info.has_key('ylimits'):
        plt.ylim(info['ylimits'][0], info['ylimits'][1])
    plt.savefig("{0:02d}-output.png".format(START_INDEX))
    plt.clf()
    START_INDEX += 1

def cleanup(files):
    print "Rename old files? y/n"
    rename_answer = is_positive_answer()
    if rename_answer:
        for a_file in files:
            backup_file(a_file, OLD_FILES_DIRECTORY)
        
if __name__ == '__main__':
    read_files(FILE_NAMES_LIST)
    cleanup(processed_files)
