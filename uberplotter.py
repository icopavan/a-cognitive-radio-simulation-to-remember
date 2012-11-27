#! /usr/bin/env python
import matplotlib.pyplot as plt
import json, os, glob

NUMBER_OF_EPOCHS = 10000
LEGEND_POSITION = 0
LATEST_OUTPUT_DIRECTORY = 'acrstr-latest'
GLOB_PATTERN = LATEST_OUTPUT_DIRECTORY

COLORS = { "0": 'red', "5":'blue' }
ZORDERS = { "0": 0, "5": 1 }

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

def plot_data(x, y, info):
    series = info['check last values']
    min_y = min(y) - 1    
    max_y = max(y) + 1
    plt.ylim(min_y, max_y)
    plt.plot(x, y, label=series, c=COLORS[series], zorder=ZORDERS[series])

if __name__ == '__main__':
    for dirname in glob.glob(GLOB_PATTERN):
        read_files(dirname)
