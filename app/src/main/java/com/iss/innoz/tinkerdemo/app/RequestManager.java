package com.iss.innoz.tinkerdemo.app;

import com.iss.innoz.tinkerdemo.event.FEvent;
import com.iss.innoz.tinkerdemo.event.StopEvent;
import com.iss.innoz.tinkerdemo.utils.BusProvider;
import com.squareup.otto.Subscribe;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA in maimai_a
 * cn.maitian.app.library.core
 *
 * @Author: xie
 * @Time: 2016/7/29 16:00
 * @Description:
 */
public class RequestManager {

    private static RequestManager requestManager = new RequestManager();

    private List<OnRequestListener> listeners = new LinkedList<>();

    private RequestManager() {
        BusProvider.register(this);
    }

    public static RequestManager get() {
        return requestManager;
    }

    @Subscribe
    public void error(FEvent e) {
        for (OnRequestListener listener : listeners) {
            listener.onError(e);
        }
    }

    @Subscribe
    public void stop(StopEvent e) {
        for (OnRequestListener listener : listeners) {
            listener.onStop(e);
        }
    }

    public void addOnRequestListener(OnRequestListener listener) {
        listeners.add(listener);
    }

    public void removeOnRequestListener(OnRequestListener listener) {
        listeners.remove(listener);
    }

    public interface OnRequestListener {
        void onStop(StopEvent e);

        void onError(FEvent e);
    }

}
