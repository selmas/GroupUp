package ch.epfl.sweng.groupup.lib.navigation;

import android.location.Location;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.epfl.sweng.groupup.lib.Optional;

public final class GoogleMapsNavigationModel extends AsyncTask<URL, Void, String> implements NavigationModelInterface {
    private final static String baseURL = "https://maps.googleapis.com/maps/api/directions/";
    private final static String format = "json?";
    private final static String key = "AIzaSyDtv0o9SNKJWLWt51YyYhZK0nxsR5FWMdY";
    private final static String mode = "walking";

    @Override
    protected String doInBackground(URL... urls) {
        if(urls.length == 1){
            HttpURLConnection urlConnection = null;
            StringBuilder response = new StringBuilder();
            try {
                urlConnection = (HttpURLConnection) urls[0].openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                int c;
                while((c = in.read()) != -1) {
                    response.append((char) c);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
                return jsonDecode(response.toString()).toString();
            }
        }
        else{
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String findRoute(Location origin, Location destination) throws IOException {
        URL url = new URL(baseURL + format + "origin=" + origin.getLatitude() + "," + origin.getLongitude() + "&destination=" + destination.getLatitude() + "," + destination.getLongitude() + "&key=" + key + "&mode=" + mode);
        return doInBackground(url);
    }

    public Optional<JSONArray> jsonDecode(String json) {
        if(json != null) {
            try {
                return Optional.from(new JSONObject(json).getJSONArray("routes"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }
}
