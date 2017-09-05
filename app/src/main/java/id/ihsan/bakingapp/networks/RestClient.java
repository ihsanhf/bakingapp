package id.ihsan.bakingapp.networks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import id.ihsan.bakingapp.R;
import id.ihsan.bakingapp.helpers.Constans;
import id.ihsan.bakingapp.models.Recipe;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * @author Ihsan Helmi Faisal <ihsan.helmi@ovo.id>
 * @since 2017.10.07
 */
public class RestClient {

    private static final String TAG = RestClient.class.getSimpleName();

    public static ApiService getClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS).build();

        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        Retrofit client = new Retrofit.Builder()
                .baseUrl(Constans.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(rxAdapter)
                .build();
        return client.create(ApiService.class);
    }

    public static String getErrorFail(Context context, Throwable t) {
        String error;
        if (!isInternetConnected(context)) {
            error = context.getString(R.string.no_internet_connection);
        } else if (t instanceof HttpException) {
            HttpException response = (HttpException) t;
            Log.d(TAG, "onError : " + response.code() + " : " + response.message());
            error = response.message();
        } else {
            error = context.getString(R.string.internal_server_error);
            try {
                error = error + " : " + t.getMessage();
            } catch (NullPointerException ignored) {
            }
        }
        return error;
    }

    private static boolean isInternetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public interface ApiService {

        @GET("baking.json")
        Observable<List<Recipe>> getRecipe();
    }
}
