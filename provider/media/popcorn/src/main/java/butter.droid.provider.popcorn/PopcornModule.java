/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.provider.popcorn;

import butter.droid.provider.base.ProviderScope;
import butter.droid.provider.popcorn.api.PopcornService;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class PopcornModule {

    @Provides @ProviderScope @PopcornQualifier HttpUrl providerUrl() {
        return HttpUrl.parse("https://tv-v2.api-fetch.website");
    }

    @Provides @ProviderScope @PopcornQualifier CallAdapter.Factory provideCallAdapter() {
        return RxJava2CallAdapterFactory.create();
    }

    @Provides @ProviderScope @PopcornQualifier Converter.Factory provideConverter(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    @Provides @ProviderScope @PopcornQualifier Retrofit provideRetrofit(OkHttpClient client, @PopcornQualifier HttpUrl url, @PopcornQualifier CallAdapter.Factory callAdapter,
            Converter.Factory converter) {
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(url)
                .addCallAdapterFactory(callAdapter)
                .addConverterFactory(converter)
                .build();
    }

    @Provides @ProviderScope PopcornService provideVodoService(@PopcornQualifier Retrofit retrofit) {
        return retrofit.create(PopcornService.class);
    }

    @Provides @ProviderScope
    PopcornProvider provideVodo(PopcornService service) {
        return new PopcornProvider(service);
    }

}
