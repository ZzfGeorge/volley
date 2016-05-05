package com.zhi.volley.image;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.zhi.volley.image.gif.GifDecoder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageDecoder {
    private final DecodeQueue mDecodeQueue;
    private final Map<String, DecodeRequest> mInFlights = new HashMap<>();

    /** Handler to the main thread. */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public ImageDecoder() {
        mDecodeQueue = new DecodeQueue(3);
    }

    public interface DecodeListener {
        void onResponse(@NonNull Bitmap bitmap, int frame, int count);
    }

    public DecodeContainer get(@NonNull GifModel model, @NonNull DecodeListener listener) {
        final DecodeContainer decodeContainer = new DecodeContainer(model, listener);

        // Check to see if a decode request is inf-flight.
        DecodeRequest request = mInFlights.get(model.cacheKey);
        if (request != null) {
            // If it is, add this request to the list of listeners.
            request.containers.add(decodeContainer);
        } else {
            // Otherwise, start a new decode request.
            DecodeRequest newRequest = new DecodeRequest(model);
            newRequest.containers.add(decodeContainer);
            mDecodeQueue.add(newRequest);
            mInFlights.put(model.cacheKey, newRequest);
        }
        return decodeContainer;
    }

    public class DecodeContainer {
        @NonNull
        public final GifModel model;
        @NonNull
        public final DecodeListener listener;
        @Nullable
        public Bitmap bitmap;

        public boolean visible = true;

        public DecodeContainer(@NonNull GifModel model, @NonNull DecodeListener listener) {
            this.model = model;
            this.listener = listener;
        }

        public void cancelRequest() {
            final DecodeRequest request = mInFlights.get(model.cacheKey);
            if (request != null) {
                request.containers.remove(this);
                if (request.containers.size() == 0) {
                    mInFlights.remove(model.cacheKey);
                    request.cancel();
                }
            }
        }

        public void resumeRequest() {
            visible = true;
            DecodeRequest request = mInFlights.get(model.cacheKey);
            if (request == null || request.isCanceled()) {
                request = new DecodeRequest(model);
                request.containers.add(this);
                mDecodeQueue.add(request);
                mInFlights.put(model.cacheKey, request);

            } else if (!request.containers.contains(this)) {
                request.containers.add(this);
            }
        }
    }

    class DecodeRequest implements Comparable<DecodeRequest> {
        @NonNull
        final GifModel mode;
        @NonNull
        final List<DecodeContainer> containers;

        int sequence;
        boolean mCanceled;

        DecodeRequest(@NonNull GifModel mode) {
            this.mode = mode;
            this.containers = new LinkedList<>();
        }

        void cancel() {
            mCanceled = true;
        }

        boolean isCanceled() {
            return mCanceled;
        }

        void finish() {
            containers.clear();
            mDecodeQueue.finish(this);
        }

        @WorkerThread
        void onResponse(@NonNull final Bitmap bitmap, final int frame, final int count, int delay) {
            // post the response.
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (DecodeContainer container : containers) {
                        container.bitmap = bitmap;
                        if (container.visible) {
                            container.listener.onResponse(bitmap, frame, count);
                        }
                    }
                }
            });

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (containers.size() > 0) {
                        for (DecodeContainer container : containers) {
                            if (container.visible) {
                                // continue to decode next frame.
                                mDecodeQueue.add(DecodeRequest.this);
                                mInFlights.put(mode.cacheKey, DecodeRequest.this);
                                break;
                            }
                        }
                    }
                }
            }, delay);
        }

        @Override
        public int compareTo(@NonNull DecodeRequest another) {
            return sequence - another.sequence;
        }
    }

    static class DecodeWorker extends Thread {

        /** Used for telling us to die. */
        volatile boolean mQuit = false;
        @NonNull
        final BlockingQueue<DecodeRequest> mQueue;

        DecodeWorker(@NonNull BlockingQueue<DecodeRequest> queue) {
            mQueue = queue;
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            while (true) {
                DecodeRequest request;
                try {
                    // Take a request from the queue.
                    request = mQueue.take();
                } catch (InterruptedException e) {
                    // We may have been interrupted because it was time to quit.
                    if (mQuit) {
                        return;
                    }
                    continue;
                }

                if (request.isCanceled()) {
                    request.finish();
                    continue;
                }

                final GifDecoder decoder = request.mode.decoder;
                final long startTimeMs = SystemClock.elapsedRealtime();
                decoder.advance();

                final Bitmap bitmap = decoder.getNextFrame();
                if (bitmap == null) {
                    request.finish();
                    continue;
                }

                final int endTimeMs = (int) SystemClock.elapsedRealtime();
                final int decodeTimeMs = (int) (endTimeMs - startTimeMs);
                int delayTimeMs = decoder.getNextDelay() - decodeTimeMs;
                if (delayTimeMs < 0) {
                    delayTimeMs = 0;
                }
                request.onResponse(bitmap,
                        decoder.getCurrentFrameIndex(),
                        decoder.getFrameCount(),
                        delayTimeMs);
            }
        }
    }

    static class DecodeQueue {
        @NonNull
        final BlockingQueue<DecodeRequest> mQueue;
        @NonNull
        final DecodeWorker[] mWorkers;
        @NonNull
        final AtomicInteger mSequenceGenerator = new AtomicInteger();

        DecodeQueue(int size) {
            mQueue = new PriorityBlockingQueue<>();
            mWorkers = new DecodeWorker[size > 3 ? size : 3];
            for (int i = 0; i < mWorkers.length; i++) {
                mWorkers[i] = new DecodeWorker(mQueue);
                mWorkers[i].start();
            }
        }

        void add(@NonNull DecodeRequest request) {
            request.sequence = mSequenceGenerator.incrementAndGet();
            mQueue.add(request);
        }

        void finish(@NonNull DecodeRequest request) {
            mQueue.remove(request);
        }
    }
}
