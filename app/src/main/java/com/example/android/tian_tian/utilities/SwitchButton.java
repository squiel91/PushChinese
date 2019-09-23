package com.example.android.tian_tian.utilities;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.android.tian_tian.R;

public class SwitchButton {
    TextView button;
    Boolean active;
    Context context;

    public SwitchButton(Context context, TextView button, boolean initially_active) {
        this.button = button;
        this.context = context;
        if (initially_active) setSelected();
        else setUnselected();
    }

    public boolean toggle() {
        if (active) setUnselected();
        else setSelected();
        return active;
    }

    public void setUnselected() {
        active = false;
        button.setBackground(context.getDrawable(R.drawable.filter_item_inactive));
        button.setTextColor(context.getColor(R.color.black));
    }

    public void setSelected() {
        active = true;
        button.setBackground(context.getResources().getDrawable(R.drawable.filter_item_active));
        button.setTextColor(context.getColor(R.color.white));
    }

    public TextView getButton() {
        return button;
    }

    public Boolean getActive() {
        return active;
    }
}
