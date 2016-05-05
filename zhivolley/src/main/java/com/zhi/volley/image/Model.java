package com.zhi.volley.image;

public interface Model {
    int MODEL_BITMAP = 0;
    int MODEL_GIF = 1;

    int byteSize();

    boolean check();
}
