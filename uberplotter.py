#! /usr/bin/env python
import matplotlib.pyplot as plt
import os
import datetime
import json

FILE_NAMES_LIST = "acrstr-output-file-names.ini"
OLD_FILES_DIRECTORY = "what-once-was"
NUMBER_OF_EPOCHS = 10000
LEGEND_POSITION = 3

START_INDEX = 5
STOP_INDEX = 5

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
            for i in range(START_INDEX,STOP_INDEX+1):
                a_pair = []
                for single_file in file_pairs:
                    single_file_name = single_file.format(i)
                    print single_file_name
                    processed_files.append(single_file_name)
                    a_pair.append(get_float_values_from_file(single_file_name))
                info = json.loads(pairs_and_info[1])
                plot_data(a_pair, info, i)
    input_file_list.close()

def get_float_values_from_file(file_name):
    values = []
    file_obj = open(file_name, 'r')
    for line in file_obj:
        values.append(float(line))
    file_obj.close()
    return values

def plot_data(value_pairs, info, sim_number):
    print "Plotting simulation number: {0:d}".format(sim_number)
    for index, values in enumerate(value_pairs):
        plt.plot(range(1,1+NUMBER_OF_EPOCHS), values, c=info['colors'][index], label=info['labels'][index], lw=2)
    plt.legend(loc=LEGEND_POSITION)
    plt.xlabel(info['xlabel'])
    plt.ylabel(info['ylabel'])
    if info.has_key('ylimits'):
        plt.ylim(info['ylimits'][0], info['ylimits'][1])
    plt.title("Evaluate Last {0} Rewards".format(sim_number))
#    plt.savefig("output-{0:02d}.png".format(sim_number))
#    plt.clf()

def cleanup(files):
    print "Rename old files? y/n"
    rename_answer = is_positive_answer()
    if rename_answer:
        for a_file in files:
            backup_file(a_file, OLD_FILES_DIRECTORY)
        
if __name__ == '__main__':
    read_files(FILE_NAMES_LIST)
    plt.show()
    cleanup(processed_files)
