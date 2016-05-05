package com.zhi.volley.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.zhi.volley.ZhiVolley;
import com.zhi.volley.image.NetworkImageView;

public class MainActivity extends AppCompatActivity {

    private NetworkImageView mImageView;
    private NetworkImageView mGifView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ZhiVolley.getInstance().init(this);

        mImageView = (NetworkImageView) findViewById(R.id.image);
        mImageView.setImageUrl("http://www.pp3.cn/uploads/201509/2015091507.jpg");

        mGifView = (NetworkImageView) findViewById(R.id.gif);
        mGifView.setGifUrl("http://b350.photo.store.qq.com/psb?/V12E5cr70k3vuk/RZrT9s3y0IBsp7XuSkJ91SMGgdgX4CKjWQ1VBzyGaps!/b/dCxdotBVJgAA&bo=IANYAgAAAAAC*6I!&rf=viewer_4");
    }
}
