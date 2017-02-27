package xiao.free.refreshlayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import xiao.free.refreshlayoutlib.SwipeRefreshLayout;
import xiao.free.refreshlayoutlib.base.OnLoadMoreListener;
import xiao.free.refreshlayoutlib.base.OnRefreshListener;

/**
 * Created by robincxiao on 2017/2/24.
 */

public class ClassicActivity extends AppCompatActivity implements OnRefreshListener, OnLoadMoreListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private ArrayList<String> mArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classic);

        mListView = (ListView) findViewById(R.id.swipe_target);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setOnLoadMoreListener(this);

        mListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getData()));
    }

    private ArrayList<String> getData() {
        for (int i=0; i<50; i++){
            mArrayList.add("测试数据" + i);
        }
        return mArrayList;
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 3000);
    }

    @Override
    public void onLoadMore() {
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setLoadingMore(false);
            }
        }, 3000);
    }
}
