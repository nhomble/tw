import matplotlib.pyplot as plt;
import numpy as np
import matplotlib.pyplot as plt

def main():
    objects = ('Control', 'DOTA', 'Riot Games', 'Fortnite', 'PUBG ESL', 'StarCraft', 'Bethesda', 'Counter-Strike')
    prob = [
        0.03943086,
        0.06368172,
        0.05266944,
        0.05513004,
        0.08387219,
        0.06061224,
        0.08611445,
        0.06815733
    ]
    colors = ['b'] * (len(prob) - 1)
    y_pos = np.arange(len(objects))

    plt.bar(y_pos, prob, align='center', alpha=0.5, color=['r'] + colors)
    plt.xticks(y_pos, objects)
    plt.ylabel('Probability')
    plt.title('Attribute Matches and how they affect edge probability')

    plt.show()

if __name__ == "__main__":
    plt.rcdefaults()
    main()