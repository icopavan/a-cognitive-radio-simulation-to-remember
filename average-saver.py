#! /usr/bin/env python2
import matplotlib.pyplot as plt
import os
import datetime
import json

FILE_NAMES_LIST = "felice-convergence-output-file-names.ini"
OLD_FILES_DIRECTORY = "what-once-was"
NUMBER_OF_EPOCHS = 10000
LEGEND_POSITION = 3

START_INDEX = 0
STOP_INDEX = 50

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
    file_name_template = 'qlearning_{0}.txt'
    convergences = []
    fc = open('converge-all-iters.txt', 'r')
    for l in fc:
        convergences.append(int(l))
    averages = []
    for i in range(START_INDEX,STOP_INDEX+1):
        file_name = file_name_template.format(i)
        f = open(file_name)
        average_totals = 0
        for line in f:
            average_totals += float(line)
        averages.append([average_totals / 10000.0, convergences[i]])
    ratios = []
    for i in range(START_INDEX, STOP_INDEX+1):
        ratios.append(averages[i][0] / averages[i][1])
    plot_data(ratios)

def get_float_values_from_file(file_name):
    values = []
    file_obj = open(file_name, 'r')
    for line in file_obj:
        values.append(float(line))
    file_obj.close()
    return values

def plot_data(data):
    plt.plot(range(START_INDEX, STOP_INDEX+1), data, c='blue')
    plt.savefig("averages_over_convergences.png")
    plt.clf()

def cleanup(files):
    print "Rename old files? y/n"
    rename_answer = is_positive_answer()
    if rename_answer:
        for a_file in files:
            backup_file(a_file, OLD_FILES_DIRECTORY)
        
if __name__ == '__main__':
    read_files(FILE_NAMES_LIST)
