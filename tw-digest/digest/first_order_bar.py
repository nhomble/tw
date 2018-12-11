import matplotlib.pyplot as plt;
import numpy as np
import matplotlib.pyplot as plt


def main():
    objects = (
    'Control', 'DOTA', 'Fortnite', 'League of Legends', 'Counter Strike', 'FIFA', 'Madden Games', 'Minecraft',
    'Hearthstone', 'Halo')
    prob = [
        0.09100909,
        0.04926108,
        0.1145173,
        0.1468591,
        0.32,
        0.09100909,
        0.09100909,
        0.09100909,
        0.081,
        0.03075839
    ]
    colors = ['b'] + ['r'] * 4 + ['g'] * 5
    y_pos = np.arange(len(objects))

    plt.bar(y_pos, prob, align='center', alpha=0.5, color=colors)
    plt.xticks(y_pos, objects)
    plt.ylabel('Probability')
    plt.title('Probability of similar streamers followed by games')

    plt.show()


if __name__ == "__main__":
    plt.rcdefaults()
    plt.rcParams.update({'font.size': 6})
    main()
