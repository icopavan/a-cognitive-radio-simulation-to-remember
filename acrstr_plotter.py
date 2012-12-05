#! /usr/bin/env python
import matplotlib.pyplot as plt
import json, os, glob, sys

NUMBER_OF_EPOCHS = 10000
LEGEND_POSITION = 3
LATEST_OUTPUT_DIRECTORY = 'acrstr-latest'
GLOB_PATTERN = LATEST_OUTPUT_DIRECTORY

last_min_y = sys.float_info.max
last_max_y = - sys.float_info.max

processed_files = []

def read_files(filedir):
    files = os.listdir(filedir)
    files.sort()
    os.chdir(filedir)
    averages = []
    convergences = []
    for a_file in files:
        values = []
        opened_file = open(a_file, 'r')
        info = json.loads(opened_file.readline())
        for line in opened_file:
            values.append(float(line))
        averages.append([get_average(values), info])
        convergences.append([get_convergence(values), info])
        plot_an_epochs_values(values, info)
    plot_differing_values(averages, filedir)
    os.chdir('..')

def get_convergence(values):
    convergence = 1
    for index, value in enumerate(values):
        if index > 0:
            if value != values[index - 1]:
                convergence = index + 1
    return convergence
                
def get_average(values):
    running_sum = 0.0
    for value in values:
        running_sum += value
    return running_sum / len(values)

def plot_differing_values(values_in_pairs, filedir):
    differing_values = []
    infos = []
    for value_and_info in values_in_pairs:
        differing_values.append(value_and_info[0])
        infos.append('[' + value_and_info[1]["q response"] + '-'
                     + value_and_info[1]["rate response"] + ']')
    plt.plot(range(len(differing_values)), differing_values)
    plt.savefig(filedir + '.png')
    plt.clf()

def plot_an_epochs_values(values, info):
    plot_data(range(1,1+NUMBER_OF_EPOCHS), values, info)
    plt.legend(loc=LEGEND_POSITION)
    plt.ylim(last_min_y-0.1, last_max_y+0.1)

def plot_data(x, y, info):
    global last_min_y, last_max_y
    plt.plot(x, y, label=info['comparing'], c=info['color'])
    current_min_y = min(y)
    current_max_y = max(y)
    if current_min_y < last_min_y:
        last_min_y = current_min_y
    if current_max_y > last_max_y:
        last_max_y = current_max_y
    plt.xlabel(info['xLabel'])
    plt.ylabel(info['yLabel'])

if __name__ == '__main__':
    for dirname in glob.glob(GLOB_PATTERN):
        read_files(dirname)
