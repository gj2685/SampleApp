package com.example.micro.sample.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.micro.sample.R;
import com.example.micro.sample.model.Article;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Article> mArticleList = new ArrayList<>();
    private String mQueryText;

    public ArticleAdapter(Context context, ArrayList<Article> list) {
        mContext = context;
        mArticleList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.article_row, null, false);
        //View v = View.inflate(mContext, R.layout.article_row, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //holder.imgView.setImageUrl(mArticleList.get(position).getImgUrl(), mImgLoader);
        String thumbSrc = mArticleList.get(position).getThumbSrc();
        //int width = mArticleList.get(position).getThumbWidth();
        //int height = mArticleList.get(position).getThumbHeight();
        if (thumbSrc != null) {
            Picasso.with(mContext)
                    .load(thumbSrc)
                    .into(holder.imgView);
        /*Glide.with(mContext)
                .load(mArticleList.get(position).getImgUrl())
                .sizeMultiplier(dm.density/6)
                .centerCrop()
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(holder.imgView);*/
        }
        String title = mArticleList.get(position).getTitle();

        SpannableStringBuilder ssb = new SpannableStringBuilder(title);
        if (mQueryText != null && title.toLowerCase().startsWith(mQueryText.toLowerCase())) {
            StyleSpan style = new StyleSpan(Typeface.BOLD);
            ssb.setSpan(style, 0, mQueryText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        holder.tv_title.setText(ssb);
    }

    @Override
    public int getItemCount() {
        if (mArticleList != null)
            return mArticleList.size();
        return 0;
    }

    public void updateArticles(ArrayList<Article> list) {
        mArticleList = list;
        notifyDataSetChanged();
    }

    public void setQueryText(String queryText) {
        mQueryText = queryText;
    }

    public String getQueryText() {
        return mQueryText;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        ImageView imgView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            this.imgView = (ImageView) itemView.findViewById(R.id.article_img);
        }
    }
}