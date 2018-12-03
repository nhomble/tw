import os


def off_root(path: str, f=None):
    parts = os.path.split(__file__)
    root = os.path.join(parts[0], path)

    if not os.path.exists(root):
        os.makedirs(root)

    if f is None:
        return root
    else:
        return os.path.join(root, f)


target_dir = "target"
