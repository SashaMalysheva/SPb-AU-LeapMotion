package ru.spbau.mit;

public class MatAppr {
    private final int INF = 700;
    private int height;
    private int width;
    private float data[][];
    private int count;
    private MinPair[] horizontal, vertical;
    private Min minVert, minHor;

    private class MinPair {
        public int dist;
        public int coord1;
        public int coord2;

        public MinPair(int dist, int coord1, int coord2) {
            this.dist = dist;
            this.coord1 = coord1;
            this.coord2 = coord2;
        }

        public MinPair() {
            dist = INF;
        }
    }
    private class Min {
        public int value;
        public int coord;

        public Min(int value, int coord) {
            this.value = value;
            this.coord = coord;
        }
    }

    public MatAppr(int height, int width, float[][] data) {
        this.height = height;
        this.width = width;
        this.data = data;
        count = height * width;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                data[i][j] = Math.max(data[i][j], 0);
                if (data[i][j] > 0) {
                    count--;
                }
            }
        }
        minVert = new Min(INF, 0);
        minHor = new Min(INF, 0);
        horizontal = new MinPair[height];
        for (int i = 0; i < height; i++)
            horizontal[i] = new MinPair();
        vertical = new MinPair[width];
        for (int i = 0; i < width; i++)
            vertical[i] = new MinPair();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public float[][] getData() {
        return data;
    }

    public void debugOutput() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(data[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void approximate() {
        refreshHorizontal(0, height - 1);
        refreshVertical(0, width - 1);

        while (count > 0) {
            if (minHor.value == INF && minVert.value == INF) {
                makeCloseValues();
            }

            if (count == 0)
                break;

            if (minVert.value < minHor.value) {
                int coordinate = minVert.coord;
                MinPair tmp = vertical[coordinate];
                float delta = (data[tmp.coord2][coordinate] - data[tmp.coord1][coordinate]) / tmp.dist;
                for (int i = 1; i < tmp.dist; i++) {
                    data[tmp.coord1 + i][coordinate] = data[tmp.coord1][coordinate] + i * delta;
                }
                count -= (tmp.dist - 1);
                refreshHorizontal(tmp.coord1 + 1, tmp.coord2 - 1);
                refreshVertical(coordinate, coordinate);
            } else {
                int coordinate = minHor.coord;
                MinPair tmp = horizontal[coordinate];
                float delta = (data[coordinate][tmp.coord2] - data[coordinate][tmp.coord1]) / tmp.dist;
                for (int i = 1; i < tmp.dist; i++) {
                    data[coordinate][tmp.coord1 + i] = data[coordinate][tmp.coord1] + i * delta;
                }
                count -= (tmp.dist - 1);
                refreshVertical(tmp.coord1 + 1, tmp.coord2 - 1);
                refreshHorizontal(coordinate, coordinate);
            }
            debugOutput();
        }
    }

    private void makeCloseValues() {
        if (count < Math.floor(0.5 * width * height)) {
            fillBounds();
            return;
        }

        int hor[][] = new int[width][2];
        int vert[][] = new int[height][2];
        for (int i = 0; i < width; i++)
            hor[i][0] = hor[i][1] = INF;
        for (int i = 0; i < height; i++)
            vert[i][0] = vert[i][1] = INF;

        for (int i = 0; i < height; i++) {
            int j = 0;
            while (j < width) {
                if (data[i][j] != 0) {
                    vert[i][0] = j++;
                    while (j < width && data[i][j] != 0)
                        j++;
                    vert[i][1] = j - 1;
                    break;
                } else {
                    j++;
                }
            }
        }

        for (int j = 0; j < width; j++) {
            int i = 0;
            while (i < height) {
                if (data[i][j] != 0) {
                    hor[j][0] = i++;
                    while (i < height && data[i][j] != 0)
                        i++;
                    hor[j][1] = i - 1;
                    break;
                } else {
                    i++;
                }
            }
        }

        MinPair horizontal = new MinPair();
        MinPair vertical = new MinPair();

        vertical.dist = INF;
        int j = 0;
        int coord1 = 0, coord2 = 0;
        while (j < height - 1) {
            while (vert[j][0] == INF)
                j++;
            coord1 = j++;
            while (vert[j][0] == INF)
                j++;
            coord2 = j;
            if (data[j][i] != 0 && coord2 - coord1 > 1 && coord2 - coord1 < vertical[i].dist) {
                vertical[i].dist = coord2 - coord1;
                vertical[i].coord1 = coord1;
                vertical[i].coord2 = coord2;
            }
        }


    }

    private void fillBounds() {
        int i, j;
        while (count > Math.ceil(0.07 * width * height)) {
            i = 0;
            while (i < height) {
                j = 0;
                if (data[i][j] != 0) {
                    i++;
                    continue;
                }
                while (data[i][j] == 0 && j < width - 1)
                    j++;
                if (j != width - 1 && data[i][j + 1] != 0) {
                    float delta = data[i][j + 1] - data[i][j];
                    if (data[i][j + 1] < data[i][j]) {
                        while (j > 0) {
                            j--;
                            data[i][j] = data[i][j + 1] - delta;
                            count--;
                        }
                    } else {
                        while (data[i][j] > delta && j > 0) {
                            j--;
                            data[i][j] = data[i][j + 1] - delta;
                            count--;
                        }
                    }
                }
                i++;
            }
            debugOutput();
            i = 0;
            while (i < height) {
                j = width - 1;
                if (data[i][j] != 0) {
                    i++;
                    continue;
                }
                while (data[i][j] == 0 && j > 0)
                    j--;
                if (j != 0 && data[i][j - 1] != 0) {
                    float delta = data[i][j - 1] - data[i][j];
                    if (data[i][j - 1] < data[i][j]) {
                        while (j < width - 1) {
                            j++;
                            data[i][j] = data[i][j - 1] - delta;
                            count--;
                        }
                    } else {
                        while (data[i][j] > delta && j < width - 1) {
                            j++;
                            data[i][j] = data[i][j - 1] - delta;
                            count--;
                        }
                    }
                }
                i++;
            }
            debugOutput();
            j = 0;
            while (j < width) {
                i = 0;
                if (data[i][j] != 0) {
                    j++;
                    continue;
                }
                while (data[i][j] == 0 && i < height - 1)
                    i++;
                if (i != height - 1 && data[i + 1][j] != 0) {
                    float delta = data[i + 1][j] - data[i][j];
                    if (data[i + 1][j] < data[i][j]) {
                        while (i > 0) {
                            i--;
                            data[i][j] = data[i + 1][j] - delta;
                            count--;
                        }
                    } else {
                        while (data[i][j] > delta && i > 0) {
                            i--;
                            data[i][j] = data[i + 1][j] - delta;
                            count--;
                        }
                    }
                }
                j++;
            }
            debugOutput();
            j = 0;
            while (j < width) {
                i = height - 1;
                if (data[i][j] != 0) {
                    j++;
                    continue;
                }
                while (data[i][j] == 0 && i > 0)
                    i--;
                if (i != 0 && data[i - 1][j] != 0) {
                    float delta = data[i - 1][j] - data[i][j];
                    if (data[i - 1][j] < data[i][j]) {
                        while (i < height - 1) {
                            i++;
                            data[i][j] = data[i - 1][j] - delta;
                            count--;
                        }
                    } else {
                        while (data[i][j] > delta && i < height - 1) {
                            i++;
                            data[i][j] = data[i - 1][j] - delta;
                            count--;
                        }
                    }
                }
                j++;
            }
        }
    }

    private void refreshHorizontal(int up, int down) {
        assert up <= down;
        assert up >= 0;
        assert down < height;

        for (int i = up; i <= down; i++) {
            horizontal[i].dist = INF;
            int j = 0;
            int coord1 = 0, coord2 = 0;
            while (j < width - 1) {
                while (data[i][j] == 0 && j < width - 2)
                    j++;
                coord1 = j++;
                while (data[i][j] == 0 && j < width - 1)
                    j++;
                coord2 = j;
                if (data[i][j] != 0 && coord2 - coord1 > 1 && coord2 - coord1 < horizontal[i].dist) {
                    horizontal[i].dist = coord2 - coord1;
                    horizontal[i].coord1 = coord1;
                    horizontal[i].coord2 = coord2;
                }
            }
        }

        minHor.value = INF;
        for (int i = 0; i < height; i++)
            if (horizontal[i].dist < minHor.value) {
                minHor.value = horizontal[i].dist;
                minHor.coord = i;
            }
    }

    private void refreshVertical(int left, int right) {
        assert left <= right;
        assert left >= 0;
        assert right < width;

        for (int i = left; i <= right; i++) {
            vertical[i].dist = INF;
            int j = 0;
            int coord1 = 0, coord2 = 0;
            while (j < height - 1) {
                while (data[j][i] == 0 && j < height - 2)
                    j++;
                coord1 = j++;
                while (data[j][i] == 0 && j < height - 1)
                    j++;
                coord2 = j;
                if (data[j][i] != 0 && coord2 - coord1 > 1 && coord2 - coord1 < vertical[i].dist) {
                    vertical[i].dist = coord2 - coord1;
                    vertical[i].coord1 = coord1;
                    vertical[i].coord2 = coord2;
                }
            }
        }

        minVert.value = INF;
        for (int i = 0; i < width; i++)
            if (vertical[i].dist < minVert.value) {
                minVert.value = vertical[i].dist;
                minVert.coord = i;
            }
    }
}
