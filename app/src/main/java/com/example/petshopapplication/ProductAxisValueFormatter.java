package com.example.petshopapplication;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import java.util.List;

public class ProductAxisValueFormatter implements IAxisValueFormatter {
    private List<String> productLabels;

    public ProductAxisValueFormatter(List<String> productLabels) {
        this.productLabels = productLabels;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // Get the label text
        String label = productLabels.get((int) value);

        // Check if the label is too long and split it
        if (label.length() > 10) {  // Set a threshold for splitting
            String[] words = label.split(" ");
            StringBuilder formattedLabel = new StringBuilder();

            int lineLength = 0;
            for (String word : words) {
                if (lineLength + word.length() > 10) {
                    formattedLabel.append("\n"); // Add a new line
                    lineLength = 0;
                }
                formattedLabel.append(word).append(" ");
                lineLength += word.length() + 1; // Update line length with space
            }
            return formattedLabel.toString().trim();  // Return the formatted label
        }

        return label;  // Return the original label if not too long
    }
}


