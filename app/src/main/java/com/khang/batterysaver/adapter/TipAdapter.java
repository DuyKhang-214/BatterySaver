package com.khang.batterysaver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.khang.batterysaver.R;
import com.khang.batterysaver.model.Tip;

import java.util.List;

/** Adapter hiển thị danh sách mẹo trong ListView. */
public class TipAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final List<Tip> tips;

    public TipAdapter(Context context, List<Tip> tips) {
        this.inflater = LayoutInflater.from(context);
        this.tips = tips;
    }

    @Override
    public int getCount() {
        return tips.size();
    }

    @Override
    public Tip getItem(int position) {
        return tips.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_tip, parent, false);
            h = new ViewHolder();
            h.icon = convertView.findViewById(R.id.imgTipIcon);
            h.title = convertView.findViewById(R.id.tvTipTitle);
            h.desc = convertView.findViewById(R.id.tvTipDesc);
            h.done = convertView.findViewById(R.id.imgTipDone);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        Tip tip = getItem(position);
        h.icon.setImageResource(tip.iconRes);
        h.title.setText(tip.title);
        h.desc.setText(tip.description);
        h.done.setVisibility(tip.done ? View.VISIBLE : View.INVISIBLE);
        h.title.setAlpha(tip.done ? 0.6f : 1f);
        return convertView;
    }

    private static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView desc;
        ImageView done;
    }
}
