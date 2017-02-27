package xiao.free.refreshlayout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import xiao.free.refreshlayoutlib.SwipeRefreshLayout;
import xiao.free.refreshlayoutlib.base.OnLoadMoreListener;
import xiao.free.refreshlayoutlib.base.OnRefreshListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private ArrayList<String> mArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_classic).setOnClickListener(this);
        findViewById(R.id.btn_google_hook).setOnClickListener(this);
        findViewById(R.id.btn_google).setOnClickListener(this);
        findViewById(R.id.btn_jindong).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_classic:
                startActivity(new Intent(this, ClassicActivity.class));
                break;
            case R.id.btn_google_hook:
                startActivity(new Intent(this, GoogleHookActivity.class));
                break;
            case R.id.btn_google:
                startActivity(new Intent(this, GoogleActivity.class));
                break;
            case R.id.btn_jindong:
                startActivity(new Intent(this, JindongActivity.class));
                break;
        }
    }
}
