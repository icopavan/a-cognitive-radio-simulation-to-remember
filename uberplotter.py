#! /usr/bin/env python
import matplotlib.pyplot as plt
import json, os

NUMBER_OF_EPOCHS = 10000
LEGEND_POSITION = 3
LATEST_OUTPUT_DIRECTORY = 'acrstr-latest'

processed_files = []

def read_files():
    files = os.listdir(LATEST_OUTPUT_DIRECTORY)
    os.chdir(LATEST_OUTPUT_DIRECTORY)
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
        plot_average_values(values, info)
#    plot_differing_values(averages)
#    plot_differing_values(convergences)

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

def plot_differing_values(values_in_pairs):
    differing_values = []
    infos = []
    for value_and_info in values_in_pairs:
        differing_values.append(value_and_info[0])
        infos.append('[' + value_and_info[1]["q response"] + '-'
                     + value_and_info[1]["rate response"] + ']')
    plt.bar(range(len(differing_values)), differing_values)
    plt.xticks(range(len(differing_values)), infos)

def plot_average_values(values, info):
    plot_data(range(1,1+NUMBER_OF_EPOCHS), values, info)
    plt.legend(loc=LEGEND_POSITION)

def plot_data(x, y, info):
    plot_label = '{0}, {1}'.format(info["q response"], info["rate response"])
    plt.plot(x, y, label=plot_label)


if __name__ == '__main__':
    read_files()
    plt.show()
