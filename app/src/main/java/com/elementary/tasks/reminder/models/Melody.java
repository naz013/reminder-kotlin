/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elementary.tasks.reminder.models;

public class Melody {

    private String fileName;
    private String melodyPath;
    private int volume;

    public Melody(String fileName, String melodyPath, int volume) {
        this.fileName = fileName;
        this.melodyPath = melodyPath;
        this.volume = volume;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMelodyPath() {
        return melodyPath;
    }

    public void setMelodyPath(String melodyPath) {
        this.melodyPath = melodyPath;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    @Override
    public String toString(){
        return "JMelody->Volume: " + volume +
                "->Melody: " + melodyPath;
    }
}