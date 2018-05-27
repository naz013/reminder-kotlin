package com.elementary.tasks.core.di;

import com.elementary.tasks.core.controller.EventManager;
import com.elementary.tasks.core.services.CallReceiver;
import com.elementary.tasks.core.view_models.BaseDbViewModel;
import com.elementary.tasks.reminder.lists.RemindersRecyclerAdapter;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Copyright 2018 Nazar Suhovich
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
@Singleton
@Component(modules = {AppModule.class, DbModule.class, ThemeModule.class})
public interface AppComponent {
    void inject(BaseDbViewModel viewModel);

    void inject(RemindersRecyclerAdapter adapter);

    void inject(EventManager eventManager);

    void inject(CallReceiver callReceiver);
}
