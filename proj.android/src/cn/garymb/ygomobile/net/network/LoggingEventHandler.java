/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A test EventHandler: Logs everything received
 */

package cn.garymb.ygomobile.net.network;


/**
 * {@hide}
 */
public class LoggingEventHandler implements EventHandler {

    public void requestSent() {
        //HttpLog.v("LoggingEventHandler:requestSent()");
    }

    public void status(int major_version,
                       int minor_version,
                       int code, /* Status-Code value */
                       String reason_phrase) {
    }

    public void headers(Headers headers) {

    }

    public void locationChanged(String newLocation, boolean permanent) {

    }

    public void data(byte[] data, int len) {

         HttpLog.v(new String(data, 0, len));
    }
    public void endData() {

    }

    public void error(int id, String description) {

    }
}
