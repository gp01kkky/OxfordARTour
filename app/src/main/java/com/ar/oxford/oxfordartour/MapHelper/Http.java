package com.ar.oxford.oxfordartour.MapHelper;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class takes a http url query and establish a connection and return the result back
 * Created by Kelvin Khoo on 22/06/2016.
 */
public class Http {

    public String read(String httpUrl) throws IOException {
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        String data = "";
        try {
            URL url = new URL(httpUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            data = stringBuffer.toString();
            bufferedReader.close();
        } catch (Exception e) {
            Log.d("HTTP URL READ ERROR", e.toString());
        } finally {
            inputStream.close();
            httpURLConnection.disconnect();
        }
        return data;
    }
}
