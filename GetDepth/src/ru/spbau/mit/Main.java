package ru.spbau.mit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        List<String> records = new ArrayList<String>();
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader("coordinates.txt"));
            String line;
            while ((line = reader.readLine()) != null)
            {
                records.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < records.size(); i += 4) {
            double x1 = Double.parseDouble(records.get(i));

            System.out.print(x1);
            double y1 = Double.parseDouble(records.get(i + 1));
            double x2 = Double.parseDouble(records.get(i + 2));
            double y2 = Double.parseDouble(records.get(i + 3));
        }
    }
}
