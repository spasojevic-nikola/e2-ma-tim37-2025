package com.example.habitquest;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<TaskCategory> {

    public CategoryAdapter(Context context, List<TaskCategory> categories) {
        super(context, 0, categories);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TaskCategory category = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_category, parent, false);
        }

        TextView tvName = convertView.findViewById(R.id.tvCategoryName);
        View colorView = convertView.findViewById(R.id.viewCategoryColor);

        tvName.setText(category.getName());

        // Setuje boju kružića
        GradientDrawable drawable = (GradientDrawable) colorView.getBackground();
        drawable.setColor(android.graphics.Color.parseColor(category.getColor()));

        return convertView;
    }
}
