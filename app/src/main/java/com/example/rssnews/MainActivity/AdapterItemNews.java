package com.example.rssnews.MainActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rssnews.DataBase.News;
import com.example.rssnews.DataBase.Utilities;
import com.example.rssnews.R;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//Adapter cài đặt giao diện từng dòng tin
public class AdapterItemNews extends BaseAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    //    List danh sách tin đầu vào
    private List<News> data;

    public AdapterItemNews(Context context, List<News> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    private static class ViewHolder {
        private TextView textViewDescription;
        private TextView textViewTitle;
        private TextView textViewDate;
        private ImageView imageViewThumbnail;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_news, null);
            viewHolder = new ViewHolder();
            viewHolder.textViewDescription = (TextView) view.findViewById(R.id.description);
            viewHolder.textViewTitle = (TextView) view.findViewById(R.id.title);
            viewHolder.textViewDate = (TextView) view.findViewById(R.id.date);
            viewHolder.imageViewThumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        News news = data.get(i);

//        Cài giá trị của tin vào giao diện

//        Set tiêu đề
        viewHolder.textViewTitle.setText(news.getTitle());
//        Set mô tả
        viewHolder.textViewDescription.setText(news.getDescription()!=null?news.getDescription():"");
//        Set ngày tạo
        viewHolder.textViewDate.setText(news.getPubdate()!=null?convertDateToString(news.getPubdate()):"");

//        Set hình ảnh
        if (news.getImage() != null) {
//            Nếu có ảnh thì hiện
            Picasso.get().load(news.getImage()).into(viewHolder.imageViewThumbnail);
        }
        else {
//            Nếu tin không có ảnh thì load ảnh của báo
            Picasso.get().load(Utilities.logoWeb(news.getWebSite())).into(viewHolder.imageViewThumbnail);
        }

//        Cài đặt hiệu ứng fade (hiện dần) cho item list
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_list);
        view.startAnimation(animation);

        return view;
    }

    //    Format ngày để hiển thị
    public String convertDateToString(Date date) {
        String pattern = "HH:mm dd/MM/yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }
}

