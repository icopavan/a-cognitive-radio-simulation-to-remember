#! /usr/bin/env python
import matplotlib.pyplot as plt
import json, os, glob, sys

LEGEND_POSITION = 4
GLOB_PATTERN = 'acrstr-latest'
NUMBER_OF_EPOCHS = 10000

last_min_y = sys.float_info.max
last_max_y = - sys.float_info.max

def read_files(filedir):
    files = os.listdir(filedir)
    txt_files = []
    for a_file in files:
        if a_file[-3:] == 'txt':
            txt_files.append(a_file)
    txt_files.sort()
    os.chdir(filedir)
    averages = []
    for a_file in txt_files:
        values = []
        opened_file = open(a_file, 'r')
        info = json.loads(opened_file.readline())
        for line in opened_file:
            values.append(float(line))
        averages.append([get_average(values), info])
        plot_a_simulation(values, info, filedir)
    plt.savefig(filedir + '.png')
    plt.clf()

def get_average(values):
    running_sum = 0.0
    for value in values:
        running_sum += value
    return running_sum / len(values)

def plot_a_simulation(values, info, filedir):
    number_of_values = int(info['numberOfValues'])
    labels = range(1,1+number_of_values)
    for index, label in enumerate(labels):
        labels[index] = label * NUMBER_OF_EPOCHS / number_of_values
    plot_data(labels, values, info),
    plt.legend(loc=LEGEND_POSITION)
    difference = last_max_y - last_min_y
    plt.ylim(last_min_y - difference / 10.0, last_max_y + difference / 10.0)

def plot_data(x, y, info):
    global last_min_y, last_max_y
    plt.plot(x, y, label=info['legend'], c=info['color'])
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
