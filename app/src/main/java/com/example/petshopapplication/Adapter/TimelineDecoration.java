package com.example.petshopapplication.Adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.R;

public class TimelineDecoration extends RecyclerView.ItemDecoration {
    private final int markerRadius;
    private final int lineWidth;
    private final Paint markerPaint;
    private final Paint linePaint;

    public TimelineDecoration(Context context) {
        markerRadius = 10; // Kích thước điểm tròn
        lineWidth = 4; // Độ dày của đường kẻ

        markerPaint = new Paint();
        markerPaint.setColor(ContextCompat.getColor(context, R.color.green));
        markerPaint.setAntiAlias(true);

        linePaint = new Paint();
        linePaint.setColor(ContextCompat.getColor(context, R.color.gray));
        linePaint.setStrokeWidth(lineWidth);
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            float centerX = child.getLeft() + 20 / 2f;
            float centerY = child.getTop() + (child.getHeight() / 2f);

            // Vẽ đường dọc
            if (i != childCount - 1) {
                float startY = centerY;
                float endY = parent.getChildAt(i + 1).getTop() + (child.getHeight() / 2f);
                canvas.drawLine(centerX, startY, centerX, endY, linePaint);
            }

            // Vẽ điểm tròn
            canvas.drawCircle(centerX, centerY, markerRadius, markerPaint);
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.left = 10; // Khoảng cách bên trái cho đường dọc và điểm tròn
    }
}
