package id.ihsan.bakingapp.helpers;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Ihsan Helmi Faisal <ihsan.helmi@ovo.id>
 * @since 2017.04.09
 */
public class AssetHelper {

    public static String getBakingData(Context context) {
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("baking.json"), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
